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

public class GradientDataModel {
    // Instance variables
    private List<Connection> connectionPool = new ArrayList<Connection>();
    private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final String DB_NAME = "test";
    private final String JDBC_URL = "jdbc:mysql://localhost/" + DB_NAME + "?useSSL=false";
    private String tableName;

    /**
     * Constructor
     */
    public GradientDataModel(String tableName) throws Exception {
        // Create table if not exists
        this.tableName = tableName;
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
    public void insert(int level, int iteration, String true_input) throws Exception {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            
            pstmt = con.prepareStatement("INSERT INTO " + tableName + 
                                         "(level, iteration, true_gradient) VALUES(?, ?, ?);");
            pstmt.setInt(1, level);
            pstmt.setInt(2, iteration);
            pstmt.setString(3, true_input);

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
     * Get new record
     */
    public String get(int level, int iteration) throws Exception {
        Connection con = null;
        Statement pstmt = null;
        String res = "";

        try {
            con = getConnection();
            con.setAutoCommit(false);

            pstmt = con.createStatement();
            ResultSet rs = pstmt.executeQuery("SELECT true_gradient FROM " + tableName + 
                            " WHERE level=" + level + " AND iteration=" + iteration + ";");

            if (rs != null) {
                while (rs.next()) {
                    res = rs.getString("true_gradient");
                }
            }

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

            return res;
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
                " (level INT NOT NULL, iteration INT NOT NULL, true_gradient TEXT, " +
                "timestamp TIMESTAMP NOT NULL DEFAULT NOW()," +
                "PRIMARY KEY (level, iteration));"
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
