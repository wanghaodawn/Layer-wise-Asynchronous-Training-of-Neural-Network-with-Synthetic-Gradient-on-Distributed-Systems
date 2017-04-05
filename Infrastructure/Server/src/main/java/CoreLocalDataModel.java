import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.*;

public class CoreLocalDataModel {
    // Instance variables
    private List<Connection> connectionPool = new ArrayList<Connection>();
    private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final String DB_NAME = "test";
    private final String JDBC_URL = "jdbc:mysql://localhost/" + DB_NAME + "?useSSL=false";
    private final String tableName = "core_local_data";

    /**
     * Constructor
     */
    public DataModel() throws Exception {
        // Create table if not exists
        createTable();
    }

    /**
     * Drop all items
     */
    public void dropAllItems() throws Exception {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            
            pstmt = con.prepareStatement("DELETE FROM " + tableName + ";");

            pstmt.executeUpdate();

            pstmt.close();
            
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                con.setAutoCommit(true);
                releaseConnection(con);
            }
        }
    }
    

    /**
     * Connect to MySQL Server
     */ 
    private synchronized Connection getConnection() throws Exception {
        if (connectionPool.size() > 0) {
            return connectionPool.remove(connectionPool.size() - 1);
        }

        try {
            Class.forName(JDBC_DRIVER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return DriverManager.getConnection(JDBC_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /** 
     * Release connection
     */
    private synchronized void releaseConnection(Connection con) {
        connectionPool.add(con);
    }

    /**
     * Insert new record using transaction
     */
    public void insert(int level, String x, String w, String y) throws Exception {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            
            pstmt = con.prepareStatement("INSERT INTO " + tableName + 
                                                    "(x, w, y) VALUES(?,?,?);");
            pstmt.setInt(1, level);
            pstmt.setString(2, x);
            pstmt.setString(3, w);
            pstmt.setString(4, y);

            pstmt.executeUpdate();

            pstmt.close();
            
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                con.setAutoCommit(true);
                releaseConnection(con);
            }
        }
    }

    /**
     * Create table if not exists using transaction
     */
    private void createTable() throws Exception {
        Connection con = null;
        Statement stmt = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            
            stmt = con.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + 
                " (id SERIAL, x TEXT, w TEXT, y TEXT, " +
                "timestamp TIMESTAMP NOT NULL DEFAULT NOW()," +
                "PRIMARY KEY (id));"
            );
            stmt.close();
            
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                con.setAutoCommit(true);
                releaseConnection(con);
            }
        }
    }
}
