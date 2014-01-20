package net.sabamiso.processing.p5_gphoto2_control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import processing.core.PApplet;
import processing.core.PImage;

class ReadThread extends Thread {
	InputStream is;
	
	public ReadThread(InputStream is) {
		this.is = is;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				is.read();
			}
			catch(Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
}

public class GPhoto2Control {
	PApplet papplet;
	Process process;
	OutputStream os;
	BufferedWriter stdin;
	InputStream is;
	
	ReadThread thread;
	
	String cmd = "/usr/bin/gphoto2";
	String cmd_local = "/usr/local/bin/gphoto2";

	String opt = "--shell --force-overwrite --filename=";
	String tmp_filename = "/tmp/_gphoto2_image.jpg";
	
	public GPhoto2Control(PApplet papplet) {
		this.papplet = papplet;
	}

	public synchronized boolean connect() {
		boolean rv;
		
		rv = exec(cmd_local + " " +  opt + tmp_filename);
		if (rv == true) return true;
		
		rv = exec(cmd + " " +  opt + tmp_filename);
		if (rv == true) return true;
		
		return false;
	}

	public synchronized  PImage takePicture() {
		new File(tmp_filename).delete();
		
		write("capture-image-and-download");
		
		while(true) {
			sleep(100);
			if (new File(tmp_filename).exists() == true) break;
		}
		sleep(500);
				
		PImage img = papplet.loadImage(tmp_filename);
		
		new File(tmp_filename).delete();

		return img;
	}

	public synchronized void close() {
		if (process == null) return;
		
		write("exit");
		try {
			stdin.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		stdin = null;
		
		try {
			is.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		is = null;
		
		try {
			os.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		os = null;
		
		try {
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			process.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		process = null;
	}

	protected boolean exec(String cmd) {
		try {
			System.out.println("exec() : cmd=" + cmd);
			process = Runtime.getRuntime().exec(cmd);
			
			os = process.getOutputStream();
			stdin = new BufferedWriter(new OutputStreamWriter(os));
			is = process.getErrorStream();
			
			thread = new ReadThread(is);
			thread.start();
			
		} catch (IOException e) {
			System.err.println("exec() : exec failed...cmd=" + cmd);
			e.printStackTrace();
			close();
			return false;
		}
		return true;
	}
	
	protected void write(String cmd) {
		if (process == null) return;
		
		try {
			// write pipe
			stdin.write(cmd + "\r\n");
			stdin.flush();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getTmpFilename() {
		return tmp_filename;
	}

	public void setTmpFilename(String tmp_filename) {
		this.tmp_filename = tmp_filename;
	}
}
