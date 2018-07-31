package com.ftpconnect;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.config.PropertyConfig;
import com.createthread.LogStatus;


public class FtpConnect {
	
	public static String EPG_Loc = null;
	public static String nonLocalisedEPG_Location = "NonLocalisedEPG";
	public static String localisedEPG_Location = "test_epg";

	public static String changenonLocalisedEPG_Location = "ChangeNoneLocalisedEPG";
	public static String changelocalisedEPG_Location = "change_test_epg";

	public static String nonLocalEPG = nonLocalisedEPG_Location;
	public static String localEPG = localisedEPG_Location;
	public static String epgDir = "Regular";
	
	static FTPClient ftp =null;

	
	public static String getEPGXMLs(String runMode) throws Exception {

		String status = null;
		String filename = null;
		
		if(runMode.equalsIgnoreCase("change")){
			nonLocalEPG = changenonLocalisedEPG_Location;
			localEPG = changelocalisedEPG_Location;
			epgDir = "Change";
		}
		
		try {
			deleteDir(new File(nonLocalEPG));
			new File(nonLocalEPG).mkdir();
			 ftp = new FTPClient();
			LogStatus.writeLog("ftp ip is:::" +PropertyConfig.getProperties("ftpIp"));
			ftp.connect(PropertyConfig.getProperties("ftpIp"));
			ftp.login(PropertyConfig.getProperties("ftpUsername"), PropertyConfig.getProperties("ftpPass"));
			ftp.enterLocalPassiveMode();
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			Date dt = new Date();
			FTPFile[] ftpDirs = ftp.listDirectories();
			ArrayList<String> dr = new ArrayList<>();
			for (FTPFile file : ftpDirs) {
				if (file.isFile())
					continue;
				dr.add(file.getName());
			}
			String date;
			if(runMode.equalsIgnoreCase("change")) {
				 date = getTodayDateString();
				 LogStatus.writeLog("Date of EPG : " + date + "\n");
			}else {
			 date = getYesterdayDateString();
			LogStatus.writeLog("Date of EPG : " + date + "\n");
			}
			Boolean changeStatus = ftp.changeWorkingDirectory(date + "/"+epgDir+"/");
			
			if(!changeStatus) {
				 ftp.changeWorkingDirectory(getYesterdayDateString() + "/"+epgDir+"/");
				 LogStatus.writeLog("Date of EPG Changed to: " + getYesterdayDateString() + "\n");
			}
			FTPFile[] ftpFiles = ftp.listFiles();
			ArrayList<FTPFile> ftpZipFiles = new ArrayList<>();
			ArrayList<FTPFile> ftpXmlFiles = new ArrayList<>();
			for (int i = 0; i < ftpFiles.length; i++) {
				String ext = FilenameUtils.getExtension(ftpFiles[i].getName());
				if(ext.equalsIgnoreCase("xml")) {
					ftpXmlFiles.add(ftpFiles[i]);
				}else {
					ftpZipFiles.add(ftpFiles[i]);
				}
			}
			List<String> allowedFiles = new ArrayList<String>();
			allowedFiles = Arrays.asList(PropertyConfig.getProperties("allowedExtensions").split(","));
			if (ftpFiles != null && ftpZipFiles.size() > 0) {
				FTPFile file = getMaxLastModified(ftpZipFiles);
					LogStatus.writeLog("Allowed extensions are :" + FilenameUtils.getExtension(file.getName()));
					LogStatus.writeLog(allowedFiles.get(0));
					if(!allowedFiles.contains(FilenameUtils.getExtension(file.getName()))){
						LogStatus.writeLog("Not allowed for " + file.getName());
					}
					filename = file.getName();
					LogStatus.writeLog("\nExecution started on "
							+ dt.toString() + "\n\nFile is " + filename + "\n");

					OutputStream output;
					output = new FileOutputStream(nonLocalEPG
							+ "/" + filename);

					ftp.retrieveFile(filename, output);
					output.close();

					LogStatus.writeLog("\nUnzipping files.....\n");
					EPG_Loc = nonLocalEPG + "/"
							+ filename.substring(0, filename.length() - 4);

					unzip(nonLocalEPG + "/" + filename, EPG_Loc);
			}
			if(filename==null) {
				EPG_Loc = nonLocalEPG;
			}
			for (int i = 0; i < ftpXmlFiles.size(); i++) {
				copyXMLs(EPG_Loc,ftpXmlFiles.get(i).getName());
			}
			
			ftp.logout();
			LogStatus
					.writeLog("\nUnziping completed, all EPG XML files unziped to "
							+ EPG_Loc + "\n");
			status = "SUCCESS";
			return status;
		} catch (Exception e) {
			LogStatus.writeLog("exception occurs while ftp connect::"+e.toString());
			status = "FAILED";
		}
		return status;

	}
	
	
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	
	public static String getYesterdayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return dateFormat.format(cal.getTime());
	}
	
	public static String getTodayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	
	public static  FTPFile getMaxLastModified(FTPFile[] ftpFiles) {
	    return Collections.max(Arrays.asList(ftpFiles), new LastModifiedComparator());
	}
	
	public static  FTPFile getMaxLastModified(ArrayList<FTPFile> ftpFiles) {
	    return Collections.max(ftpFiles, new LastModifiedComparator());
	}
	
	public static class LastModifiedComparator implements Comparator<FTPFile> {

	    public int compare(FTPFile f1, FTPFile f2) {
	        return f1.getTimestamp().compareTo(f2.getTimestamp());
	    }
	}
	
	public static void unzip(String zipFilePath, String destDirectory)
			throws Exception {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(
				zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		
		// iterates over entries in the zip file
		int i = 1;
		try {
		while (entry != null) {
			String filePath = destDirectory + "/" + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				// System.out.println(filePath);
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
			i++;
		}
		}catch(Exception e) {
			LogStatus.writeLog("Unzipping failed at channle no:  " + i + "");
		}
		zipIn.close();
	}

	private static void extractFile(ZipInputStream zipIn, String filePath)
			throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(filePath));
		byte[] bytesIn = new byte[1024];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}
	
	private static void copyXMLs(String path,String filename) throws IOException{
		OutputStream output = new FileOutputStream(path
				+ "/" + filename);
		ftp.retrieveFile(filename, output);
	}
	
}
