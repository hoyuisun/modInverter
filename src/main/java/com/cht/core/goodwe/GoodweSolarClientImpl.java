package com.cht.core.goodwe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.cht.io.SerialPort;

public class GoodweSolarClientImpl implements GoodweSolarClient {
	static final Logger LOG = Logger.getLogger(GoodweSolarClientImpl.class);

	static final int HEADER_1 = 0x0AA;
	static final int HEADER_2 = 0x055;
	static final int SOURCE_ADDRESS = 0x080;
	static final int BUFFER_SIZE = 16;

	final SerialPort serial;
	final OutputStream os;
	final PushbackInputStream is;

	long timeout = 5000L; // request timeout, not I/O timeout

	Map<String, GoodweRequest> requests = Collections.synchronizedMap(new HashMap<String, GoodweRequest>());

	ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * 
	 * @param port
	 * @param baudrate
	 * @param databits
	 * @param stopbits
	 * @param parity
	 * @param flowcontrol
	 * @param timeout		this is I/O timeout of serial port
	 * 
	 * @throws IOException
	 */
	public GoodweSolarClientImpl(String port, int baudrate, int databits, int stopbits, int parity, int flowcontrol, long timeout) throws IOException {		
		serial = new SerialPort(port, baudrate, databits, stopbits, parity, flowcontrol);
		serial.setTimeout(timeout);
		
		os = new BufferedOutputStream(serial.getOutputStream());
		is = new PushbackInputStream(new BufferedInputStream(serial.getInputStream()), BUFFER_SIZE);
	}
	
	public String getPort() {
		return serial.getPort();
	}

	/**
	 * Set the response timeout.
	 * 
	 * @param timeout
	 */
	public void setTimeout(long timeout) {		
		this.timeout = timeout;
	}

	public void close() throws IOException {
		serial.close();
	}
	
	protected synchronized void write(byte[] pdu) throws IOException {
		os.write(pdu);
		os.flush();
	}
	
	public byte[] encodeWritingRequest(int id, int controlCode, int functionCode, int length) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		baos.write(HEADER_1);
		baos.write(HEADER_2);
		baos.write(SOURCE_ADDRESS);
		baos.write(id);
		baos.write(controlCode);
		baos.write(functionCode);
		baos.write(length);
		baos.flush();

		return baos.toByteArray();
	}
	
	@Override
	public List<byte[]> discover(long timeToWait) throws IOException {
		int id = 0x7F; // broadcast
		int controlCode = 0x00;
		int functionCode = 0x00;
		int length = 0;

		String rid = "discover";
		GoodweRequest req = new GoodweRequest();
		requests.put(rid, req);

		try {
			byte[] body = encodeWritingRequest(id, controlCode, functionCode, length);

			int checksum = 0;
			for (byte b : body) {
				checksum += (int) (b & 0x00ff);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(body);
			baos.write((checksum >> 8) & 0x0FF);
			baos.write(checksum & 0x0FF);

			byte[] pdu = baos.toByteArray();

			write(pdu);

			reading(); // fork a thread to read the response
			
			List<byte[]> ids = new ArrayList<byte[]>();

			try {
				for (;;) {
					byte[] did = (byte[]) req.getValue(timeToWait);
					ids.add(did);
					
					req.reset();
				}
			} catch (TimeoutException e) {

			} catch (Exception e) {
				throw new IOException(e.getMessage(), e); // TimeoutException or
															// InterruptedException
			}

			return ids;

		} finally {
			requests.remove(rid);
		}
	}

	@Override
	public void register(byte[] sn, int id) throws IOException, TimeoutException {
		int broadcast = 0x7F;
		int controlCode = 0x00;
		int functionCode = 0x01;
		int length = 17;

		String rid = String.format("%d.register", id);
		GoodweRequest req = new GoodweRequest();
		requests.put(rid, req);

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] body = encodeWritingRequest(broadcast, controlCode, functionCode, length);
			baos.write(body);
			baos.write(sn);
			baos.write(id);
			
			body = baos.toByteArray();

			int checksum = 0;
			for (byte b : body) {
				checksum += (int) (b & 0x00ff);
			}

			baos.write((checksum >> 8) & 0x0FF);
			baos.write(checksum & 0x0FF);
			
			byte[] pdu = baos.toByteArray();

			write(pdu);

			reading();

			try {
				Integer code = (Integer) req.getValue(timeout);
				if (code != 0) {
					throw new IOException("Failed to register - code: " + code);
				}
			} catch (TimeoutException e) {
				throw e;
				
			} catch (IOException e) {
				throw e;

			} catch (Exception e) {
				throw new IOException(e.getMessage(), e); // TimeoutException or InterruptedException
			}
		} finally {
			requests.remove(rid);
		}
	}

	@Override
	public void unregister(int id) throws IOException, TimeoutException {
		int controlCode = 0x00;
		int functionCode = 0x02;
		int length = 00;

		String rid = String.format("%d.unregister", id);
		GoodweRequest req = new GoodweRequest();
		requests.put(rid, req);

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] body = encodeWritingRequest(id, controlCode, functionCode, length);
			baos.write(body);
			
			body = baos.toByteArray();

			int checksum = 0;
			for (byte b : body) {
				checksum += (int) (b & 0x00ff);
			}

			baos.write((checksum >> 8) & 0x0FF);
			baos.write(checksum & 0x0FF);
			
			byte[] pdu = baos.toByteArray();

			write(pdu);

			reading();

			try {
				Integer code = (Integer) req.getValue(timeout);
				if (code != 0) {
					throw new IOException("Failed to register - code: " + code);
				}
			} catch (TimeoutException e) {
				throw e;
				
			} catch (IOException e) {
				throw e;

			} catch (Exception e) {
				throw new IOException(e.getMessage(), e); // TimeoutException or InterruptedException
			}
		} finally {
			requests.remove(rid);
		}
	}

	@Override
	public RunningInfo readRunningInfo(int id) throws IOException, TimeoutException {
		int controlCode = 0x01;
		int functionCode = 0x01;
		int length = 00;

		String rid = String.format("%d.runningInfo", id);
		GoodweRequest req = new GoodweRequest();
		requests.put(rid, req);

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] body = encodeWritingRequest(id, controlCode, functionCode, length);
			baos.write(body);
			
			body = baos.toByteArray();

			int checksum = 0;
			for (byte b : body) {
				checksum += (int) (b & 0x00ff);
			}

			baos.write((checksum >> 8) & 0x0FF);
			baos.write(checksum & 0x0FF);
			
			byte[] pdu = baos.toByteArray();

			write(pdu);

			reading();

			try {
				return (RunningInfo) req.getValue(timeout);
				
			} catch (TimeoutException e) {
				throw e;

			} catch (Exception e) {
				throw new IOException(e.getMessage(), e); // TimeoutException or InterruptedException
			}
		} finally {
			requests.remove(rid);
		}
	}

	@Override
	public IdInfo readIdInfo(int id) throws IOException, TimeoutException {
		int controlCode = 0x01;
		int functionCode = 0x02;
		int length = 00;

		String rid = String.format("%d.idInfo", id);
		GoodweRequest req = new GoodweRequest();
		requests.put(rid, req);

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] body = encodeWritingRequest(id, controlCode, functionCode, length);
			baos.write(body);
			
			body = baos.toByteArray();

			int checksum = 0;
			for (byte b : body) {
				checksum += (int) (b & 0x00ff);
			}

			baos.write((checksum >> 8) & 0x0FF);
			baos.write(checksum & 0x0FF);
			
			byte[] pdu = baos.toByteArray();

			write(pdu);

			reading();

			try {
				return (IdInfo) req.getValue(timeout);
				
			} catch (TimeoutException e) {
				throw e;

			} catch (Exception e) {
				throw new IOException(e.getMessage(), e); // TimeoutException or
															// InterruptedException
			}
		} finally {
			requests.remove(rid);
		}
	}

	@Override
	public SettingInfo readSettingInfo(int id) throws IOException, TimeoutException {
		int controlCode = 0x01;
		int functionCode = 0x03;
		int length = 00;

		String rid = String.format("%d.settingInfo", id);
		GoodweRequest req = new GoodweRequest();
		requests.put(rid, req);

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] body = encodeWritingRequest(id, controlCode, functionCode, length);
			baos.write(body);
			
			body = baos.toByteArray();

			int checksum = 0;
			for (byte b : body) {
				checksum += (int) (b & 0x00ff);
			}

			baos.write((checksum >> 8) & 0x0FF);
			baos.write(checksum & 0x0FF);
			
			byte[] pdu = baos.toByteArray();

			write(pdu);

			reading();

			try {
				return (SettingInfo) req.getValue(timeout);
				
			} catch (TimeoutException e) {
				throw e;

			} catch (Exception e) {
				throw new IOException(e.getMessage(), e); // TimeoutException or
															// InterruptedException
			}
		} finally {
			requests.remove(rid);
		}
	}

	protected void reading() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				doRead();
			}
		});
	}

	protected int read(InputStream is) throws IOException {
		int c = is.read();
		if (c < 0) {
			throw new EOFException("EOF");
		}

		return c;
	}

	protected synchronized void doRead() {
		try {
			for (;;) {
				LOG.info("READING..\n");
				
				while (read(is) != HEADER_1); // seek to head
				
				int c;
				if ((c = read(is)) != HEADER_2) { // push back
					is.unread(c);
					continue; // try to search the HEADER_1 again
				}

				int id = read(is);
				int sourceId = read(is);
				int controlCode = read(is);
				int functionCode = read(is);
				int length = read(is);				
				
				byte[] body = new byte[length];

				for (int i = 0; i < length; i++) {
					body[i] = (byte) read(is);
				}
				
				int cs = (read(is) << 8) | (read(is));

				int checksum = HEADER_1 + HEADER_2;
				checksum += (id + sourceId + controlCode + functionCode + length);

				for (byte b : body) {
					checksum += (b & 0x0FF);
				}

				if (checksum != cs) {
					throw new IOException("checksum is incorrect");
				}
				
				if ((controlCode == 00) && (functionCode == 0x080)) { // discover
					String rid = "discover";
					GoodweRequest req = requests.get(rid);

					if (req != null) {
						req.setValue(Arrays.asList(body));
					}					
					
				} else if ((controlCode == 00) && (functionCode == 0x081)) { // register
					String rid = String.format("%d.register", id);
					GoodweRequest req = requests.get(rid);

					if (req != null) {
						if (length == 0) {
							req.setValue(0); // it's okay without any data
							
						} else {
							req.setValue(body[0]); // this is error code
						}
					}
					break;
					
				} else if ((controlCode == 00) && (functionCode == 0x082)) { // unregister
					String rid = String.format("%d.unregister", id);
					GoodweRequest req = requests.get(rid);

					if (req != null) {
						if ((length == 1) && (body[0] == 0x06)) {
							req.setValue(0); // it's okay
							
						} else {
							req.setValue(body[0]); // error code
						}
					}
					break;
					
				} else if ((controlCode == 0x01) && (functionCode == 0x081)) { // runningInfo
					String rid = String.format("%d.runningInfo", id);
					GoodweRequest req = requests.get(rid);

					if (req != null) {
						RunningInfo r = parseRunningInfo(body, length);
						req.setValue(r);
					}
					break;
					
				} else if ((controlCode == 0x01) && (functionCode == 0x082)) {  // idInfo
					String rid = String.format("%d.idInfo", id);
					GoodweRequest req = requests.get(rid);

					if (req != null) {
						IdInfo r = parseIdInfo(body);
						req.setValue(r);
					}
					break;
					
				} else if ((controlCode == 0x01) && (functionCode == 0x083)) { // settingInfo
					String rid = String.format("%d.settingInfo", id);
					GoodweRequest req = requests.get(rid);

					if (req != null) {
						SettingInfo r = parseSettingInfo(body, length);
						req.setValue(r);
					}
					break;
				}
				LOG.info("AFTER READING..\n");
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}	

	public RunningInfo parseRunningInfo(byte[] body, int length) {
		int[] data = new int[length / 2]; // length

		for (int i = 0; i < data.length; i++) { // FIXME - DataInputStream is batter
			data[i] = (((body[i * 2] << 8) & 0x0FF00) + (body[1 + i * 2] & 0x0FF));
		}
		
		RunningInfo r = new RunningInfo();
		
		for(int i = 0; i < data.length; i++) // FIXME - ALL TO YOU
			LOG.info(data[i]);
		
		int i = 0;
		
		if (data.length == 23) {
			r.v_pv1 = data[i++];
			r.v_pv2 = data[i++];
			r.i_pv1 = data[i++];
			r.i_pv2 = data[i++];
			r.v_ac1 = data[i++];
			r.i_ac1 = data[i++];
			r.f_ac1 = data[i++];			
			r.p_ac = data[i++];
			r.work_mode = data[i++];
			r.temperature = data[i++];
			r.error_h = data[i++];
			r.error_l = data[i++];
			r.etotal_h = data[i++];
			r.etotal_l = data[i++];
			r.htotal_h = data[i++];
			r.htotal_l = data[i++];
			r.tmp_fault_value = data[i++];
			r.pv1_fault_value = data[i++];
			r.pv2_fault_value = data[i++];
			r.l1v_fault_value = data[i++];
			r.l1f_fault_value = data[i++];
			r.gfci_fault_value = data[i++];
			r.e_day = data[i++];
			
			r.etotal = (r.etotal_h << 16 )  + r.etotal_l  ;
			r.htotal = (r.htotal_h << 16 )  + r.htotal_l  ;
			
		}else if (data.length == 30) {
			r.v_pv1 = data[i++];
			r.v_pv2 = data[i++];
			r.i_pv1 = data[i++];
			r.i_pv2 = data[i++];
			r.v_ac1 = data[i++];
			r.i_ac1 = data[i++];
			r.f_ac1 = data[i++];			
			r.p_ac = data[i++];
			r.work_mode = data[i++];
			r.temperature = data[i++];
			r.error_h = data[i++];
			r.error_l = data[i++];
			r.etotal_h = data[i++];
			r.etotal_l = data[i++];
			r.htotal_h = data[i++];
			r.htotal_l = data[i++];
			r.tmp_fault_value = data[i++];
			r.pv1_fault_value = data[i++];
			r.pv2_fault_value = data[i++];
			r.l1v_fault_value = data[i++];
			r.l1f_fault_value = data[i++];
			r.gfci_fault_value = data[i++];
			r.e_day = data[i++];
			
			r.etotal = (r.etotal_h << 16 )  + r.etotal_l  ;
			r.htotal = (r.htotal_h << 16 )  + r.htotal_l  ;
			
		} else if (data.length == 33) {
			r.v_pv1 = data[i++];
			r.v_pv2 = data[i++];
			r.i_pv1 = data[i++];
			r.i_pv2 = data[i++];
			r.v_ac1 = data[i++];
			r.v_ac2 = data[i++];
			r.v_ac3 = data[i++];
			r.i_ac1 = data[i++];
			r.i_ac2 = data[i++];
			r.i_ac3 = data[i++];
			r.f_ac1 = data[i++];
			r.f_ac2 = data[i++];
			r.f_ac3 = data[i++];
			r.p_ac = data[i++];
			r.work_mode = data[i++];
			r.temperature = data[i++];
			r.error_h = data[i++];
			r.error_l = data[i++];
			r.etotal_h = data[i++];
			r.etotal_l = data[i++];
			r.htotal_h = data[i++];
			r.htotal_l = data[i++];
			r.tmp_fault_value = data[i++];
			r.pv1_fault_value = data[i++];
			r.pv2_fault_value = data[i++];
			r.l1v_fault_value = data[i++];
			r.l2v_fault_value = data[i++];
			r.l3v_fault_value = data[i++];
			r.l1f_fault_value = data[i++];
			r.l2f_fault_value = data[i++];
			r.l3f_fault_value = data[i++];
			r.gfci_fault_value = data[i++];
			r.e_day = data[i++];
			
			r.etotal = (r.etotal_h << 16 )  + r.etotal_l  ;
			r.htotal = (r.htotal_h << 16 )  + r.htotal_l ;
			
		}else if (data.length == 48) {
			r.v_pv1 = data[i++];
			r.v_pv2 = data[i++];
			r.i_pv1 = data[i++];
			r.i_pv2 = data[i++];
			r.v_ac1 = data[i++];
			r.v_ac2 = data[i++];
			r.v_ac3 = data[i++];
			r.i_ac1 = data[i++];
			r.i_ac2 = data[i++];
			r.i_ac3 = data[i++];
			r.f_ac1 = data[i++];
			r.f_ac2 = data[i++];
			r.f_ac3 = data[i++];
			r.p_ac = data[i++];
			r.work_mode = data[i++];
			r.temperature = data[i++];
			r.error_h = data[i++];
			r.error_l = data[i++];
			r.etotal_h = data[i++];
			r.etotal_l = data[i++];
			r.htotal_h = data[i++];
			r.htotal_l = data[i++];
			r.tmp_fault_value = data[i++];
			r.pv1_fault_value = data[i++];
			r.pv2_fault_value = data[i++];
			r.l1v_fault_value = data[i++];
			r.l2v_fault_value = data[i++];
			r.l3v_fault_value = data[i++];
			r.l1f_fault_value = data[i++];
			r.l2f_fault_value = data[i++];
			r.l3f_fault_value = data[i++];
			r.gfci_fault_value = data[i++];
			r.e_day = data[i++];

			r.etotal = (r.etotal_h << 16 )  + r.etotal_l  ;
			r.htotal = (r.htotal_h << 16 )  + r.htotal_l ;
		}else{
			LOG.info("Length is error !");
			
		}


		return r;
	}

	public IdInfo parseIdInfo(byte[] body) {
		String str = new String(body);

		IdInfo r = new IdInfo();
		
		r.firmware_ver = str.substring(0, 5);
		r.mode_name = str.substring(5, 15);
		r.na = str.substring(15, 31);
		r.serial_number = str.substring(31, 47);
		r.nom_vpv = str.substring(47, 51);
		r.internal_version = str.substring(51, 63);
		r.safety_conntry_code = Integer.toString(body[63]);

		return r;

	}

	public SettingInfo parseSettingInfo(byte[] body, int length) {
		int[] data = new int[length / 2]; // length

		for (int i = 0; i < data.length; i++) {
			data[i] = (((body[i * 2] << 8) & 0x0FF00) + (body[1 + i * 2] & 0x0FF));
		}
		
		SettingInfo r = new SettingInfo();

		r.vpv_start = data[0];
		r.t_start = data[1];
		r.vac_min = data[2];
		r.vac_max = data[3];
		r.fac_min = data[4];
		r.fac_max = data[5];

		return r;
	}

	static class GoodweRequest {
		Object value;

		public GoodweRequest() {
		}

		public synchronized void reset() {
			value = null;
		}

		public synchronized void setValue(Object value) {
			this.value = value;
			
			notifyAll();
		}

		public synchronized Object getValue(long timeout) throws InterruptedException, TimeoutException {
			if (value == null) {
				wait(timeout);
			}
			
			if (value == null) {
				throw new TimeoutException();
			}
			
			return value;
		}
	}
}
