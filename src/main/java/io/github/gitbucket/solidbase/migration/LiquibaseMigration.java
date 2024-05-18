package io.github.gitbucket.solidbase.migration;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

import io.github.gitbucket.solidbase.Solidbase;
import static io.github.gitbucket.solidbase.migration.MigrationUtils.*;

import liquibase.*;
import liquibase.change.Change;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.resource.AbstractResource;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.Resource;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;

/**
 * Provides database migration using Liquibase.
 */
public class LiquibaseMigration implements Migration {

    private final String path;

    /**
     * Creates <code>LiquibaseMigration</code> that migrates using <code>/$MODULE_ID_$VERSION.xml</code> on the classpath.
     */
    public LiquibaseMigration(){
        this(null);
    }

    /**
     * Creates <code>LiquibaseMigration</code> that migrates using specified XML file.
     *
     * @param path the resource path on the classpath.
     */
    public LiquibaseMigration(String path){
        this.path = path;
    }

    @Override
    public void migrate(String moduleId, String version, Map<String, Object> context) throws Exception {
        Connection conn = (Connection) context.get(Solidbase.CONNECTION);
        ClassLoader cl = (ClassLoader) context.get(Solidbase.CLASSLOADER);
        Database db = (Database) context.get(Solidbase.DATABASE);

        migrate(conn, db, cl, moduleId, version, context);
    }

    protected void migrate(Connection conn, Database database, ClassLoader classLoader,
                           String moduleId, String version, Map<String, Object> context) throws Exception {

        List<String> fileNames = new ArrayList<>();
        if(this.path != null){
            if(this.path.endsWith(".xml")){
                fileNames.add(this.path.replaceFirst("\\.xml$", "_" + database.getShortName() + ".xml"));
            }
            fileNames.add(this.path);
        }
        fileNames.add(moduleId + "_" + version + "_" + database.getShortName() + ".xml");
        fileNames.add(moduleId + "_" + version + ".xml");

        String path = null;
        String source = null;
        for(String fileName: fileNames){
            source = MigrationUtils.readResourceAsString(classLoader, fileName);
            if(source != null){
                path = fileName; 
                break;
            }
        }
        
        Liquibase liquibase = new Liquibase(path, new StringResourceAccessor(path,
                new LiquibaseXmlPreProcessor().preProcess(moduleId, version, source), classLoader), database);

        ChangeLogParameters params = liquibase.getChangeLogParameters();
        params.set("currentDateTime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));

        DatabaseChangeLog changeLogs = liquibase.getDatabaseChangeLog();
        List<ChangeSet> changeSets = changeLogs.getChangeSets();
        for(ChangeSet changeSet: changeSets){
            for(Change change: changeSet.getChanges()){
                SqlStatement[] statements = change.generateStatements(database);
                Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statements, database);
                for(Sql sql: sqls){
                    updateDatabase(conn, sql.toSql());
                }
            }
        }
    }

    private static class StringResourceAccessor extends ClassLoaderResourceAccessor {
        private final String fileName;
        private final String source;
        private final ClassLoader classLoader;

        public StringResourceAccessor(String fileName, String source, ClassLoader classLoader){
            super(classLoader);
            this.fileName = fileName;
            this.source = source;
            this.classLoader = classLoader;
        }

        @Override
        public List<Resource> getAll(String path) throws IOException {
            if(path.equals(fileName)){
                List<Resource> returnList = new ArrayList<>();
                try {
                    URI uri = classLoader.getResources(path).nextElement().toURI();
                    returnList.add(new ByteArrayResource(source, path, uri));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return returnList;
            } else {
                throw new FileNotFoundException(path);
            }
        }
    }

    private static class ByteArrayResource extends AbstractResource {
        private final String source;

        public ByteArrayResource(String source, String path, URI uri) {
            super(path, uri);
            this.source = source;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Resource resolve(String other) {
            return null;
        }

        @Override
        public Resource resolveSibling(String other) {
            return null;
        }
    }

}
