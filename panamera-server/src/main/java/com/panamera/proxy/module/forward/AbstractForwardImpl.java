package com.panamera.proxy.module.forward;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public abstract class AbstractForwardImpl implements ForwardStrategy {

	public FullHttpResponse buildHttpResponse(int status_code, String content) {
		return buildHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(status_code), content);
	};
	
	public FullHttpResponse buildHttpResponse(String http_version, int status_code, String content) {
		return buildHttpResponse(HttpVersion.valueOf(http_version), HttpResponseStatus.valueOf(status_code), content);
	};

	public FullHttpResponse buildHttpResponse(HttpVersion version, HttpResponseStatus status, String content) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	};

}
