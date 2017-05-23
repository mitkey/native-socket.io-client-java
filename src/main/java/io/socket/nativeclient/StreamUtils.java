package io.socket.nativeclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
		if (!(in instanceof BufferedInputStream)) {
			in = new BufferedInputStream(in);
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(32, in.available()))) {
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}
			return baos.toByteArray();
		}
	}

	/** A ByteArrayOutputStream which avoids copying of the byte array if possible. */
	static public class OptimizedByteArrayOutputStream extends ByteArrayOutputStream {
		public OptimizedByteArrayOutputStream(int initialSize) {
			super(initialSize);
		}

		@Override
		public synchronized byte[] toByteArray() {
			if (count == buf.length)
				return buf;
			return super.toByteArray();
		}
	}

}
