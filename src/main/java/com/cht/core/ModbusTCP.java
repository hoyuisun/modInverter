package com.cht.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class ModbusTCP {
	static final Logger LOG = Logger.getLogger(ModbusTCP.class);
	
	private Socket sk;
	
	public ModbusTCP(){
	}
	
	public void Polling(ModbusProtocol mp) throws Exception {
		sk = new Socket(mp.getSerialPort(), mp.getPort());
		sk.setSoTimeout((int)mp.getTimeout());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();	
		DataOutputStream dos = new DataOutputStream(baos);
		OutputStream os = sk.getOutputStream();
		InputStream is = new BufferedInputStream(sk.getInputStream());			
		DataInputStream dis = new DataInputStream(is);
		
		int loop = 0;
		
		while(true){
			//Setting
			baos.reset();
			
			dos.writeShort(loop);
			dos.writeShort(0);
			dos.writeShort(6);
			dos.write(mp.getAddress());
			dos.write(mp.getRegister());
			dos.writeShort(mp.getReference());
			dos.writeShort(mp.getCount());
			byte[] query = baos.toByteArray();
				
			os.write(query);
			os.flush();
			
			System.out.println("-- Polling slave... (Ctrl-C to stop)");
			if(dis.readShort() != loop)
				throw new IOException("Transaction Identifier is incorrect");
			
			//Protocol Identifier
			dis.readShort();
			//Length
			dis.readShort();
			//Slave
			dis.read();
			//function code
			int function = dis.read();
			hasError(function, dis);
			int c = dis.read() / 2;
			
			for(int i = 1; i <= c; i++)
				System.out.println("[" + (mp.getReference() + i) + "]: " + dis.readShort());
			loop++;
			Thread.sleep(1000);
		}
	}
	
	public void hasError(int function, DataInputStream dis) throws IOException{
		if (function < 129 || function > 132)
			return ;
		int code = dis.read();
		if(code == 1)
			throw new IOException("Illegal Function");
		if(code == 2)
			throw new IOException("Illegal Data Address");
		if(code == 3)
			throw new IOException("Illegal Data Value");
		if(code == 4)
			throw new IOException("Slave Device Failure");
	}
}
