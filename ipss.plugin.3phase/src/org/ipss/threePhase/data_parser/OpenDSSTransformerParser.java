package org.ipss.threePhase.data_parser;

import static com.interpss.core.funcImpl.AcscFunction.acscXfrAptr;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.adpter.AcscXformer;

public class OpenDSSTransformerParser {
	
    private OpenDSSDataParser dataParser = null;
	
	public OpenDSSTransformerParser(OpenDSSDataParser parser){
		this.dataParser = parser;
	}
	
	
	public boolean parseTransformerData(String[] xfrStr) throws InterpssException{
		
		
		/*
		 * ! LOAD TRANSFORMER AT 61s/610
			! This is a 150 kVA Delta-Delta stepdown from 4160V to 480V.
			
			New Transformer.XFM1  Phases=3   Windings=2 Xhl=2.72
			~ wdg=1 bus=61s       conn=Delta kv=4.16    kva=150    %r=0.635
			~ wdg=2 bus=610       conn=Delta kv=0.48    kva=150    %r=0.635
		 */
		
		boolean no_error = true;
		
		
		
		int phaseNum = 3;
		int windingNum = 2;
		double xhl = 0;
		double losspercent1 = 0,losspercent2;
		double kva1 = 0, kva2 = 0;
		double normKV1 = 0, normKV2 = 0;
		String xfrId = "";
		String fromBusId = "", toBusId = "";
		String fromConnection="", toConnection = "";
		
		String defStr = xfrStr[0].trim();
		String wdg1Str = xfrStr[1].trim();
		String wdg2Str = xfrStr[1].trim();
		
		String[] defStrAry  = defStr.split("\\s+");
		String[] wdg1StrAry = defStr.split("\\s+");
		String[] wdg2StrAry = defStr.split("\\s+");
		
		for(int i = 0; i<defStrAry.length;i++){
			if(defStrAry[i].contains("Transformer.")){
				xfrId = defStrAry[i].substring(12);
			}
			else if(defStrAry[i].contains("Phases=")){
				phaseNum = Integer.valueOf(defStrAry[i].substring(7));
			}
			else if(defStrAry[i].contains("Windings=")){
				windingNum = Integer.valueOf(defStrAry[i].substring(9));
			}
			else if(defStrAry[i].contains("Xhl=")){
				xhl= Integer.valueOf(defStrAry[i].substring(4));
			}
			
		}
		
		for(int i = 0; i<wdg1StrAry.length;i++){
			if(wdg1StrAry[i].contains("bus=")){
				fromBusId = wdg1StrAry[i].substring(4);
			}
			else if(wdg1StrAry[i].contains("conn=")){
				fromConnection = wdg1StrAry[i].substring(5);
			}
			else if(wdg1StrAry[i].contains("kv=")){
				normKV1 = Double.valueOf(wdg1StrAry[i].substring(3));
			}
			else if(wdg1StrAry[i].contains("kva=")){
				kva1 = Double.valueOf(wdg1StrAry[i].substring(5));
			}
			else if(wdg1StrAry[i].contains("%r=")){
				losspercent1= Double.valueOf(wdg1StrAry[i].substring(3));
			}
		}
		
		for(int i = 0; i<wdg2StrAry.length;i++){
			
			if(wdg2StrAry[i].contains("bus=")){
				toBusId = wdg2StrAry[i].substring(4);
			}
			else if(wdg2StrAry[i].contains("conn=")){
				toConnection = wdg2StrAry[i].substring(5);
			}
			else if(wdg2StrAry[i].contains("kv=")){
				normKV2 = Double.valueOf(wdg2StrAry[i].substring(3));
			}
			else if(wdg2StrAry[i].contains("kva=")){
				kva1 = Double.valueOf(wdg2StrAry[i].substring(5));
			}
			else if(wdg2StrAry[i].contains("%r=")){
				losspercent2= Double.valueOf(wdg2StrAry[i].substring(3));
			}
		}
		
		
		// create a transformer object
		Branch3Phase xfrBranch = ThreePhaseObjectFactory.create3PBranch(fromBusId, toBusId, "0", this.dataParser.getDistNetwork());
		xfrBranch.setBranchCode(AclfBranchCode.XFORMER);
		
		xfrBranch.getFromAclfBus().setBaseVoltage(normKV1, UnitType.kV);
		xfrBranch.getToAclfBus().setBaseVoltage(normKV2, UnitType.kV);
		
		//TODO calculate r based on loss percent  
		xfrBranch.setZ( new Complex( 0.0, xhl ));
		
	    
	    AcscXformer xfr0 = acscXfrAptr.apply(xfrBranch);
	    
	    if(fromConnection.equalsIgnoreCase("Delta")){
		    xfr0.setFromConnectGroundZ(XfrConnectCode.DELTA11, new Complex(0.0,0.0), UnitType.PU);
	    }
	    else if(fromConnection.equalsIgnoreCase("Wye")){
	    	xfr0.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
	    }
	    else{
	    	throw new Error("Transformer connection type at winding 1 is not supported yet #"+fromConnection);
	    }
	    
	    if(toConnection.equalsIgnoreCase("Delta")){
		    xfr0.setToConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
	    }
	    else if(toConnection.equalsIgnoreCase("Wye")){
	    	xfr0.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
	    }
	    else{
	    	throw new Error("Transformer connection type at winding 2 is not supported yet #"+toConnection);
	    }
		
		
		
		return no_error;
	}

}
