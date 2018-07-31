package com.createthread;

import com.createThreads.LogStatus;
import com.service.Service;
import com.service.ServiceFactory;

public class ChannelData {

	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			LogStatus.writeLog("Argument required");
			return;
		}
		String runMode = args[0];
		if(!(runMode.equalsIgnoreCase("Regular") || runMode.equalsIgnoreCase("Change"))) {
			LogStatus.writeLog("please mention is Regular or Change");
			return;
		}
		Service service = ServiceFactory.getInstance("whatsonServiceImpl");
		try {
			service.parseXmls(runMode);
		} catch (Exception e) {
			LogStatus.writeLog(e.toString());
		}
	}

}
