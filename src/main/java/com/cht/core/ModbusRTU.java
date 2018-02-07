package com.cht.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.cht.io.SerialPort;

public class ModbusRTU {
	static final Logger LOG = Logger.getLogger(ModbusRTU.class);
	
	private String serialPort;
	private int baudrate;
	private int databits;
	private int stopbits;
	private int parity;
	private int flowcontrol;
	
	public ModbusRTU(){
	}
	
	public ModbusRTU(String serialPort, int baudrate, int databits, int stopbits, int parity, int flowcontrol){
		this.serialPort = serialPort;
		this.baudrate = baudrate;
		this.databits = databits;
		this.stopbits = stopbits;
		this.parity = parity;
		this.flowcontrol = flowcontrol;
	}
	
	public void Polling(int address, int reference, int register, int count, long timeout) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		dos.write(address);
		dos.write(register);
		dos.writeShort(reference);
		dos.writeShort(count);
		
		byte[] query = baos.toByteArray();
		int crc [] = Utils.crc(query);
		
		dos.write(crc[0]);
		dos.write(crc[1]);
		
		query = baos.toByteArray();
		
		String p = "";
		for (byte d : query) {
			p += String.format("%02X ", d);
		}
		
		LOG.info(p);
		
		try {
			SerialPort sp = new SerialPort(serialPort, baudrate, databits, stopbits, parity, flowcontrol);
			sp.setTimeout(timeout);

			OutputStream os = sp.getOutputStream();
			InputStream is = new BufferedInputStream(sp.getInputStream());
			DataInputStream dis = new DataInputStream(is);
			
			os.write(query);
			os.flush();
			
			//Slave
			if(dis.read() != address)
				throw new IOException("Slave id is incorrect");
			if(dis.read() != register)
				throw new IOException("function code is incorrect");
			//Length
			int c  = dis.read() / 2;
			for(int i = 1; i <= c; i++)
				System.out.println("[" + (reference + i) + "]: " + dis.readShort());
			
			int crc_h = dis.read();
			int crc_l = dis.read();
			//System.out.printf("%02X %02X", crc_h, crc_l);
		}catch(Exception e){
			LOG.error(e.toString());
		}
	}
}
