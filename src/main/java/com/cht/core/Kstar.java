package com.cht.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.cht.io.SerialPort;

public class Kstar {

	static final Logger LOG = Logger.getLogger(Kstar.class);
	
	private int soi = 0xAAAA;
	private int eoi = 0x0A0D;
	private int addr = 0x01;    // 站號改這裡
	private int order = 0x00;
	private int kind = 0x0c;
	private int length = 0x0000;
	private int cks = 0;
	
	public Kstar(){
	}
	
	public Kstar(int addr){
		this.addr = addr;
	}
	
	public void Polling(String port, int baudrate, int databits, int stopbits, int parity, int flowcontrol, long timeout) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		dos.writeShort(soi);
		dos.write(addr);
		dos.write(order);
		dos.write(kind);
		dos.writeShort(length);

		byte[] bytes = baos.toByteArray();
		for (byte c : bytes) {
			cks += c & 0xff;
		}
		
		dos.writeShort(cks);
		dos.writeShort(eoi);

		byte[] query = baos.toByteArray();
		
		String p = "";
		for (byte d : query) {
			p += String.format("%02x ", d);
		}
		
		LOG.info(p);

		SerialPort sp = new SerialPort(port, baudrate, databits, stopbits, parity, flowcontrol);		
		try {
			sp.setTimeout(timeout);

			OutputStream os = sp.getOutputStream();
			InputStream is = new BufferedInputStream(sp.getInputStream());

			os.write(query);
			os.flush();

			for (;;) {
				int b;
				int count = 1;
				while ((b = is.read()) != -1) {
					LOG.info(String.format("%02d\t%02X", count, b));
					count++;
				}		
			}
		}catch (IOException e){
			LOG.info(e.toString());		
		}finally {
			sp.close();
		}
	}
}
