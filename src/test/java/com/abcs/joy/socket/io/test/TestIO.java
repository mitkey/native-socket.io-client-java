package com.abcs.joy.socket.io.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.socket.nativeclient.IO;
import io.socket.nativeclient.OnSocketCall;
import io.socket.nativeclient.SocketClient;
import io.socket.nativeclient.SocketIOException;

/**
 * @作者 mitkey
 * @时间 2017年5月23日 下午3:21:13
 * @类说明 TestIO.java <br/>
 * @版本 0.0.1
 */
public class TestIO {

	private SocketClient socket;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		String host = "localhost";
		int port = 80;
		socket = IO.socket(host, port, new OnSocketCall() {

			@Override
			public void onMessage(byte[] data) {
				System.out.println("TestIO.setUp().new OnSocketCall() {...}.onMessage()");
			}

			@Override
			public void onError(SocketIOException socketIOException) {
				System.out.println("TestIO.setUp().new OnSocketCall() {...}.onError()");
			}

			@Override
			public void onDisconnect() {
				System.out.println("TestIO.setUp().new OnSocketCall() {...}.onDisconnect()");
			}

			@Override
			public void onConnect() {
				System.out.println("TestIO.setUp().new OnSocketCall() {...}.onConnect()");
			}
		});
	}

	@After
	public void tearDown() throws Exception {
		socket.dispose();
	}

	@Test
	public void test() {
		System.err.println(socket.isBlocked());
		System.err.println(socket.isConnected());
	}

}
