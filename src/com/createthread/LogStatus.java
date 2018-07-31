package com.createthread;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;

public class LogStatus
{
    @SuppressWarnings("deprecation")
	public static void writeLog(String data) throws Exception
    {
        FileWriter fileWritter = new FileWriter("Localisation_log.log",true);
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
        Date date=new Date();
        data=date.toLocaleString()+" ::: "+data+"\n";
        System.out.println(data);
        bufferWritter.write(data);
        bufferWritter.flush();
        bufferWritter.close();
    }

    public static void main(String[] args)
    {
        
    }
}
