package com.lights0123.Waked;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Waked {
	public static void main(final String[] args){
		logger.info("Starting up...");
		logger.info("Creating config folder...");
		File home = new File(getAppData());
		File settingsDirectory = new File(home, "Waked");
		if(!settingsDirectory.exists()) {
			logger.severe("Config directory missing. This prevents this from connecting to the website!");
		}
		File settingsFile = new File(settingsDirectory, "Waked.properties");
		if(!settingsFile.exists()) {
			logger.severe("Config file missing. This prevents this from connecting to the website!");
		}
		String APIKey=null;
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(settingsFile));
			APIKey=properties.getProperty("APIKey");
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Port mapping...");
		int port=0;
		if (!PortMapper.Map(7,false)){
			logger.warning("Port 7 failed. Trying port 9...");
			if (!PortMapper.Map(9,false)){
				logger.severe("Port 9 failed or the router does not support UPnP or NAT-PMP. (Insert error sound here)");
			}else{
				logger.info("Port 9 mapped successfully!");
				port=9;
			}
		}else{
			logger.info("Port 7 mapped successfully!");
			port=7;
		}
		String MACAddress=HTTPManager.getMACAddress();
		if(MACAddress==null){
			logger.warning("MAC Address is null!");
		}else{
			logger.info("MAC Address: "+MACAddress);
		}
		/*Map<String,String> mapA = new HashMap<>();

		mapA.put("port", port);
		mapA.put("MAC", MACAddress);
		HTTPManager.HTTPSend("http://github.com")*/
	}
	private final static Logger logger = Logger.getLogger(Waked.class.getName());
	public static String getAppData(){
		String OS = System.getProperty("os.name").toUpperCase();
		if (OS.contains("WIN")) {
			return System.getenv("APPDATA");
		}else if (OS.contains("MAC")) {
			return System.getProperty("user.home") + "/Library/Application "
					+ "Support";
		}else if (OS.contains("NUX")) {
			return System.getProperty("user.home") + "/.config";
		}
		return System.getProperty("user.dir");
	}

}