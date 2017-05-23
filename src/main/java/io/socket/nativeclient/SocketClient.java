package io.socket.nativeclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.nativeclient.IO.Options;

/**
 * @作者 mitkey
 * @时间 2017年5月22日 下午3:17:24
 * @类说明 SocketClient.java <br/>
 * @版本 0.0.1
 */
public class SocketClient {

	private ExecutorService executorService = Executors.newCachedThreadPool();
	private Timer timer = new Timer();

	private ReplyPollThread pollThread;
	/** ReplyPollThread 是否堵塞 */
	private boolean blocked;

	/** 是否已连接的 */
	private boolean connected;

	private final OnSocketCall onCall;

	private Socket socket;
	private BufferedOutputStream bos;
	private BufferedInputStream bis;

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

			bos = new BufferedOutputStream(socket.getOutputStream());
			bis = new BufferedInputStream(socket.getInputStream());
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

	public void sendData(byte[] data) {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
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

		// 通知回调
		onCall.onConnect();

		// 开启定时任务用于检测 socket 是否已断开
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					socket.sendUrgentData(0xff);
					if (!connected && pollThread == null) {
						// 开启服务器消息回复线程处理
						pollThread = new ReplyPollThread();
						pollThread.start();
					}
					connected = true;
				} catch (IOException e) {
					cancel();
					transportDisconnected();
				}
			}
		}, 0, 1000);
	}

	private void transportData(byte[] respData) {
		onCall.onMessage(respData);
	}

	private void transportDisconnected() {
		// 角标更新为 false
		connected = false;

		// 关闭任务执行器
		executorService.shutdownNow();

		// 通知回调
		onCall.onDisconnect();

		// 取消计时器
		timer.cancel();

		// 终止轮询线程
		if (pollThread != null) {
			pollThread.interrupt();
		}

		// 断开 socket
		if (bos != null) {
			try {
				bos.close();
				bos = null;
			} catch (IOException e) {
				transportError(e);
			}
		}
		if (bis != null) {
			try {
				bis.close();
				bis = null;
			} catch (IOException e) {
				transportError(e);
			}
		}
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
		transportDisconnected();
	}

	private class ReplyPollThread extends Thread {
		public ReplyPollThread() {
			super("Reply Poll Thread.");
		}

		@Override
		public void run() {
			super.run();
			while (isConnected()) {
				try {
					blocked = true;
					byte[] bs = StreamUtils.copyToByteArray(bis);
					if (bs != null && bs.length != 0) {
						transportData(bs);
					}
					blocked = false;
				} catch (IOException e) {
					if (!interrupted()) {
						transportError(e);
						return;
					}
				}
				try {
					sleep(50);
				} catch (InterruptedException e) {
				}
			}
			transportDisconnected();
		}
	}

}