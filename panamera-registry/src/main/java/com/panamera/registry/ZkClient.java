package com.panamera.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZkClient {

	private final static int CONNECTION_TIMEOUT = 3 * 1000;
	private static String zkString = "101.37.118.103:20181,101.37.20.213:20181,101.37.205.187:20181";

	private static RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
//	CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.128.129:2181", 5000, 5000, retryPolicy);
	private static CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkString)
			.connectionTimeoutMs(CONNECTION_TIMEOUT).sessionTimeoutMs(30 * 1000).retryPolicy(retryPolicy)
			// 命名空间 .namespace("super")
			.build();

	/* connect server. */
	public static CuratorFramework getClient() {
		if (null != client && !client.getZookeeperClient().isConnected()) {
//		if (!client.getZookeeperClient().isConnected()) {
			client.start();
			System.out.println("zk client start successfully!");
		} else {
			System.out.println("zkclient:" + client);
		}

		return client;
//		return client.getZookeeperClient();
	}
	
	

}
