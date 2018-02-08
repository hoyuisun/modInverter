package com.cht.core;

import com.cht.io.SerialPort;

public class ModbusProtocol {
	//RTU setting
	private int baudrate = 9600;
	private int databits = 8;
	private int stopbits = 1;
	private int parity = SerialPort.PARITY_NONE;
	private int flowcontrol = SerialPort.FLOWCONTROL_NONE;
	private String serialPort = "/dev/ttyS1";
	
	//Device Default query
	private int address = 1;
	private int reference = 100;
	private int register = 4;
	private int count = 1;
	private int port = 502;
	private long timeout = 3000L;
	
	public ModbusProtocol(){
	}
	
	public void setBaudrate(int baudrate){
		this.baudrate = baudrate;
	}
	
	public int getBaudrate(){
		return baudrate;
	}
	
	public void setDatabits(int databits){
		this.databits = databits;
	}
	
	public int getDatabits(){
		return databits;
	}
	
	public void setStopbits(int stopbits){
		this.stopbits = stopbits;
	}
	
	public int getStopbits(){
		return stopbits;
	}
	
	public void setParity(int parity){
		this.parity = parity;
	}
	
	public int getParity(){
		return parity;
	}
	
	public void setFlowcontrol(int flowcontrol){
		this.flowcontrol = flowcontrol;
	}
	
	public int getFlowcontrol(){
		return flowcontrol;
	}
	
	public void setSerialPort(String serialPort){
		this.serialPort = serialPort;
	}
	
	public String getSerialPort(){
		return serialPort;
	}
	
	public void setAddress(int address){
		this.address = address;
	}
	
	public int getAddress(){
		return address;
	}
	
	public void setReference(int reference){
		this.reference = reference;
	}
	
	public int getReference(){
		return reference;
	}
	
	public void setRegister(int register){
		this.register = register;
	}
	
	public int getRegister(){
		return register;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public int getCount(){
		return count;
	}
	
	public void setPort(int port){
		this.port = port;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setTimeout(long timeout){
		this.timeout = timeout;
	}
	
	public long getTimeout(){
		return timeout;
	}
}
