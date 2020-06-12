package com.thomsonreuters.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class DatabaseChecker {

    private static String DBURL = "jdbc:oracle:thin:@//localhost:1521/DOCKER";
    private static String DBUSER = "TRERP";
    private static String DBPASS = "TRERP";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Logger logger = null;

        String className = "";
        String methodName = "";
        if (Thread.currentThread().getStackTrace().length > 2) {
            className = Thread.currentThread().getStackTrace()[2].getClassName();
            methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            methodName = methodName + "(" + Thread.currentThread().getStackTrace()[2].getLineNumber() + ")";

            logger = Logger.getLogger(className);
        } else {
            logger = Logger.getLogger(DatabaseChecker.class);
        }

        logger.info("Openning new Database Connection");

        Class.forName("oracle.jdbc.OracleDriver");
        String url = DBURL;
        String user = DBUSER;
        String pass = DBPASS;
        Connection con = DriverManager.getConnection(url, user, pass);

        logger.info("Connected to Oracle Database");

        return con;
    }

}