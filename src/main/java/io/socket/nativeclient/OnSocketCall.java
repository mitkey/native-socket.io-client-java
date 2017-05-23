package io.socket.nativeclient;

/**
 * @作者 mitkey
 * @时间 2017年5月22日 下午3:42:59
 * @类说明 OnSocketCall.java <br/>
 * @版本 0.0.1
 */
public interface OnSocketCall {

	void onDisconnect();

	void onConnect();

	void onMessage(byte[] data);

	void onError(SocketIOException socketIOException);

}
