package io.github.gitbucket.solidbase;

import io.github.gitbucket.solidbase.manager.JDBCVersionManager;
import io.github.gitbucket.solidbase.manager.VersionManager;
import io.github.gitbucket.solidbase.migration.Migration;
import io.github.gitbucket.solidbase.model.Module;
import io.github.gitbucket.solidbase.model.Version;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class Solidbase {

    public static String CONNECTION = "solidbase.context.connection";
    public static String CLASSLOADER = "solidbase.context.classloader";
    public static String DATABASE = "solidbase.context.database";

    public void migrate(Connection conn, ClassLoader classLoader, Database database, Module module) throws Exception {
        database.setConnection(new JdbcConnection(conn));

        Map<String, Object> context = new HashMap<>();
        context.put(CONNECTION, conn);
        context.put(CLASSLOADER, classLoader);
        context.put(DATABASE, database);

        this.migrate(new JDBCVersionManager(conn), context, module);
    }

    public void migrate(VersionManager versionManager, Map<String, Object> context, Module module) throws Exception {
        versionManager.initialize();
        String currentVersion = versionManager.getCurrentVersion(module.getModuleId());

        boolean skip = true;

        if(currentVersion == null){
            skip = false;
        }

        for(Version version: module.getVersions()){
            if(!skip){
                for(Migration migration: version.getMigrations()){
                    migration.migrate(module.getModuleId(), version.getVersion(), context);
                }
                versionManager.updateVersion(module.getModuleId(), version.getVersion());
            }
            if(version.getVersion().equals(currentVersion)){
                skip = false;
            }
        }
    }

}
