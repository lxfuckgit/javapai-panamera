package com.panamera.registry;

import com.panamera.registry.ippool.ProxyServerDiscovery;

public class TestDiscovery {// auto discovery.

	public static void main(String[] args) {
//		CuratorFramework client1 = ZkClient.getClient();
//		// 测试事件
//		MyCuratorClient.update(client1, "/father/me", "me2");
//		MyCuratorClient.query(client1, "/father/me");// 查询不会触发
//		MyCuratorClient.delete(client1, "/father/me");
		
		ProxyServerDiscovery testProxy = new ProxyServerDiscovery();
		
		
	     //线程睡眠，等待监听事件响应
        try {
			Thread.sleep(500000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        client.close();
	}

}
