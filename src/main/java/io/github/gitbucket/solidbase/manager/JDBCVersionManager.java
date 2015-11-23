package io.github.gitbucket.solidbase.manager;

import io.github.gitbucket.solidbase.migration.MigrationUtils;

import java.sql.Connection;

/**
 * Created by takezoe on 15/11/23.
 */
public class JDBCVersionManager implements VersionManager {

    private Connection conn;

    public JDBCVersionManager(Connection conn){
        this.conn = conn;
    }


    @Override
    public void initialize() throws Exception {
        if(!checkTableExist()){
            MigrationUtils.update(conn, "CREATE TABLE VERSIONS (MODULE_ID VARCHAR(100) NOT NULL, VERSION VARCHAR(100) NOT NULL)");
            MigrationUtils.update(conn, "ALTER TABLE VERSIONS ADD CONSTRAINT VERSIONS_PK PRIMARY KEY (MODULE_ID)");
        }
    }

    @Override
    public void updateVersion(String moduleId, String version) throws Exception {
        if(MigrationUtils.update(conn, "UPDATE VERSIONS SET VERSION = ? WHERE MODULE_ID = ?", version, moduleId) == 0){
            MigrationUtils.update(conn, "INSERT INTO VERSIONS (MODULE_ID, VERSION) VALUES (?, ?)", moduleId, version);
        }
    }

    @Override
    public String getCurrentVersion(String moduleId) throws Exception {
        return MigrationUtils.selectString(conn, "SELECT VERSION FROM VERSIONS WHERE MODULE_ID = ?", moduleId);
    }

    protected boolean checkTableExist(){
        try {
            MigrationUtils.selectInt(conn, "SELECT COUNT(*) FROM VERSIONS");
            return true;
        } catch(Exception ex){
            return false;
        }
    }

}
