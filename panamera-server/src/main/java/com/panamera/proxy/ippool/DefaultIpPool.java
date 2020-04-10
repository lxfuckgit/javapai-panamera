package com.panamera.proxy.ippool;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DefaultIpPool implements IpPool {
	/**/
	private static DefaultIpPool defalutIpPool = null;
	/**/
	private List<String> ipList = new CopyOnWriteArrayList<>();

	@Override
	public String nextIp() {
		// TODO Auto-generated method stub
		String ip = ipList.get(0);
		ipList.remove(0);
		return ip;
	}

	public void refreshIpPool() {
		System.out.println("-------refreshIpPool-------");
		
		ipList.clear();

		String[] ip_group = getIps().split("\\r\\n");
		for (int i = 0; i < ip_group.length; i++) {
			ipList.add(ip_group[i].split(",")[0]);
		}
	}

	public static DefaultIpPool getInstance() {
		if (defalutIpPool == null) {
			defalutIpPool = new DefaultIpPool();
		}
		return defalutIpPool;
	}

	private String getIps() {
		HttpURLConnection connection = null;
		InputStream is = null;
		java.io.BufferedReader br = null;
		String result = null;// 返回结果字符串
		try {
			// 创建远程url连接对象
			URL url = new URL(
					"http://proxy.xiaoxiangyoupin.cn:19876/proxy/queryProxyInfo.json?username=shoes&password=YUANshi123");
			// 通过远程url连接对象打开一个连接，强转成httpURLConnection类
			connection = (HttpURLConnection) url.openConnection();
			// 设置连接方式：get
			connection.setRequestMethod("GET");
			// 设置连接主机服务器的超时时间：15000毫秒
			connection.setConnectTimeout(15000);
			// 设置读取远程返回的数据时间：60000毫秒
			connection.setReadTimeout(60000);
			// 发送请求
			connection.connect();
			// 通过connection连接，获取输入流
			if (connection.getResponseCode() == 200) {
				is = connection.getInputStream();
				// 封装输入流is，并指定字符集
				br = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
				// 存放数据
				StringBuffer sbf = new StringBuffer();
				String temp = null;
				while ((temp = br.readLine()) != null) {
					sbf.append(temp);
					sbf.append("\r\n");
				}
				result = sbf.toString();
			}
		} catch (java.net.MalformedURLException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭资源
			if (null != br) {
				try {
					br.close();
				} catch (java.io.IOException e) {
					e.printStackTrace();
				}
			}

			if (null != is) {
				try {
					is.close();
				} catch (java.io.IOException e) {
					e.printStackTrace();
				}
			}

			connection.disconnect();// 关闭远程连接
		}

		return result;
	}

}
