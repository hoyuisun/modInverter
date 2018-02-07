package com.cht.core.goodwe;

public class RunningInfo {
	public Integer v_pv1; //  PV1 voltage
	public Integer v_pv2; //  PV2 voltage
	public Integer i_pv1; //  PV1 current
	public Integer i_pv2; //  PV2 current
	 
	public Integer v_ac1; //  phase L1 voltage
	public Integer v_ac2; //  phase L2 voltage
	public Integer v_ac3; //  phase L3 voltage
	public Integer i_ac1; //  phase L1 current
	public Integer i_ac2; //  phase L2 current
	public Integer i_ac3; //  phase L3 current
	public Integer f_ac1; //  phase L1 frequency
	public Integer f_ac2; //  phase L2 frequency
	public Integer f_ac3; //  phase L3 frequency
	public Integer p_ac; //  phase L3 frequency
	
	public Integer work_mode; //  Work Mode Table3-6  wait: 0000, normal: 0001, fault:,0002
	public Integer temperature; //  Inverter internal temperature
	public Integer error_h; //  Failure description for status failure Table3-7
	public Integer error_l; //  phase L3 frequency
	
	public Integer etotal_h; // Total Feed Energy to grid
	public Integer etotal_l; // Total Feed Energy to grid
	public Integer htotal_h; // Total feeding hours
	public Integer htotal_l; // Total feeding hours
	
	public Integer tmp_fault_value; // Temperature fault value
	public Integer pv1_fault_value; // PV1 voltage fault value
	public Integer pv2_fault_value; // PV1 voltage fault value
	
	public Integer l1v_fault_value; // Phase L1 voltage fault value
	public Integer l2v_fault_value; // Phase L2 voltage fault value
	public Integer l3v_fault_value; // Phase L3 voltage fault value
	
	public Integer l1f_fault_value; // Phase L1 frequency fault value
	public Integer l2f_fault_value; // Phase L2 frequency fault value
	public Integer l3f_fault_value; // Phase L3 frequency fault value
	
	public Integer gfci_fault_value; // GFCI fault value
	public Integer e_day; // Feed Engery to grid in today
	
	//NEW <3
	public Integer etotal; // Total Feed Energy to grid
	public Integer htotal; // Total feeding hours
	
//	//NEW 2.0  結果均彥說他甚麼都不想要
//	public Integer firmware_ver;     // 0x16    NA
//	public Integer warning_code;     // 0x17    NA
//	public Integer function_value;   // 0x19    NA
//	public Integer bus_voltage;      // 0x1C    0.1v
//	public Integer nbus_voltage;     // 0x1D    0.1v
//	
//	public Integer year_month;       // 0x3B    
//	public Integer date_hour;        // 0x3C    
//	public Integer min_sec;          // 0x3D    
//	public Integer manufacture_id;   // 0x3E    
//	public Integer rssi;             // 0x3F    % 
//	public Integer fm_ver;           // 0x54    
//	public Integer gprs_mode;        // 0x55    
//	public Integer prated;           // 0x74    
//	public Integer power_factor;     // 0x75    
//	public Integer model_name;       // 0x77-0x7B
//	
}
