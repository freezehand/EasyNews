package com.example.easynews.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBOpenHelper {
    private static String driver = "com.mysql.jdbc.Driver"; //MySql驱动
    private static String url = "jdbc:mysql://192.168.1.209:3306/user?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"; //MySql数据库连接url
/*    private static String url = "jdbc:mysql://172.20.10.4:3306/user?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"; //MySql数据库连接url*/

    private static String user = "root"; //用户名
    private static String paswword = "Xcq883205"; //密码
    /*连接数据库*/
    public static Connection getConn(){
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = (Connection) DriverManager.getConnection(url,user,paswword); //获取连接
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  conn;

    }
}
