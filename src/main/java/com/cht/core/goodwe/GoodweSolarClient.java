package com.cht.core.goodwe;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;


public interface GoodweSolarClient {
	
	void setTimeout(long timeout);
	
	/**
	 * Send the offline query and wait for responses.
	 * 
	 * @param waiting
	 * 
	 * @return SN numbser list
	 */
	List<byte[]> discover(long timeToWait) throws IOException;	
	
	/**
	 * Send the register query to inverter
	 * 
	 * @param id and serial number(SN of device)
	 * 
	 * @return  
	 * 
	 */
	void register(byte[] sn, int id) throws IOException, TimeoutException;
	
	/**
	 * Send the unregister query to inverter
	 * 
	 * @param id 
	 * 
	 * @return 
	 */	
	void unregister(int id) throws IOException, TimeoutException;
	
	/**
	 * read the running info of device
	 * 
	 * @param id 
	 *  
	 * @return All device info in object Running info 
	 */
	RunningInfo readRunningInfo(int id) throws IOException, TimeoutException;
	
	/**
	 * read the device id of device
	 * 
	 * @param id 
	 *  
	 * @return device id, sn in object Running info 
	 */
	IdInfo readIdInfo(int id) throws IOException, TimeoutException;
	
	
	/**
	 * read the SettingInfo of device
	 * 
	 * @param id 
	 *  
	 * @return device id, sn in object Running info 
	 */
	SettingInfo readSettingInfo(int id) throws IOException, TimeoutException;	
}
