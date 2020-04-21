package com.panamera.common.config;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class ServerConfig {
	private static Map<String, Object> config = null;
	private static ArrayList<Map<String, Object>> portArr = null;

	public ServerConfig() {
		try {
			// 定位当前文件夹路径(zrp)
			String dir = System.getProperty("user.dir");
//            String dir = System.getProperty("user.home");

			File file = new File(dir + File.separator + "server-config.yaml");
			InputStream in = new FileInputStream(file);
			Yaml yaml = new Yaml();
			config = (Map<String, Object>) yaml.load(in);
//	        portArr = (ArrayList<Map<String, Object>>) get("config");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Object getConfig(String key) {
		if (config == null) {
			new ServerConfig();
		}
		return config.get(key);
	}

	public static ArrayList<Map<String, Object>> getPortArray() {
		if (portArr == null) {
			new ServerConfig();
		}
		return portArr;
	}

}
