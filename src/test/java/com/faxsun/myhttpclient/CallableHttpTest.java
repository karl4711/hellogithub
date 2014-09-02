package com.faxsun.myhttpclient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Assert;
import org.junit.Test;

public class CallableHttpTest {
	private String[] urisToGet = { "http://hc.apache.org/",
			"http://hc.apache.org/httpcomponents-core-ga/",
			"http://hc.apache.org/httpcomponents-client-ga/", };

	private long[] byteLengthsToGet = { 15203, 13614, 17839 };

	@Test
	public void testCallableThreaded() throws Exception {
		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(100);

		CloseableHttpClient httpclient = HttpClients.custom()
				.setConnectionManager(cm).build();

		// Use a latch for 3 threads.
		CountDownLatch latch = new CountDownLatch(3);

		// Use ExecutorService to execute callable threads.
		ExecutorService exec = Executors.newCachedThreadPool();

		try {
			// create a thread for each URI
			CallableGetThread[] threads = new CallableGetThread[urisToGet.length];

			List<Future<int[]>> futures = new ArrayList<Future<int[]>>();
			for (int i = 0; i < threads.length; i++) {
				HttpGet httpget = new HttpGet(urisToGet[i]);
				threads[i] = new CallableGetThread(httpclient, httpget, i + 1,
						latch);
			}

			// Execute callable threads, and put the results into Future List.
			for (int j = 0; j < threads.length; j++) {
				futures.add(exec.submit(threads[j]));

			}

			// Use await instead of join.
			latch.await();

			int id;
			int length;
			for (Future<int[]> future : futures) {
				if (future.isDone()) {
					id = future.get()[0];
					length = future.get()[1];
					System.out.println(id + " - " + length + " bytes read");
					Assert.assertEquals(length, byteLengthsToGet[id - 1]);
				}
			}

		} finally {
			httpclient.close();
		}
	}
}
