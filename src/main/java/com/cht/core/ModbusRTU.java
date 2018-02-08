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
	
	public ModbusRTU(){
	}
	
	public void Polling(ModbusProtocol mp) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		while(true){
			System.out.println("-- Polling slave... (Ctrl-C to stop)");
			
			dos.write(mp.getAddress());
			dos.write(mp.getRegister());
			dos.writeShort(mp.getReference());
			dos.writeShort(mp.getCount());
		
			byte[] query = baos.toByteArray();
			int crc [] = Utils.crc(query);
			
			dos.write(crc[0]);
			dos.write(crc[1]);
			
			try {
				SerialPort sp = new SerialPort(mp.getSerialPort(), mp.getBaudrate(), mp.getDatabits(), mp.getStopbits(), mp.getParity(), mp.getFlowcontrol());
				sp.setTimeout(mp.getTimeout());
	
				OutputStream os = sp.getOutputStream();
				InputStream is = new BufferedInputStream(sp.getInputStream());
				DataInputStream dis = new DataInputStream(is);
				
				os.write(query);
				os.flush();
				
				//Slave
				if(dis.read() != mp.getAddress())
					throw new IOException("Slave id is incorrect");
				if(dis.read() != mp.getRegister())
					throw new IOException("function code is incorrect");
				//Length
				int c  = dis.read() / 2;
				for(int i = 1; i <= c; i++)
					System.out.println("[" + (mp.getReference() + i) + "]: " + dis.readShort());
				
				int crc_h = dis.read();
				int crc_l = dis.read();
				//System.out.printf("%02X %02X", crc_h, crc_l);
				Thread.sleep(3000);
			}catch(Exception e){
				LOG.error(e.toString());
			}
		}
	}
}
