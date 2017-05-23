package io.socket.nativeclient;

import java.util.Objects;

/**
 * @作者 mitkey
 * @时间 2017年5月22日 下午2:52:52
 * @类说明 IO.java <br/>
 * @版本 0.0.1
 */
public final class IO {

	private IO() {
	}

	public static SocketClient socket(String host, int port, OnSocketCall onCall) {
		return socket(host, port, new Options(), onCall);
	}

	public static SocketClient socket(String host, int port, Options opts, OnSocketCall onCall) {
		Objects.requireNonNull(host);
		Objects.requireNonNull(port);
		Objects.requireNonNull(opts);
		Objects.requireNonNull(onCall);
		return new SocketClient(host, port, opts, onCall);
	}

	public static class Options {

		/** 连接超时单位毫秒 */
		public int connectTimeout = 5000;

		/**
		 * 性能偏好由三个整数描述，它们的值分别指示短连接时间、低延迟和高带宽的相对重要性。<br/>
		 * 这些整数的绝对值没有意义；为了选择协议，需要简单比较它们的值，较大的值指示更强的偏好。<br/>
		 * 负值表示的优先级低于正值。<br/>
		 * 
		 * 如果应用程序相对于低延迟和高带宽更偏好短连接时间，则其可以使用值 (1, 0, 0) 调用此方法。<br/>
		 * 如果应用程序相对于低延迟更偏好高带宽，而相对于短连接时间更偏好低延迟，则其可以使用值 (0, 1, 2) 调用此方法。
		 * 
		 * 在连接套接字后调用此方法无效。
		 */

		/** 表达短连接时间的相对重要性的 int */
		public int performancePrefConnectionTime = 0;
		/** 表达低延迟的相对重要性的 int */
		public int performancePrefLatency = 1;
		/** 表达高带宽的相对重要性的 int */
		public int performancePrefBandwidth = 0;

		/**
		 * 推荐使用以下值。值必须 0 <= trafficClass <= 255，否则将抛出 IllegalArgumentException。
		 * <ul>
		 * <li>IPTOS_LOWCOST (0x02) - cheap!
		 * <li>IPTOS_RELIABILITY (0x04) - reliable connection with little package loss.
		 * <li>IPTOS_THROUGHPUT (0x08) - lots of data being sent.
		 * <li>IPTOS_LOWDELAY (0x10) - low delay.
		 * </ul>
		 */
		public int trafficClass = 0x14; // low delay + reliable

		/** 为 true 表示底层的TCP实现会监视该连接是否有效。为false，表示TCP不会监视连接是否有效，不活动的客户端可能会永久存在下去，而不会注意到服务器已经崩溃 */
		public boolean keepAlive = true;
		/** 是否立即发送消息 */
		public boolean tcpNoDelay = true;

		/** socket 用于输出数据的缓冲区的大小 */
		public int sendBufferSize = 4096;
		/** socket 用于输入数据的缓冲区的大小 */
		public int receiveBufferSize = 4096;

		/**
		 * 用来控制Socket关闭时的行为，关闭时是否逗留 <br/>
		 * 若为 false，执行Socket的close()方法时，该方法会立即返回，但底层的Socket也会立即关闭，所有未发送完的剩余数据被丢弃<br/>
		 * 若为 true，执行Socket的close()方法时，该方法不会立即返回，而进入阻塞状态，同时，底层的Socket会尝试发送剩余的数据<br/>
		 */
		public boolean linger = false;
		/** socket 关闭时逗留的时间，单位秒. 仅当 linger 为 true 时该值才有效 */
		public int lingerDuration = 0;

		/** 当通过Socket的输入流读数据时，如果还没有数据，就会等待。Socket类的SO_TIMEOUT选项用于设定接收数据的等待超时时间，单位为毫秒，它的默认值为0，表示会无限等待，永远不会超时。 */
		public int socketTimeout = 0;

		@Override
		public String toString() {
			return "Options [connectTimeout=" + connectTimeout + ", performancePrefConnectionTime=" + performancePrefConnectionTime + ", performancePrefLatency="
					+ performancePrefLatency + ", performancePrefBandwidth=" + performancePrefBandwidth + ", trafficClass=" + trafficClass + ", keepAlive=" + keepAlive
					+ ", tcpNoDelay=" + tcpNoDelay + ", sendBufferSize=" + sendBufferSize + ", receiveBufferSize=" + receiveBufferSize + ", linger=" + linger + ", lingerDuration="
					+ lingerDuration + ", socketTimeout=" + socketTimeout + "]";
		}

	}

}
