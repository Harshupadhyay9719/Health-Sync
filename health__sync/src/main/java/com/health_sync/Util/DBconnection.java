package com.health_sync.Util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBconnection {
    private static final String URL = "jdbc:mysql://localhost:3306/health_sync";
    private static final String USER = "root"; 
    private static final String PASSWORD = "21696579"; 

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database Connected Successfully!\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    
}
