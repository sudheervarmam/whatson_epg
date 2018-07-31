package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.DBConnection.DB_Connect;
import com.createthread.LogStatus;
import com.model.WhatsonChannelData;


public class DaoImpl {
	private static PreparedStatement ps = null;
	private static Connection con = null;

	public static synchronized void insertValues(WhatsonChannelData bean) throws Exception {
		try {
		con = DB_Connect.openConnection();
	    ps = con.prepareStatement("INSERT INTO whatson_epg(channel_name, program_name, start_time, end_time, genre) values(?,?,?,?,?)");
		ps.setString(1, bean.getChannelName());
		ps.setString(2, bean.getProgramName());
		ps.setString(3, bean.getStartTime());
		ps.setString(4, bean.getEndTime());
		ps.setString(5, bean.getGenere());
		ps.executeUpdate();
		ps.close();
		}catch (Exception e) {
			LogStatus.writeLog("Error occur while insertion of values");
		}
	}
	
	public static synchronized void deleteValues(WhatsonChannelData bean) throws Exception {
		try {
			con = DB_Connect.openConnection();
			ps = con.prepareStatement("delete from whatson_epg where channel_name=? and start_time between ? AND ?");
			ps.setString(1, bean.getChannelName());
			ps.setString(2, bean.getStartTime());
			ps.setString(3, bean.getChangedEndTime());
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			LogStatus.writeLog("Error occur while deleting duplicate values");
		}
	    
	}
	
}
