package com.panamera.registry.ippool;

import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerWatcher implements CuratorWatcher {
	private static final Logger log = LoggerFactory.getLogger(ProxyServerWatcher.class);

	@Override
	public void process(WatchedEvent event) throws Exception {
		// TODO Auto-generated method stub
		log.info("接收到CuratorWatcher事件，事件类型：{}，节点名称：{}", event.getType(), event.getPath());
	}
}
