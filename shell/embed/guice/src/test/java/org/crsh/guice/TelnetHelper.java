package org.crsh.guice;

import java.io.IOException;
import java.io.InputStream;

public class TelnetHelper {

	public static String readUntil(String pattern, InputStream in) throws IOException {
		char lastChar = pattern.charAt(pattern.length() - 1);
		StringBuffer sb = new StringBuffer();
		char ch = (char) in.read();
		while (true) {
			System.out.print(ch);
			sb.append(ch);
			if (ch == lastChar) {
				if (sb.toString().endsWith(pattern)) {
					return sb.toString();
				}
			}
			ch = (char) in.read();
		}
	}
	
}
