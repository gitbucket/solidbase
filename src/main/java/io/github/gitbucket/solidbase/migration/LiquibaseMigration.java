package io.github.gitbucket.solidbase.migration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.*;

import io.github.gitbucket.solidbase.Solidbase;
import liquibase.*;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Provides database migration using Liquibase.
 */
public class LiquibaseMigration implements Migration {

    private String path;

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

        String path = this.path;
        if(path == null){
            path = moduleId + "_" + version + ".xml";
        }

        // add required attributes: id and author
        Document doc = parseXml(MigrationUtils.readResourceAsString(classLoader, path));
        Element root = doc.getDocumentElement();
        if(!root.hasAttribute("id")) {
            root.setAttribute("id", version);
        }
        if(!root.hasAttribute("author")) {
            root.setAttribute("author", moduleId);
        }

        Liquibase liquibase = new Liquibase("solidbase.xml", new StringResourceAccessor(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<databaseChangeLog\n" +
                "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\"\n" +
                "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd\n" +
                "        http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                "\n" +
                printXml(doc) + "\n" +
                "</databaseChangeLog>\n", classLoader), database);

        DatabaseChangeLog changeLogs = liquibase.getDatabaseChangeLog();
        List<ChangeSet> changeSets = changeLogs.getChangeSets();
        for(ChangeSet changeSet: changeSets){
            for(Change change: changeSet.getChanges()){
                SqlStatement[] statements = change.generateStatements(database);
                Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statements, database);
                for(Sql sql: sqls){
                    MigrationUtils.update(conn, sql.toSql());
                }
            }
        }
    }

    private static Document parseXml(String xml) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        return doc;
    }

    private static String printXml(Document doc) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(out));

        return new String(out.toByteArray(), "UTF-8");
    }

    private static class StringResourceAccessor extends ClassLoaderResourceAccessor {

        private String xml;

        public StringResourceAccessor(String xml, ClassLoader classLoader){
            super(classLoader);
            this.xml = xml;
        }

        @Override
        public Set<InputStream> getResourcesAsStream(String path) throws IOException {
            if(path.equals("solidbase.xml")){
                Set<InputStream> set = new HashSet<>();
                set.add(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
                return set;
            } else {
                return super.getResourcesAsStream(path);
            }
        }
    }

}
