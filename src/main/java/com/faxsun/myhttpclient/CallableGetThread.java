package com.faxsun.myhttpclient;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class CallableGetThread implements Callable<int[]> {

	private final CloseableHttpClient httpClient;
	private final HttpContext context;
	private final HttpGet httpget;
	private final int id;
	private final CountDownLatch latch;

	public CallableGetThread(CloseableHttpClient httpClient, HttpGet httpget,
			int id, CountDownLatch latch) {
		this.httpClient = httpClient;
		this.context = new BasicHttpContext();
		this.httpget = httpget;
		this.id = id;
		this.latch = latch;
	}

	/**
	 * @return Array of 2 Integers. The 1st element is the id, and the 2nd is
	 *         the length.
	 */
	@Override
	public int[] call() {
		try {
			System.out.println(id + " - about to get something from "
					+ httpget.getURI());
			CloseableHttpResponse response = httpClient.execute(httpget,
					context);
			try {
				System.out.println(id + " - get executed");
				// get the response body as an array of bytes
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					byte[] bytes = EntityUtils.toByteArray(entity);

					// Return the length instead of print it.
					return new int[] { id, bytes.length };
					// reslength = bytes.length;
					// System.out.println(id + " - " + reslength +
					// " bytes read");
				}
			} finally {
				latch.countDown();
				response.close();
			}
		} catch (Exception e) {
			System.out.println(id + " - error: " + e);
		}
		return new int[] { 0, 0 };
	}

}
