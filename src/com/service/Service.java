package com.service;

public interface Service {

	public void parseXmls(String runMode) throws Exception;
	public void channelDetails(String path) throws Exception;
	public String getChannelName();
	public String getEndTime(String startTime, String duration) throws Exception;
	public String changeEndTime(String endTime) throws Exception;
}
