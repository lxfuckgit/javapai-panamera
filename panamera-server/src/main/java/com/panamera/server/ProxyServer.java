package com.panamera.server;

/**
 * 代理服务.<br>
 * https://www.cnblogs.com/lxlx1798/p/10417676.html
 * 
 * Client -> ProxyServer(inbound) -> TargetServer(outbound)
 * 
 * @author lx
 *
 */
public interface ProxyServer {
	
	public void start();

	public void stop();

}
