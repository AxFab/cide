package core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import core.type.NetworkBody;
import core.type.NetworkHeader;
import core.type.NetworkMessage;

public class Execution {
	String path;
	String[] args;
	Process proc;

	NetworkHeader widgetHeader; 
	OutputStream stdin;
	
	/**
	 * Backend for the Execution widget
	 * @param path Path of the executable
	 * @param args Arguments to pass to executable
	 * @param h Header describing which widget to send to
	 * @throws IOException Unable to launch the executable
	 */
	public Execution(String path, String[] args, NetworkHeader h) throws IOException {
		this.path = path;
		this.widgetHeader = h;

		// Launch process
		List<String> cmdarray = new ArrayList<String>();

		cmdarray.add(path);
		for (String arg : args) {
			cmdarray.add(arg);
		}

		ProcessBuilder pb = new ProcessBuilder(cmdarray);
		proc = pb.start();

		// Setup listeners
		// stdout
		new Thread(new Runnable() {
			public void run() {
				InputStream out = proc.getInputStream();

				byte[] buffer = new byte[0x2000];

				try {
					while (out.read(buffer) != -1) {
						sendStdout(new String(buffer, "UTF-8"));
					}
				} catch (IOException e) {
					
				}
			}
		}).start();
		// stderr
		new Thread(new Runnable() {
			public void run() {
				InputStream err = proc.getErrorStream();

				byte[] buffer = new byte[0x2000];

				try {
					while (err.read(buffer) != -1) {
						sendStderr(new String(buffer, "UTF-8"));
					}
				} catch (IOException e) {
					
				}
			}
		}).start();
		// stdin
		this.stdin = proc.getOutputStream();
	}

	private void sendStdout(String out) {
		NetworkBody body = new NetworkBody("stdout", out);
		NetworkMessage msg = new NetworkMessage(this.widgetHeader, body);
		ConnectionManager.sendMessage(msg);
	}

	private void sendStderr(String err) {
		NetworkBody body = new NetworkBody("stderr", err);
		NetworkMessage msg = new NetworkMessage(this.widgetHeader, body);
		ConnectionManager.sendMessage(msg);
	}
	
	public void sendStdin (String in) throws IOException {
		try {
			this.stdin.write(in.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			play.Logger.fatal("Charset UTF-8 not found");
			play.Play.fatalServerErrorOccurred();
		}
	}
}
