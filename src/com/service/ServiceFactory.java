package com.service;

public class ServiceFactory {

	static Service service = null;
	
	public static Service getInstance(String obj) {
		if(obj.equalsIgnoreCase("whatsonServiceImpl")) {
			service = new ServiceImpl();
		}
		return service;
	}
}
