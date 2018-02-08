package com.cht.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.cht.io.SerialPort;

public class Motech {

	static final Logger LOG = Logger.getLogger(Motech.class);
	
	private final int START_BYTE = 0x0A;
	private final int STOP_BYTE = 0x0D;
	private final int FUNCTION = 3;
	
	private OutputStream os;
	private DataInputStream is;
	private int start;
	private int end;
	
	public Motech(){
	}
	
	public Motech(int start, int end){
		this.start = start;
		this.end = end;
	}
	
	public void Polling(ModbusProtocol mp) throws Exception {
		SerialPort sp = new SerialPort(mp.getSerialPort(), mp.getBaudrate(), mp.getDatabits(), mp.getStopbits(), mp.getParity(), mp.getFlowcontrol());
		sp.setTimeout(mp.getTimeout());
		
		os = new BufferedOutputStream(sp.getOutputStream());
		is = new DataInputStream(new BufferedInputStream(sp.getInputStream()));
		
		int slave = 0;
		for (int i = start; i <= end; i++){
			try {
				slave = i;
				LOG.info("Now scanning slave id: "+ slave + "...");
				readHoldingRegisters(slave, 181, 1);
				LOG.info("Oh yeah!  Slave id " + slave + " is alive! <3");
			} catch (Exception e) {
				//LOG.error(e.getMessage(), e);
				LOG.info("You can't see me! Slave id " + slave + " is died!");	
			}
			Thread.sleep(3000);
		}
		sp.close();
	}
	
	public void readHoldingRegisters(int slave, int startAddress, int quantity) throws IOException {
		byte[] body = encodeReadingRequest(slave, startAddress, quantity);

		os.write(body);
		os.flush();

		/*int b;
		int count = 1;
		while ((b = is.read()) != -1) {
			LOG.info(String.format("%02d\t%02X", count, b));
			count++;
		}*/
		decodeReadingResponse(is, slave);
	}
	
	public byte[] encodeReadingRequest(int slave, int startAdd, int quantity) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] body = encodeReadingBody(slave, startAdd, quantity);
		int crc = CRC16(body);

		baos.write(START_BYTE);
		baos.write(body);
		baos.write(crc & 0x0FF);
		baos.write((crc >> 8) & 0x0FF);
		baos.write(STOP_BYTE);

		baos.flush();

		return baos.toByteArray();
	}
	
	public byte[] encodeReadingBody(int slave, int startAdd, int quantity) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		baos.write(slave);
		baos.write(FUNCTION);
		baos.write((startAdd >> 8) & 0x0FF);
		baos.write(startAdd & 0x0FF);
		baos.write((quantity >> 8) & 0x0FF);
		baos.write(quantity & 0x0FF);

		baos.flush();

		return baos.toByteArray();
	}
	
	public byte[] decodeReadingResponse(DataInputStream is, int slave) throws IOException {
		
		if (read(is) != START_BYTE) {
			throw new IOException("Start byte is incorrect");
		}

		if (read(is) != slave) {
			throw new IOException("Slave id is incorrect");
		}

		if (read(is) != FUNCTION) {
			throw new IOException("function code is incorrect");
		}

		int length = read(is);
		byte[] data = new byte[3 + length];

		data[0] = (byte) slave;
		data[1] = FUNCTION; // function code
		data[2] = (byte) length;

		is.read(data, 3, length);

		int crcData = CRC16(data);
		int crcRead = is.read() | (is.read() << 8);
		if (crcData != crcRead) { // confirm CRC
			throw new IOException("CRC is incorrect");
		}

		if (is.read() != STOP_BYTE) { // confirm STOP byte
			throw new IOException("Stop byte is incorrect");
		}

		return data;
	}
	
	int read(InputStream is) throws IOException {
		int b = is.read();
		if (b < 0) {
			throw new EOFException();
		}
	
		return b;
	}

	private int CRC16(byte[] bytes) {
		int crc_value = 0xFFFF;

		for (int i = 0; i < bytes.length; i++) {
			crc_value ^= (bytes[i] & 0x0FF);

			for (int j = 0; j < 8; j++) {
				if ((crc_value & 0x01) == 0) {
					crc_value = (crc_value >> 1);
				} else {
					crc_value = ((crc_value >> 1) ^ (0xA001));
				}
			}
		}
		return crc_value;
	}
}
