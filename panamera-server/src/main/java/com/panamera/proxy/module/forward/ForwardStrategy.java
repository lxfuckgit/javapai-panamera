package com.panamera.proxy.module.forward;

import com.panamera.proxy.ippool.IpPool;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public interface ForwardStrategy {
	/**
	 * 
	 * @return
	 */
	public String normalForward();

	/**
	 * 通过代理服务器转发请求.<br>
	 * 当Server接受到http请求后，Server将根据相应的策略(默认按加入顺序)将http请求转发到代理服务器上。<br>
	 * 代理服务器IP资源来源于Ip池{@link IpPool}。<br>
	 * 
	 * @return 请求内容。<br>
	 */
	public FullHttpResponse proxyForward(FullHttpRequest request);
	//public FullHttpResponse proxyForward();

}
