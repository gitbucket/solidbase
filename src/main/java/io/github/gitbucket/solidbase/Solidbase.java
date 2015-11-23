package io.github.gitbucket.solidbase;

import io.github.gitbucket.solidbase.migration.Migration;
import io.github.gitbucket.solidbase.migration.MigrationUtils;
import io.github.gitbucket.solidbase.model.Module;
import io.github.gitbucket.solidbase.model.Version;
import liquibase.database.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Solidbase {

    public static String CONNECTION = "solidbase.context.connection";
    public static String CLASSLOADER = "solidbase.context.classloader";
    public static String DATABASE = "solidbase.context.database";

    public void migrate(Connection conn, ClassLoader classLoader, Database database, Module module) throws Exception {
        createTableIfNotExist(conn);
        String currentVersion = getCurrentVersion(conn, module.getModuleId());

        // TODO Should context is given from out of solidbase?
        Map<String, Object> context = new HashMap<>();
        context.put(CONNECTION, conn);
        context.put(CLASSLOADER, classLoader);
        context.put(DATABASE, database);

        boolean skip = true;

        if(currentVersion == null){
            skip = false;
        }

        for(Version version: module.getVersions()){
            if(!skip){
                for(Migration migration: version.getMigrations()){
                    migration.migrate(module.getModuleId(), version.getVersion(), context);
                }
                updateVersion(conn, module.getModuleId(), version.getVersion());
            }
            if(version.getVersion().equals(currentVersion)){
                skip = false;
            }
        }
    }

    protected void updateVersion(Connection conn, String moduleId, String version) throws SQLException {
        if(MigrationUtils.update(conn, "UPDATE VERSIONS SET VERSION = ? WHERE MODULE_ID = ?", version, moduleId) == 0){
            MigrationUtils.update(conn, "INSERT INTO VERSIONS (MODULE_ID, VERSION) VALUES (?, ?)", moduleId, version);
        }
    }

    protected void createTableIfNotExist(Connection conn) throws SQLException {
        if(!checkTableExist(conn)){
            MigrationUtils.update(conn, "CREATE TABLE VERSIONS (MODULE_ID VARCHAR(100) NOT NULL, VERSION VARCHAR(100) NOT NULL)");
            MigrationUtils.update(conn, "ALTER TABLE VERSIONS ADD CONSTRAINT VERSIONS_PK PRIMARY KEY (MODULE_ID)");
        }
    }

    protected String getCurrentVersion(Connection conn, String moduleId) throws SQLException {
        return MigrationUtils.selectString(conn, "SELECT VERSION FROM VERSIONS WHERE MODULE_ID = ?", moduleId);
    }

    protected boolean checkTableExist(Connection conn){
        try {
            MigrationUtils.selectInt(conn, "SELECT COUNT(*) FROM VERSIONS");
            return true;
        } catch(Exception ex){
            return false;
        }
    }

}
