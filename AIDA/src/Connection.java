import java.sql.*;


public class Connection {
    private static final String url = "jdbc:oracle:thin:@localhost:1521/orcl";


    public static java.sql.Connection connect(String username, String password) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (ClassNotFoundException e) {
            System.out.println("Can't find JDBC Driver!");
            e.printStackTrace();
        }

        java.sql.Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
        }
        return connection;
    }

    public static void close (java.sql.Connection c) {
        try {
            if((c != null) && !(c.isClosed())) {
                c.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}