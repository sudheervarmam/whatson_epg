package com.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyConfig {
	
private static Properties prop = null;
	
	public static void setProperties()
	{
		InputStream is = null;
        try {
            prop = new Properties();
             is = new FileInputStream("properties.properties");
            prop.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

	}
	
	public static String getProperties(String name)
	{
		if(prop == null)
		{
			setProperties();
		}
		return prop.getProperty(name);
	}



}
