package com.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.comparator.SizeFileComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.DBConnection.DB_Connect;
import com.createthread.LogStatus;
import com.dao.DaoImpl;
import com.ftpconnect.FtpConnect;
import com.model.WhatsonChannelData;
import com.utils.SlackUtility;

public class ServiceImpl implements Service {
	
	private static WhatsonChannelData bean;
	private static DocumentBuilderFactory docFactory;
	private static DocumentBuilder docBuilder;
	private static Document doc;
	
	public static String hpath;
	SlackUtility slack  = null ;

	public void parseXmls(String runMode) throws Exception {
		try {
			LogStatus.writeLog("Insertion of WhatsOn Channels EPG data into DB started");
			String ftpStatus = FtpConnect.getEPGXMLs(runMode);
			if (ftpStatus != null && ftpStatus.equalsIgnoreCase("SUCCESS")) {
				hpath = FtpConnect.EPG_Loc;

				File folder = new File(ServiceImpl.hpath);
				File listOfFiles[] = folder.listFiles();
				Arrays.sort(listOfFiles, SizeFileComparator.SIZE_COMPARATOR);
				int no_of_files = listOfFiles.length;
				for (int i = 0; i < no_of_files; i++) {
					if (listOfFiles[i].isFile()) {
						String fl = listOfFiles[i].getName();
						if (fl.substring(fl.lastIndexOf(".") + 1).equals("xml")) {
							channelDetails(ServiceImpl.hpath + "/" + fl);
							Thread.sleep(10);
					//		return;
						}
					}
				}
			} else {
				LogStatus.writeLog("EPG Not available or ftp not connected");
				slack.slackMessage("EPG Not available \n");

			}
			LogStatus.writeLog("Insertion of WhatsOn Channels EPG data into DB Completed");
		} catch (Exception e) {
			LogStatus.writeLog("something happend while parsing xml files::" + e.toString());
			slack.slackMessage("something happend while parsing xml files::" + e.toString());
		} finally {
			DB_Connect.closeConnection();
			LogStatus.writeLog("DB Connection closed");

		}
	}

	public void channelDetails(String path) throws Exception {
		try {
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(path);
			
			ServiceImpl.bean = new WhatsonChannelData();

			String channelName = getChannelName();
			LogStatus.writeLog(channelName + " started");
			Node sies = doc.getElementsByTagName("SiEventSchedule").item(0);
			Node siser_node = sies;
			Element siser_element = (Element) siser_node;
			NodeList siser_nodelist = siser_element.getElementsByTagName("SiEvent");
			
			for (int i = 0; i < siser_nodelist.getLength(); i++) {
				Node sievent = siser_element.getElementsByTagName("SiEvent").item(i);
				Element sievent_element = (Element) sievent;
				Node programNameNode = sievent_element.getElementsByTagName("eventName").item(0);
				String programName = programNameNode.getChildNodes().item(0).getNodeValue().toString();
				Node startTimeNode = sievent_element.getElementsByTagName("displayDateTime").item(0);
				String start_time = startTimeNode.getChildNodes().item(0).getNodeValue().toString();
				Node durationNode = sievent_element.getElementsByTagName("displayDuration").item(0);
				String duration = durationNode.getChildNodes().item(0).getNodeValue().toString();
				String endTime = getEndTime(start_time,duration);
				String changedEndTime = changeEndTime(endTime);
				Node genereNode = sievent_element.getElementsByTagName("genreId").item(0);
				String genere = genereNode.getChildNodes().item(0).getNodeValue().toString();
				bean.setChannelName(channelName);
				bean.setProgramName(programName);
				bean.setStartTime(start_time);
				bean.setEndTime(endTime);
				bean.setGenere(genere);
				bean.setChangedEndTime(changedEndTime);
				DaoImpl.deleteValues(bean);
				try {
				DaoImpl.insertValues(bean);
				}catch (Exception e) {
				LogStatus.writeLog("Data insertion skipped for progarm :"+ programName + " in channel :"+channelName);
				}
			}
			LogStatus.writeLog(channelName + " Completed");
		}catch (Exception e) {
			LogStatus.writeLog("Exception occurs while parsing xml data :: " + e.toString());
			e.printStackTrace();
		}
		
	}

	public String getChannelName() {
		NodeList sies = doc.getElementsByTagName("SiEventSchedule");
		Node siser_node = sies.item(0);
		Element siser_element = (Element) siser_node;
		NodeList siser_nodelist = siser_element
				.getElementsByTagName("siService");
		Element siser_element2 = (Element) siser_nodelist.item(0);
		NodeList siser_fstNm = siser_element2.getChildNodes();
		String channelName = siser_fstNm.item(0).getNodeValue().toString();
		channelName = channelName.replaceAll("[-+.^:,]", "");
		return channelName;
	}
	
	public String getEndTime(String startTime, String duration) throws Exception {
		String endTime = null;
		try {
		String[] startTimeArr = startTime.split(" ");
		String[] timeArr  = startTimeArr[1].split(":");
		int end_mi = (Integer.parseInt(timeArr[0]) * 60) + Integer.parseInt(timeArr[1]) + Integer.parseInt(duration);
		String end_time = ""+startTimeArr[0]+" "+(end_mi/60)+":"+(end_mi%60)+":"+timeArr[2];
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date2 = simpleDateFormat.parse(end_time);
        simpleDateFormat.format(date2);
        endTime = simpleDateFormat.format(date2);
		}catch (Exception e) {
			LogStatus.writeLog("Exception occurs while getting End Time :: "+e.toString());
		}
		return endTime;
	}
	
	public String changeEndTime(String endTime) throws Exception {
		String finalDate = null;
		try {
		String[] date_arr = endTime.split(" ");
		String[] time_arr = date_arr[1].split(":");
		int minutes = (Integer.parseInt(time_arr[0])*60) + Integer.parseInt(time_arr[1]) - 1;
		String new_endTime = String.valueOf(minutes/60) + ":" + String.valueOf(minutes%60) + ":" +"00";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date2 = simpleDateFormat.parse(date_arr[0] + " " +new_endTime);
        finalDate = simpleDateFormat.format(date2);
		}catch (Exception e) {
			LogStatus.writeLog("Exception occured while changing the end time :: "+e.toString());
		}
		return finalDate;
	}
	
}
