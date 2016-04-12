package io.github.gitbucket.solidbase.migration;

import io.github.gitbucket.solidbase.Solidbase;
import static io.github.gitbucket.solidbase.migration.MigrationUtils.*;
import liquibase.database.Database;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.Map;

/**
 * Provides database migration using a specified SQL file.
 */
public class SqlMigration implements Migration {

    private String path;

    /**
     * Creates <code>SqlMigration</code> that migrates using <code>/$MODULE_ID_$VERSION.sql</code> on the classpath.
     */
    public SqlMigration(){
        this(null);
    }

    /**
     * Creates <code>SqlMigration</code> that migrates using specified SQL file.
     *
     * @param path the resource path on the classpath.
     */
    public SqlMigration(String path){
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

        String path = this.path;
        if(path == null){
            path = moduleId + "_" + version + "_" + database.getShortName() + ".sql";
        }

        String sql = MigrationUtils.readResourceAsString(classLoader, path);
        if(sql == null){
            // Retry
            path = moduleId + "_" + version + ".sql";
            sql = MigrationUtils.readResourceAsString(classLoader, path);
        }

        if(sql == null){
            throw new FileNotFoundException(path);
        }

        updateDatabase(conn, sql);
    }

}
