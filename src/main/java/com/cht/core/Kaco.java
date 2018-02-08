package com.cht.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.cht.io.SerialPort;

public class Kaco {
	static final Logger LOG = Logger.getLogger(Kaco.class);
	
	private final char QUERY = '#';
	private final char LF = 0x0A;
	private final char CR = 0x0D;	
	private int address = 1;
	
	public Kaco(){
	}
	
	public Kaco(int address){
		this.address = address;
	}
	
	public void Polling(ModbusProtocol mp) throws Exception {
		SerialPort sp = new SerialPort(mp.getSerialPort(), mp.getBaudrate(), mp.getDatabits(), mp.getStopbits(), mp.getParity(), mp.getFlowcontrol());
		
		try {
			sp.setTimeout(mp.getTimeout());
			
			OutputStream os = sp.getOutputStream();						
			InputStream is = new BufferedInputStream(sp.getInputStream());			
			
			int queryForInverterSeries = 0;
			String req = String.format("%c%02d%d%c", QUERY, address, queryForInverterSeries, CR);
		
			LOG.info("Request: " + req);
			
			os.write(req.getBytes());
			os.flush();
			
			 // 23 600TL 4  618.6 13.37  8276  630.7 12.86  8111  567.2 18.85 10695  226.2 39.14  224.4 39.11  225.1 39.16 27083 26254 1.000  45.9 139222 BECA
//			
//			int b;
//			int q = 0;
//			while ((b = is.read()) != -1) {
//				q+=b;
//				LOG.info(String.format("%02X : %d , %d", b , j++ , q ));
//				System.out.print(String.format("%02X ", b));
//		
//			}
			
			int b;
			while ((b = is.read()) != LF) {
				// seek the first LF
			}
		
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((b = is.read()) != CR) {		
				if (b == -1) {
					throw new EOFException();
				}
				baos.write(b);
			}
			LOG.info("Response: " + new String(baos.toByteArray()));			
		} catch(Exception e){
			LOG.info(e.toString());
		}finally {
			sp.close();
		}
	}
}
