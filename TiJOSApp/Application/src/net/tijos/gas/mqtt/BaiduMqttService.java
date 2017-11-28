package net.tijos.gas.mqtt;

import java.util.Formatter;

public class BaiduMqttService extends MqttService {
	
	private static final String CLIENT_ID = System.getProperty("host.name");;
	private static final String BROKER = "tcp://gas.mqtt.iot.bj.baidubce.com:1883";;
	private static final String USER_NAME = format("gas/%s", CLIENT_ID);
	private static final String USER_PASS = "jGnMrrkc0717sUn4yqDvJyM/6xdClsZEReeGWJzryP4=";
	
	

	@Override
	public String getBroker() {
		return BROKER;
	}

	@Override
	public String getClientId() {
		return CLIENT_ID;
	}

	@Override
	public String getUserName() {
		return USER_NAME;
	}

	@Override
	public String getPassword() {
		return USER_PASS;
	}

	@Override
	public boolean isAutomaticReconnect() {
		return true;
	}
	
	public static String format(String format, Object... args) {
	       return new Formatter().format(format, args).toString();
	    }

	
}
