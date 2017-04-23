package org.ipss.threePhase.data_parser;

import org.apache.commons.math3.complex.Complex;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.DistLoadType;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.basic.LoadConnectionType;
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;

import com.interpss.common.exp.InterpssException;

public class OpenDSSLoadParser {
	
	/*
	 *  load model code: 
	 *  1:Standard constant P+jQ load. (Default)
		2:Constant impedance load.
		3:Const P, Quadratic Q (like a motor).
		4:Nominal Linear P, Quadratic Q (feeder mix). Use this with CVRfactor.
		5:Constant Current Magnitude
		6:Const P, Fixed Q
		7:Const P, Fixed Impedance Q
		8:ZIPV (7 values)
	 */
	
	/*
	 *  !
		! LOAD DEFINITIONS
		!
		! Note that 1-phase loads have a voltage rating = to actual voltage across terminals
		! This could be either 2.4kV for Wye connectoin or 4.16 kV for Delta or Line-Line connection.
		! 3-phase loads are rated Line-Line (as are 2-phase loads, but there are none in this case).
		! Only the balanced 3-phase loads are declared as 3-phase; unbalanced 3-phase loads are declared
		! as three 1-phase loads.
	 */
	    private OpenDSSDataParser dataParser = null;
		
		public OpenDSSLoadParser(OpenDSSDataParser parser){
			this.dataParser = parser;
		}
		
		public boolean parseLoadData(String loadStr) throws InterpssException{
			boolean no_error = true;
			
			/*
			 * New Load.S1a   Bus1=1.1    Phases=1 Conn=Wye   Model=1 kV=2.4   kW=40.0  kvar=20.0 
			 * 
			 * NOTE:
			 * 1) if kvar is not used, need to use PF 
			 * 2) for loads connected between two phases, Bus1=BusName.1.2, Phases=1
			 * To model loads connected between phases, use a pair of <-Iload, Iload> for the two phases.
			 * For the load modeling, need to add connection type, phase1, phase2 attributes
			 */
			final String DOT = ".";
			String loadId ="";
			String loadId_phases ="";
			String busName ="";
			String phase1 =""; 
			String phase2 ="";
			String phase3 ="";
			String connectionType = "";
			int phaseNum = 3;  // three phases by default
			int modelType = 1; // constant power load by default
			double nominalKV = 0;
			double loadP = 0.0, loadQ  = 0.0;
			double powerfactor = 0.0;
			
			String[] loadStrAry = loadStr.trim().split("\\s+");
			
			for(int i = 0; i <loadStrAry.length;i++){
				if(loadStrAry[i].startsWith("Load.")||loadStrAry[i].startsWith("load.")){
					loadId =loadStrAry[i].substring(5); 
				}
				else if(loadStrAry[i].startsWith("Bus1=")||loadStrAry[i].startsWith("bus1=")){
					loadId_phases =loadStrAry[i].substring(5);
				}
				else if(loadStrAry[i].startsWith("Phases=")||loadStrAry[i].startsWith("phases=")){
					phaseNum = Integer.valueOf(loadStrAry[i].substring(7));
				}
				else if(loadStrAry[i].startsWith("Conn=")||loadStrAry[i].startsWith("conn=")){
					connectionType =loadStrAry[i].substring(5);
				}
				else if(loadStrAry[i].startsWith("Model=")||loadStrAry[i].startsWith("model=")){
					 modelType =Integer.valueOf(loadStrAry[i].substring(6));
				}
				else if(loadStrAry[i].startsWith("kW=")||loadStrAry[i].startsWith("kw=")){
					 loadP=Double.valueOf(loadStrAry[i].substring(3));
				}
				else if(loadStrAry[i].startsWith("kVar=")||loadStrAry[i].startsWith("kvar=")){
					 loadQ=Double.valueOf(loadStrAry[i].substring(5));
				}
				else if(loadStrAry[i].startsWith("PF=")||loadStrAry[i].startsWith("pf=")){
					 powerfactor=Double.valueOf(loadStrAry[i].substring(3));
				}
				
				
			}
			
			if(loadId_phases.contains(DOT)){
				String[] idPhasesAry = loadId_phases.split(DOT);
				busName = idPhasesAry[0];
				if(idPhasesAry.length>1){
					phase1 = idPhasesAry[1];
				}
				if(idPhasesAry.length>2){
					phase2 = idPhasesAry[2];
				}
				if(idPhasesAry.length>3){
					phase3 = idPhasesAry[3];
				}
				
			}
			else{
				busName = loadId_phases;
				if(phaseNum==3){
					//TODO need to set phase1,2,3???
				}
			}
			
			if(powerfactor!=0.0 && loadQ==0.0){
				loadQ = loadP*Math.tan(Math.acos(powerfactor));
			}
			
			Complex loadPQ = new Complex(loadP,loadQ);
			
			//get the bus object
			Bus3Phase bus = (Bus3Phase) this.dataParser.getDistNetwork().getBus(busName);
			
			Load3Phase load= new Load3PhaseImpl();
			
			// rated KV
			load.setNominalKV(nominalKV);
			
			//load model type
			if(modelType==1){
				load.setLoadModelType(DistLoadType.CONST_PQ);
				load.setLoadCP(loadPQ);
			}
			else if(modelType==2){
				load.setLoadModelType(DistLoadType.CONST_Z);
				load.setLoadCZ(loadPQ);
			}
			else if(modelType==5){
				load.setLoadModelType(DistLoadType.CONST_I);
				load.setLoadCI(loadPQ);
			}
			else{
				no_error = false;
				throw new Error("Load model type is not supported yet! # "+loadStr);
			}
				
			//load connection type
			if(phaseNum==3){
				if(connectionType.equalsIgnoreCase("delta")){
					load.setLoadConnectionType(LoadConnectionType.Three_Phase_Delta);
				}
				else if(connectionType.equalsIgnoreCase("wye")){
					load.setLoadConnectionType(LoadConnectionType.Three_Phase_Wye);
				}
				else{
					no_error = false;
					throw new Error("Load connection type is not supported yet! # "+connectionType);
				}
			}
			else if(phaseNum==2){
				no_error = false;
			    throw new Error("Load connection type for two phases is not supported yet! # "+connectionType);
				
			}
			else if(phaseNum==1){
				if(connectionType.equalsIgnoreCase("wye")){
					load.setLoadConnectionType(LoadConnectionType.Single_Phase_Wye);
				}
				else if(connectionType.equalsIgnoreCase("delta")){
					if(phase1.length()>0 && phase2.length()>0)
					  load.setLoadConnectionType(LoadConnectionType.Single_Phase_Delta);
					else{
						no_error = false;
						throw new Error("Connection phases info is not consistent with the connection type # "+loadStr);
					}
				}
				else{
					no_error = false;
					throw new Error("Load connection type not supported yet! # "+connectionType);
				}
			}
			
		 
			
			bus.getThreePhaseLoadList().add(load);
			
			return no_error;
			
		}
}
