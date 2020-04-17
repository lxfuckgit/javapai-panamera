package com.panamera.proxy.ippool;

public interface IpPool {

	public String nextIp();
	
	public void initIpPool();
	
	/**
	 * IP池有效性检查。<br>
	 */
	public void healthCheck();

}
