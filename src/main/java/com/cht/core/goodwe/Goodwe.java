package com.cht.core.goodwe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.cht.core.ModbusProtocol;

public class Goodwe {
	static final Logger LOG = Logger.getLogger(GoodweSolarClientImpl.class);
	
	private String GoodweSN [] = new String [10];
	private int id [] = new int [10];
	private int count = 0;
	private BufferedReader br;
	
	public Goodwe(){
	}
	
	public Goodwe(String path) throws IOException{
		FileReader fr = new FileReader(path);
		br = new BufferedReader(fr);
		String line;
		while((line = br.readLine()) != null){
			id[count] = Integer.valueOf(line);
			line = br.readLine();
				GoodweSN[count] = line;
			count++;
		}
	}
	
	public void Polling(ModbusProtocol mp) throws Exception {
		byte sn [][] = new byte[10][];
		for(int i = 0; i < count; i++)
			sn[i] = GoodweSN[i].getBytes();
		
		//9025KDTU17AR0672
		//byte[] sn1 = {0x39, 0x30, 0x32, 0x35, 0x4b, 0x44, 0x54, 0x55, 0x31, 0x37, 0x41, 0x52, 0x30, 0x36, 0x37, 0x32 }; 

		GoodweSolarClientImpl client = new GoodweSolarClientImpl(mp.getSerialPort(), mp.getBaudrate(), mp.getDatabits(), mp.getStopbits(), mp.getParity(),
				mp.getFlowcontrol(), mp.getTimeout());

		client.setTimeout(mp.getTimeout());
		
		//Register first!!
		for(int i = 0; i < count; i++){
			LOG.info("Register " + id[i] + " of Goodwe");
			try {
				client.register(sn[i], id[i]);			
				Thread.sleep(5000L);		
			} catch(Exception e){
				LOG.info(e.toString());
			}
		}
		//Query goodwe
		for (int i = 0; i < count; i++){
			LOG.info("Asking " + id[i] + " of Goodwe");
			try {	
				client.readRunningInfo(id[i]);
				Thread.sleep(8000L);
			} catch(Exception e){
				LOG.info(e.toString());
			} 
		}
		//client.close();
	}
}
