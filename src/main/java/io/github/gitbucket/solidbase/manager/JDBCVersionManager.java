package io.github.gitbucket.solidbase.manager;

import io.github.gitbucket.solidbase.migration.MigrationUtils;
import static io.github.gitbucket.solidbase.migration.MigrationUtils.*;

import java.sql.Connection;
import java.sql.ResultSet;

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
            updateDatabase(conn, "CREATE TABLE VERSIONS (MODULE_ID VARCHAR(100) NOT NULL, VERSION VARCHAR(100) NOT NULL)");
            updateDatabase(conn, "ALTER TABLE VERSIONS ADD CONSTRAINT VERSIONS_PK PRIMARY KEY (MODULE_ID)");
        }
    }

    @Override
    public void updateVersion(String moduleId, String version) throws Exception {
        if(updateDatabase(conn, "UPDATE VERSIONS SET VERSION = ? WHERE MODULE_ID = ?", version, moduleId) == 0){
            updateDatabase(conn, "INSERT INTO VERSIONS (MODULE_ID, VERSION) VALUES (?, ?)", moduleId, version);
        }
    }

    @Override
    public String getCurrentVersion(String moduleId) throws Exception {
        return selectStringFromDatabase(conn, "SELECT VERSION FROM VERSIONS WHERE MODULE_ID = ?", moduleId);
    }

    protected boolean checkTableExist(){
        try {
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{ "TABLE" });
            try {
                while(rs.next()){
                    String tableName = rs.getString("TABLE_NAME");
                    if(tableName.toUpperCase().equals("VERSIONS")){
                        return true;
                    }
                }
            } finally {
                rs.close();
            }
        } catch(Exception ex){ /* ignore */ }
        return false;
    }

}
