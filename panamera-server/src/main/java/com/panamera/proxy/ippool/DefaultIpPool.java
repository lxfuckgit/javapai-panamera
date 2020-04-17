package com.panamera.proxy.ippool;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 默认IP池客户端。<br>
 * 
 * @author lx
 *
 */
public final class DefaultIpPool implements IpPool {
	/**/
	private static int QUEUE_SIZE = 3000;
	/**/
	private static int QUEUE_MIN_SIZE = 100;
	/**/
	private static DefaultIpPool defalutIpPool = null;
	/**/
	private List<String> ipList = new CopyOnWriteArrayList<>();
	/**/
	private BlockingQueue<ProxyIp> ipQueue = new LinkedBlockingQueue<ProxyIp>(QUEUE_SIZE);
	
	@Override
	public String nextIp() {
		// TODO Auto-generated method stub
//		return ipList.remove(0);//remove(index) = get(index) & remove(index);
		
//		return ipList.get(new java.util.Random().nextInt(ipList.size() + 1));
		
		ProxyIp next = ipQueue.poll();//(30, TimeUnit.SECONDS);
		System.out.println("---------->" + ipQueue.size());
		System.out.println("---主机名--------->" + next.getHost());
		System.out.println("---return ip--->" + next.getIp());
		System.out.println("---有效期--------->" + next.getCloseTime());
		if (validateProxyStatus(next.getCloseTime())) {
			ipQueue.add(next);
		} else {
			ipQueue.remove(next);
			System.out.println("---remove and find next!--->" + next.getIp());
			nextIp();
		}
		
		checkMinQueue();
		return next.getIp() + ":" + next.getPort();
	}
	
	@Override
	public void initIpPool() {
		// TODO Auto-generated method stub
		/**/
		getConfigIps();

		/**/
//		getDynamicIps();
		refreshIpPool();
	}

//	public void addIp();
	public void refreshIpPool() {
		System.out.println("-------refreshIpPool-------");

		/**/
		System.out.println("-------static config refresh!-------");

		/**/
		System.out.println("-------dynamic config refresh!-------");
		String[] ip_group = getDynamicIps().split("\\r\\n");
		for (int i = 0; i < ip_group.length; i++) {
			String target[] = ip_group[i].split(",");
			if (ipQueue.size() != QUEUE_SIZE && validateProxyStatus(Long.valueOf(target[4]))) {
				ProxyIp proxy = new ProxyIp(target[2]);
				proxy.setIp(target[0].split(":")[0]);
				proxy.setPort(Integer.valueOf(target[0].split(":")[1]));
				proxy.setOpenTime(Long.valueOf(target[3]));
				proxy.setCloseTime(Long.valueOf(target[4]));
				ipQueue.add(proxy);
			} else {
				// will export.
			}
		}
		
		/**/
		checkMinQueue();
	}

	public static DefaultIpPool getInstance() {
		if (defalutIpPool == null) {
			defalutIpPool = new DefaultIpPool();
			defalutIpPool.initIpPool();
		}
		return defalutIpPool;
	}
	
	private String getConfigIps() {
		return null;
	}

	private String getDynamicIps() {
		HttpURLConnection connection = null;
		InputStream is = null;
		java.io.BufferedReader br = null;
		String result = null;// 返回结果字符串
		
		
//		String ipurl ="http://proxy.xiaoxiangyoupin.cn:19876/proxy/queryProxyInfo.json?username=shoes&password=YUANshi123";
		String ipurl ="http://proxy.xiaoxiangyoupin.cn:19876/proxy/queryProxyInfo.json?username=checkuser&password=YUANshi123";
			
		try {
			// 创建远程url连接对象
			URL url = new URL(ipurl);
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

	/**
	 * 
	 * @param proxyTime
	 * @return 有效Ture/无效False
	 */
	public boolean validateProxyStatus(long proxyTime) {
		//120s之后有效的ip.
		if ((proxyTime - 120) >= (System.currentTimeMillis() / 1000)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void checkMinQueue() {
		if (ipQueue.size() <= QUEUE_MIN_SIZE) {
			refreshIpPool();
		}
		if (ipQueue.size() <= QUEUE_MIN_SIZE) {
			System.out.println("------无法保证最小有效IP数量-----------------------");
		}
	}

	
	@Override
	public void healthCheck() {
		// TODO Auto-generated method stub
		// 队列自我检查。
		System.out.println("-------checkIpPool---->");
//		ipQueue.stream().filter(x -> !validateProxyStatus(x.getCloseTime()));
		ipQueue.forEach(quque -> {
			if (!validateProxyStatus(quque.getCloseTime())) {
				ipQueue.remove(quque);
			}
		});
		checkMinQueue();
	}
}
