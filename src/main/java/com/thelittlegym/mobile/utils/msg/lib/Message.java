package com.thelittlegym.mobile.utils.msg.lib;

import com.thelittlegym.mobile.utils.msg.config.AppConfig;
import com.thelittlegym.mobile.utils.msg.lib.base.ISender;
import com.thelittlegym.mobile.utils.msg.lib.base.Sender;

import java.util.Map;

/**
 * A Sender class define the message mode to send HTTP request.
 * 
 * @see ISender
 * @see Sender
 * @version 1.0 at 2014/10/28
 * */
public class Message extends Sender {

	private static final String API_SEND = "http://api.submail.cn/message/send.json";
	private static final String API_XSEND = "http://api.submail.cn/message/xsend.json";
	private static final String API_SUBSCRIBE = "http://api.submail.cn/addressbook/message/subscribe.json";
	private static final String API_UNSUBSCRIBE = "http://api.submail.cn/addressbook/message/unsubscribe.json";

	public Message(AppConfig config) {
		this.config = config;
	}

	/**
	 * Send request data to server.The data consists of two parts. One of them
	 * is original data,and another is signature.For example,the original data
	 * is
	 * <p>
	 * address=jam@submail.cn?name=jam
	 * </p>
	 * .Then,the final request data is
	 * <p>
	 * address=jam@submail.cn?name=jam?appid=xxx?appkey=xxx?timestamp=xxx?signature=xxx?
	 * <p>
	 * The algorithm of signature is {@link #createSignature(String)}
	 * @see RequestSigner#getSignature()
	 * @param data
	 *            is the original data only contains content.
	 * */
	@Override
	public boolean send(Map<String, Object> data) {
		return request(API_SEND, data);
	}

	@Override
	public boolean xsend(Map<String, Object> data) {
		return request(API_XSEND, data);
	}

	@Override
	public boolean subscribe(Map<String, Object> data) {
		// TODO Auto-generated method stub
		return request(API_SUBSCRIBE, data);
	}

	@Override
	public boolean unsubscribe(Map<String, Object> data) {
		// TODO Auto-generated method stub
		return request(API_UNSUBSCRIBE, data);
	}
}
