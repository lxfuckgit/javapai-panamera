package com.panamera.proxy.module.forward;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.panamera.proxy.ippool.DefaultIpPool;
import com.panamera.proxy.protocol.http.HttpClientUtil;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public final class HttpClientForwardStrategy extends AbstractForwardImpl implements ForwardStrategy {

	@Override
	public String normalForward() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FullHttpResponse proxyForward(FullHttpRequest request) {
		// TODO Auto-generated method stub
		String[] proxyAddress = DefaultIpPool.getInstance().nextIp().split(":");
		CloseableHttpClient httpClient = HttpClientUtil.getHttpClient(true, proxyAddress[0], Integer.valueOf(proxyAddress[1]), "", "", 1000);
		
//		String url = request.uri();
//		String param = null;
//		// 分离uri 和参数
//		String paramSplitRes[] = url.split("\\?");
//		if (paramSplitRes.length >= 2) {
//			url = paramSplitRes[0];
//			param = paramSplitRes[1];
//		}
		System.out.println("request.method:" + request.method());
		System.out.println("request.uri:" + request.uri());
		System.out.println("request.head :" + request.headers());
//		System.out.println("param :" + param);
//		System.out.println("Remote Address:" + ctx.channel().remoteAddress());
		
//		HttpRequestBase tempRequest = null;
		CloseableHttpResponse tempResponse = null;
		try {
			String method = request.method().name();
			String content_type = request.headers().get("content-type") != null ? request.headers().get("content-type") : "text/HTML;charset:utf-8";
			String targetUrl = request.headers().get("target");// "http://ip111.cn"
			if ("Get".equalsIgnoreCase(method)) {// multipart/form-data
				HttpGet tempRequest = new HttpGet(targetUrl);
				if ("application/x-www-form-urlencoded".equalsIgnoreCase(content_type)) {

				} else {

				}
				tempResponse = httpClient.execute(tempRequest);

			} else if ("Post".equalsIgnoreCase(method)) {
				HttpPost tempRequest = new HttpPost(targetUrl);
				String data = request.content().toString(io.netty.util.CharsetUtil.UTF_8);
				if ("application/json".equalsIgnoreCase(content_type)) {
					tempRequest.addHeader("Content-Type", "application/json;charset=UTF-8");
					tempRequest.setEntity(new StringEntity(data));
				} else if ("application/x-www-form-urlencoded".equalsIgnoreCase(content_type)) {
					List<NameValuePair> formparams = new ArrayList<NameValuePair>();
					for (int i = 0; i < data.split("&").length; i++) {
						String kv = data.split("&")[i];
						formparams.add(new BasicNameValuePair(kv.split("=")[0], kv.split("=")[1]));
					}
					tempRequest.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
				}
				tempResponse = httpClient.execute(tempRequest);

			} else {

			}
//			tempResponse = httpClient.execute(tempRequest);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		FullHttpResponse fullResponse = null;
		try {
			String version = tempResponse.getProtocolVersion().toString();
			int status = tempResponse.getStatusLine().getStatusCode();
			fullResponse = buildHttpResponse(version, status, EntityUtils.toString(tempResponse.getEntity(), "UTF-8"));
			return fullResponse.copy();
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		} finally {
			fullResponse.release();
			try {
//				tempRequest.releaseConnection();
				tempResponse.close();
//				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return fullResponse;
		
//		if (200 == response.getStatusLine().getStatusCode()) {
//			try {
//				return EntityUtils.toString(response.getEntity(), "UTF-8");
//			} catch (ParseException | IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			return null;
//		}
	}

}
