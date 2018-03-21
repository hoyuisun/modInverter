package com.cht.jasperboy;

import org.apache.log4j.Logger;

import com.cht.core.Kaco;
import com.cht.core.Kstar;
import com.cht.core.ModbusProtocol;
import com.cht.core.ModbusRTU;
import com.cht.core.ModbusTCP;
import com.cht.core.Motech;
import com.cht.core.goodwe.Goodwe;

public class Main {
	static final Logger LOG = Logger.getLogger(Main.class);
	static boolean isStupid = false;
	static boolean isError = false;
	
	private ModbusProtocol mp = new ModbusProtocol();
	private String method = "";
	
	private String path = "";		//Goodwe
	
	private int Motech_start = 1;	
	private int Motech_end = 250;
	
	public Main(){
	}
	
	public Main(String args []){
		if (args.length == 0 | (args.length)%2 == 1){
			isStupid = true;
			return ;
		}
		try{
			for (int i = 0; i < args.length; i += 2){
				if(args[i].equals("-m")) 
					method = args[i+1];
				else if(args[i].equals("-b")) 
					mp.setBaudrate(Integer.valueOf(args[i+1]));
				else if(args[i].equals("-d")) 
					mp.setDatabits(Integer.valueOf(args[i+1]));
				else if(args[i].equals("-s")) 
					mp.setStopbits(Integer.valueOf(args[i+1]));
				else if(args[i].equals("-i")) 
					mp.setSerialPort(args[i+1]);
				else if(args[i].equals("-a")) 
					mp.setAddress(Integer.valueOf(args[i+1]));
				else if (args[i].equals("-r"))
					mp.setReference(Integer.valueOf(args[i+1]) - 1);	//Base 0
				else if(args[i].equals("-c")) 
					mp.setCount(Integer.valueOf(args[i+1]));
				else if(args[i].equals("-t")) 
					mp.setRegister(Integer.valueOf(args[i+1]));
				else if (args[i].equals("-p"))
					mp.setPort(Integer.valueOf(args[i+1]));
				else if(args[i].equals("--timeout")) 
					mp.setTimeout(Integer.valueOf(args[i+1]));
				else if(args[i].equals("-f")) 
					path = args[i+1];
				else if(args[i].equals("--ms"))
					Motech_start = Integer.valueOf(args[i+1]); 
				else if(args[i].equals("--me"))	
					Motech_end = Integer.valueOf(args[i+1]);  
				else if(args[i].equals("-h")) 
					isStupid = true;
				else
					isStupid = true;
			}
		}catch(Exception e){
			LOG.info("I think you need help TAT, so find Jasper!! Or maybe type -h ask for help");
			isError = true;
		}
	}
	
	public void doPoll() {
		if (method == "")
			return ;
		else if(method.equals("motech")) 
			pollMotech();
		else if(method.equals("goodwe")) 
			pollGoodwe();
		else if(method.equals("kaco")) 
			pollKaco();
		else if(method.equals("kstar")) 
			pollKstar();
		else if (method.equals("rtu"))
			pollModbusRTU();
		else if (method.equals("tcp"))
			pollModbusTCP();
		else
			LOG.info("This inverter is not supported yet!, please tell Jasper to support");
	}
	
	public void pollModbusTCP(){
		HoldToInput(mp.getRegister());
		try {
			ModbusTCP modbus = new ModbusTCP();
			modbus.Polling(mp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pollModbusRTU(){
		HoldToInput(mp.getRegister());
		try {
			ModbusRTU modbus = new ModbusRTU();
			modbus.Polling(mp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pollGoodwe(){
		try {
			Goodwe goodwe = new Goodwe(path);
			goodwe.Polling(mp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pollKaco(){
		Kaco kaco = new Kaco(mp.getAddress());
		try {
			kaco.Polling(mp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pollMotech(){
		Motech motech = new Motech(Motech_start, Motech_end);
		try {
			motech.Polling(mp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pollKstar(){
		Kstar kstar = new Kstar(mp.getAddress());
		try {
			kstar.Polling(mp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void HoldToInput(int register){
		if(register == 3)
			mp.setRegister(4);
		else if(register == 4)
			mp.setRegister(3);
	}
	
	public void doHelp(){
		System.out.println("[Usage]");
		System.out.println("> java -jar modInverter.jar -h");
		System.out.println("\nUsage: modinverter -m method [options]");
		System.out.println("\nGeneral options:\n");
		System.out.println("-m rtu|tcp|motech|goodwe|kaco|kstar\tCHT supported inverter");
		System.out.println("-b #\t\tBaudrate (e.g. 9600, 19200, ...) (9600 is default)");
		System.out.println("-d #\t\tDatabits (7 or 8 for ASCII protocol, 8 for RTU)");
		System.out.println("-s #\t\tStopbits (1 or 2, 1 is default)");
		System.out.println("-a #\t\tSlave id (1 is default)");
		System.out.println("-r #\t\tStart reference (1-65546, 100 is default)");
		System.out.println("-t 0\t\tDiscrete output (coil) data type");
		System.out.println("-t 1\t\tDiscrete input data type");
		System.out.println("-t 3\t\t16-bit input register data type");
		System.out.println("-t 4\t\t16-bit output (holding) register data type (default)");
		System.out.println("-p #\t\tTCP port number (502 is default)");
		System.out.println("-f #\t\tThe path of ID/SN for Goodwe");
		System.out.println("--timeout #\tResponse timeout in ms, (1000 is default)");
		System.out.println("--ms #\t\tThe start position of slave id is queried in Motech, (1 is default)");
		System.out.println("--me #\t\tThe end position of slave id is queried in Motech, (250 is default)");
		System.out.println("\n[Example 1: modbus/motech]");
		System.out.println("> java -Djava.library.path=lib -jar modinverter.jar -m motech -b 9600 -d 8 -s 1 --ms 2 --me 5 -i /dev/ttyS1");
		System.out.println("\n[Example 2: modbus/goodwe]");
		System.out.println("> java -Djava.library.path=lib -jar modinverter.jar -m goodwe -b 9600 -d 8 -s 1 -i /dev/ttyS1 -f sn");
		System.out.println("\n[Example 3: modbus/kaco]");
		System.out.println("> java -Djava.library.path=lib -jar modinverter.jar -m kaco -b 9600 -d 8 -s 1 -a 1 --timeout 5000 -i /dev/ttyS1");
		System.out.println("\n[Example 4: modbus/kstar]");
		System.out.println("> java -Djava.library.path=lib -jar modinverter.jar -m kstar -b 9600 -d 8 -s 1 -a 1 --timeout 5000 -i /dev/ttyS1\n");
	}
	
	public static void main(String [] args) {
		final Main main = new Main(args);
		if (!isStupid && !isError)
			main.doPoll();	
		else if(isStupid)
			main.doHelp();
	}
}
