package io.github.gitbucket.solidbase.migration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides convenience methods which are useful in migration processing.
 */
public class MigrationUtils {

    public static int updateDatabase(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        try {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } finally {
            if(stmt != null){
                stmt.close();
            }
        }
    }

    public static Integer selectIntFromDatabase(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        try {
            setParameters(stmt, params);
            ResultSet rs = stmt.executeQuery();
            try {
                if(rs.next()){
                    return rs.getInt(1);
                } else {
                    return null;
                }
            } finally {
                if(rs != null){
                    rs.close();
                }
            }
        } finally {
            if(stmt != null){
                stmt.close();
            }
        }
    }

    public static String selectStringFromDatabase(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        try {
            setParameters(stmt, params);
            ResultSet rs = stmt.executeQuery();
            try {
                if(rs.next()){
                    return rs.getString(1);
                } else {
                    return null;
                }
            } finally {
                if(rs != null){
                    rs.close();
                }
            }
        } finally {
            if(stmt != null){
                stmt.close();
            }
        }
    }

    private static void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for(int i = 0; i < params.length; i++){
            Object param = params[i];
            if(param instanceof Integer){
                stmt.setInt(i + 1, (Integer) param);
            } else if(param instanceof String){
                stmt.setString(i + 1, (String) param);
            } else {
                // TODO unsupported
            }
        }
    }

    public static String readStreamAsString(InputStream in) throws IOException {
        if(in == null){
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024 * 8];
            int length = 0;
            while((length = in.read(buf)) != -1){
                out.write(buf, 0, length);
            }
            return new String(out.toByteArray(), "UTF-8");
        } finally {
            if(in != null){
                in.close();
            }
        }
    }

    public static String readResourceAsString(ClassLoader cl, String path) throws IOException {
        return readStreamAsString(cl.getResourceAsStream(path));
    }

    public static void ignoreException(ThrowableRunnable f){
        try {
            f.run();
        } catch(Exception ex){
            // Do nothing
        }
    }

    public interface ThrowableRunnable {
        void run() throws Exception;
    }

}
