package io.socket.nativeclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @作者 mitkey
 * @时间 2017年5月22日 下午4:22:44
 * @类说明 StreamUtils.java <br/>
 * @版本 0.0.1
 */
public final class StreamUtils {
	private static final int DEFAULT_BUFFER_SIZE = 4096;

	private StreamUtils() {
	}

	public static byte[] copyToByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
		copy(in, out);
		return out.toByteArray();
	}

	public static int copy(InputStream in, OutputStream out) throws IOException {
		int byteCount = 0;
		byte buffer[] = new byte[4096];
		for (int bytesRead = -1; (bytesRead = in.read(buffer)) != -1;) {
			out.write(buffer, 0, bytesRead);
			byteCount += bytesRead;
		}
		out.flush();
		return byteCount;
	}

}
