package com.panamera.registry.service;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.panamera.registry.ServiceDiscovery;
import com.panamera.registry.ZkClient;

public class DefaultServiceDiscovery implements ServiceDiscovery {
	@Override
	public void subscribe() {
		// TODO Auto-generated method stub
	}
	
//	public CuratorZookeeperClient getClient() {
//		connect();
//		return client.getZookeeperClient();
//	}
	
	public static void main(String[] args) {
		CuratorFramework client1 = ZkClient.getClient();
		
		try {
			System.out.println(client1.getData().forPath("/"));
			System.out.println(client1.getChildren().forPath("/"));
			System.out.println(client1.getChildren().forPath("/proxy"));
			
			client1.getChildren().forPath("/proxy").forEach(action -> {
				try {
					System.out.println(client1.getChildren().forPath("/proxy/" + action));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
