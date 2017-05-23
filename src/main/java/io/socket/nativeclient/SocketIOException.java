package io.socket.nativeclient;

/**
 * @作者 mitkey
 * @时间 2017年5月22日 下午3:28:52
 * @类说明 SocketIOException.java <br/>
 * @版本 0.0.1
 */
public class SocketIOException extends RuntimeException {

	private static final long serialVersionUID = -1743432941676839396L;

	SocketIOException(String message, Throwable cause) {
		super(message, cause);
	}

	SocketIOException(String message) {
		super(message);
	}

	SocketIOException(Throwable cause) {
		super(cause);
	}

}
