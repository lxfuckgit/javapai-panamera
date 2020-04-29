package com.panamera.registry.ippool;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.panamera.registry.server.ServerDiscovery;

public final class ProxyServerDiscovery implements ProxyIpDiscovery {
	private static final Logger log = LoggerFactory.getLogger(ProxyServerDiscovery.class);
	
	private volatile ConcurrentHashMap<String, String> proxyMap = new ConcurrentHashMap<String, String>();
//	private volatile ConcurrentHashMap<String, List<ProxyIp>> proxyMap;

//	// IP池活动连接数
//	private AtomicInteger activeSize = new AtomicInteger(0);
	
	private static String zkString = "101.37.118.103:20181,101.37.20.213:20181,101.37.205.187:20181";

	private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
	CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkString).connectionTimeoutMs(3 * 1000)
			.sessionTimeoutMs(30 * 1000).retryPolicy(retryPolicy).build();

	public ProxyServerDiscovery() {
		// TODO Auto-generated constructor stub
		client.start();
		log.info("----------------->");
		
		initProxyServer();
		log.info("-------初始化---------->");

		watchProxyNode();
		log.info("----------------->");
	}

	private void watchProxyNode() {
		// TODO Auto-generated method stub
//		try {
//			client.getData().usingWatcher(new ProxyServerWatcher()).forPath("/proxy");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		/* add listener */
		TreeCache treeCache = new TreeCache(client, "/proxy");
		try {
			treeCache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		treeCache.getListenable().addListener(new TreeCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				System.out.println("监听到节点数据变化，类型：" + event.getType() + ",路径：" + event.getData().getPath());
				if (null != event.getData()) {
					switch (event.getType()) {
					case NODE_ADDED:

						break;
					case NODE_REMOVED:

						break;
					case NODE_UPDATED:

						break;
//					case GET_DATA:
//						break;

					default:
						break;
					}
				} else {

				}
			}
		});
		
	}

	@Override
	public List<ProxyIp> discovery() {
		// TODO Auto-generated method stub
		return null;
//		return proxyMap;
	}

	/**
	 * 
	 */
	protected void initProxyServer() {
		/* init from static config. */
		
		/* init from dynamic Ip(动态IP切换。). */
		String defaultPath = "/proxy";
		getChildPath(defaultPath).forEach(proxygroup -> {
			System.out.println("<------path:/proxy/" + proxygroup + "------->");
			
			if(proxygroup.startsWith("pool")) {
				
			getChildPathIfExsit(defaultPath + "/" + proxygroup).forEach(node1 -> {
					String nodeKey = defaultPath + "/" + proxygroup + "/" + node1;
					
					System.out.println("<------path:" + nodeKey + "------->");
					System.out.println("<------代理节点数据：" + getPathData(nodeKey) + "------->");
					
					JSONObject json = JSONObject.parseObject(getPathData(defaultPath + "/" + proxygroup + "/" + node1));
					if (null != json && (json.getLongValue("endTime") - 3600) <= (System.currentTimeMillis() / 1000)) {
						proxyMap.put(nodeKey, node1);
					}
				
//				System.out.println("<-------------节点子节点-------------------->");
//				getChildPathIfExsit(defaultPath + "/" + proxygroup + "/" + node1).forEach(node2 -> {
//					System.out.println("<------节点名称：" + node2 + "------->");
//					System.out.println("<------节点数据：" + getPathData(defaultPath + "/" + proxygroup + "/" + node1) + "------->");
//				});
			});
			
			}
		});
	}
	
	private List<String> getChildPath(String path) {
		try {
			return client.getChildren().forPath(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getPathData(String path) {
		try {
			return new String(client.getData().forPath(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private List<String> getChildPathIfExsit(String path) {
		try {
			Stat stat = client.checkExists().forPath(path);
			if(null != stat) {
				return client.getChildren().forPath(path);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
