package com.panamera.registry;

public interface ServiceRegistry {
//	public void register(String serviceName, String serviceAddress);
	public void register(String serviceName, String serviceIp, int servicePort);

}
