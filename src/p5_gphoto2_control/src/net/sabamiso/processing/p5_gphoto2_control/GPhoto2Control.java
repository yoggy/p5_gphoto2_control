package net.sabamiso.processing.p5_gphoto2_control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import processing.core.PApplet;
import processing.core.PImage;

public class GPhoto2Control {
	PApplet papplet;
	Process process;
	OutputStream os;
	BufferedWriter stdin;
	
	String cmd = "/usr/local/bin/gphoto2 --shell --force-overwrite --filename=";
	String tmp_filename = "/tmp/_gphoto2_image.jpg";
	
	public GPhoto2Control(PApplet papplet) {
		this.papplet = papplet;
	}

	public synchronized boolean connect() {
		return exec(cmd + tmp_filename);
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
