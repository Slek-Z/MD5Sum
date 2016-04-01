/* Compute MD5 checksums of files.
   Copyright (C) 2016 Slek.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* Version 0.1, 01/04/2016.  */

package md5sum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Sum {

	private final static int PAGE_SIZE = 0x1000;

	private final static String AUTHOR = "By Slek";
	private final static String USAGE = "Usage: md5sum [OPTION] [FILE]";
	private final static String VERSION = "Version 0.1 (01-04-2016)";

	public static void main(String[] args) {
		try {
			char opt;
			PrintStream out = System.out;
			File file = null;
			String original = null;
			if (args.length > 0) {
				for (int i = 0; i < args.length; i++) { // While?
					if ((args[i].charAt(0) == '-') && (args[i].length() == 2)) {
						opt = Character.toLowerCase(args[i].charAt(1));
						switch (opt) {
						case 'c':
							original = args[++i];
							if (!isValidHash(original))
								throw new MD5SumException("Invalid hash");
							break;
						case 'o':
							out = new PrintStream(new FileOutputStream(args[++i], true));
							break;
						case '?':
						case 'h':
							if (args.length == 1)
								usage();
							break;
						default:
							throw new MD5SumException("Invalid option", "Use -h for help");
						}
					} else if (i == (args.length - 1))
						file = new File(args[i]);
					else
						throw new MD5SumException("Invalid option", "Use -h for help");
				}
				if (file != null) {
					MessageDigest md5 = MessageDigest.getInstance("MD5");
					byte[] hash = digest_file(file, md5);
					String digest = toHexString(hash);
					if (original != null) {
						if (original.equalsIgnoreCase(digest))
							System.out.println("OK");
						else
							System.out.println("FAILED");
					} else
						out.println(digest); // TODO: file.getName();
				}
				if (out != System.out)
					out.close();
			} else
				usage();
		} catch (MD5SumException e) {
			System.err.println(e.getMessage());
			if (e.hasHint())
				System.out.println(e.getHint());
		} catch (ArrayIndexOutOfBoundsException e) {
			usage();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Fatal: MD5 algorithm not supported");
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			// TODO: default behavior
			e.printStackTrace();
		}
	}

	public static void usage() {
		System.out.println(USAGE);
		System.out.println("Print or check MD5 checksums.");
		System.out.println();
		System.out.println("Options:");
		System.out.println("    -c [hash]   Check against hash");
		System.out.println("    -o [file]   Set output file");
		System.out.println("    -h          Print this message");
		System.out.println();
		System.out.println(AUTHOR);
		System.out.println(VERSION);
	}

	public static byte[] digest_file(File file, MessageDigest md) throws IOException {
		FileInputStream in = new FileInputStream(file);
		byte[] buffer = new byte[PAGE_SIZE];
		int n;
		while ((n = in.read(buffer)) >= 0)
			md.update(buffer, 0, n);
		in.close();
		return md.digest();
	}

	public static String toHexString(byte[] b) {
		char[] toHexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] hex = new char[b.length * 2];
		int v;
		for (int i = 0; i < b.length; i++) {
			v = b[i] & 0xFF;
			hex[i * 2] = toHexChar[v >> 4];
			hex[i * 2 + 1] = toHexChar[v & 0xF];
		}
		return new String(hex);
	}

	public static boolean isValidHash(String hash) {
		if (hash.length() != 32)
			return false;
		char ch;
		boolean ret = true;
		int i = 0;
		while (ret && (i < hash.length())) {
			ch = Character.toLowerCase(hash.charAt(i++));
			if (!Character.isDigit(ch) && (ch < 'a') || (ch > 'f'))
				ret = false;
		}
		return ret;
	}
	
	private static class MD5SumException extends RuntimeException {

		private static final long serialVersionUID = -3360197226792464007L;
		private String hint = null;

		public MD5SumException(String msg) {
			super(msg);
		}

		public MD5SumException(String msg, String hint) {
			this(msg);
			this.hint = hint;
		}

		public String getHint() {
			return hint;
		}

		public boolean hasHint() {
			return (hint != null);
		}

	}
}