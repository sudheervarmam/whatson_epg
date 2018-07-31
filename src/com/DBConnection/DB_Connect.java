package com.DBConnection;

import java.sql.*;


import com.config.PropertyConfig;

public class DB_Connect {

	private static Connection con = null;
	private static Statement stmt = null;

	public static Connection openConnection() throws Exception {

		if(con==null) {
		Class.forName(PropertyConfig.getProperties("driver"));
		con = DriverManager.getConnection(PropertyConfig.getProperties("url"),
				PropertyConfig.getProperties("dbUsername"), PropertyConfig.getProperties("dbPass"));
		stmt = con.createStatement();
		}
		return con;
	}
	
	public static void closeConnection() throws Exception {
		if (stmt != null)
			stmt.close();

		if (con != null)
			con.close();
	}
	
}
