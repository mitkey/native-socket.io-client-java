package io.socket.nativeclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.socket.nativeclient.IO.Options;

/**
 * @作者 mitkey
 * @时间 2017年5月22日 下午3:17:24
 * @类说明 SocketClient.java <br/>
 * @版本 0.0.1
 */
public class SocketClient {

	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

	/** ReplyPollThread 是否堵塞 */
	private boolean blocked;

	/** 是否已连接的 */
	private boolean connected;

	private final OnSocketCall onCall;

	private Socket socket;
	private BufferedOutputStream bos;

	SocketClient(String host, int port, Options opts, OnSocketCall onCall) {
		System.out.println(String.format("start connect to host[%s] port[%s] .....", host, port));

		this.onCall = onCall;
		this.socket = new Socket();

		try {
			// 在 socket 调用 connect 之前调用
			socket.setPerformancePreferences(opts.performancePrefConnectionTime, opts.performancePrefLatency, opts.performancePrefBandwidth);
			socket.setTrafficClass(opts.trafficClass);
			socket.setTcpNoDelay(opts.tcpNoDelay);
			socket.setKeepAlive(opts.keepAlive);
			socket.setSendBufferSize(opts.sendBufferSize);
			socket.setReceiveBufferSize(opts.receiveBufferSize);
			socket.setSoLinger(opts.linger, opts.lingerDuration);
			socket.setSoTimeout(opts.socketTimeout);
			// 为了确保一个进程关闭了Socket后，即使它还没释放端口，同一个主机上的其他进程还可以立刻重用该端口
			socket.setReuseAddress(true);

			// 连接协议处理
			connectTransport(new InetSocketAddress(host, port), opts);
		} catch (Exception e) {
			transportError(e);
		}
	}

	public void dispose() {
		transportDisconnected();
	}

	public boolean isBlocked() {
		return blocked;
	}

	public boolean isConnected() {
		return connected;
	}

	public void sendData(final byte[] data) {
		scheduledExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					if (bos == null) {
						bos = new BufferedOutputStream(socket.getOutputStream());
					}
					bos.write(data);
					bos.flush();
				} catch (IOException e) {
					transportError(e);
				}
			}
		});
	}

	/*
	 * ======================================
	 */

	private synchronized void connectTransport(InetSocketAddress socketAddress, Options opts) throws IOException {
		// 在建立连接或者发生错误之前，连接一直处于阻塞状态。
		socket.connect(socketAddress, opts.connectTimeout);

		// 通知连接成功回调
		onCall.onConnect();

		// 角标更新为 true
		connected = true;

		// 开启任务线程处理接收服务器消息
		openTaskReceiveData();

		// 开启定时任务用于检测 socket 是否已断开
		openTaskCheckStatus();
	}

	private void openTaskCheckStatus() {
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					socket.sendUrgentData(0xff);
				} catch (IOException e) {
					transportDisconnected();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	private void openTaskReceiveData() {
		if (!isConnected()) {
			return;
		}

		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			private BufferedInputStream bis;

			@Override
			public void run() {
				if (isConnected()) {
					try {
						if (bis == null) {
							bis = new BufferedInputStream(socket.getInputStream());
						}

						blocked = true;

						// ”一次性“从输入流中读完
						int available = bis.available();
						if (available != 0) {
							byte[] buffer = new byte[available];
							int bytesRead = bis.read(buffer);
							// 不能使用 != -1 来判断是否读到完，因为 socket 的输入流只有 socket 断开时才会返回 -1
							if (bytesRead > 0) {
								transportData(buffer);
							}
						}

						blocked = false;
					} catch (Exception e) {
						transportError(e);
					}
				}
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
	}

	private void transportData(byte[] respData) {
		onCall.onMessage(respData);
	}

	private void transportDisconnected() {
		// 角标更新为 false
		connected = false;

		// 关闭任务执行器
		scheduledExecutorService.shutdownNow();

		// 通知回调
		onCall.onDisconnect();

		// 断开 socket
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (Exception e) {
				transportError(e);
			}
		}
	}

	private void transportError(Exception error) {
		onCall.onError(new SocketIOException(error));
	}

}
