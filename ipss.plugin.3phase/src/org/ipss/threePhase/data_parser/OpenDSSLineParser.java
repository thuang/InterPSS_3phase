package org.ipss.threePhase.data_parser;

import org.apache.commons.math3.complex.Complex;
import org.ieee.odm.model.IODMModelParser;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.LineConfiguration;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;

public class OpenDSSLineParser {

    private OpenDSSDataParser dataParser = null;
	
	public OpenDSSLineParser(OpenDSSDataParser parser){
		this.dataParser = parser;
	}
	
	public void parseLineString(String lineStr) throws InterpssException{
		
		final String  DOT = ".";
		
		String  lineId = "";
		String  fromBusId = "";
		String  fromBusPhases  ="1.2.3"; // by default;;
		String  toBusId = "";
		String  toBusPhases = "1.2.3"; // by default;
		String  lineCodeId = "";
		double  lineLength = 0;
		int     phaseNum = 3;    // 3 phases by default
		
		String  fromBusStr = "";
		String  toBusStr = "";
		
		DStabNetwork3Phase distNet = this.dataParser.getDistNetwork();
		
		Bus3Phase fromBus = null, toBus = null;
	
		
		int lineIdIdx = 1, phaseIdx = 2, fromBusIdx = 3, toBusIdx = 4, lineCodeIdx = 5, lengthIdx = 6;
		
		if (!lineStr.contains("Phases=")){
			phaseIdx = -1;
			fromBusIdx =2;
			toBusIdx = 3; 
			lineCodeIdx = 4; 
			lengthIdx = 5;
			
		}
		String[] lineStrAry = lineStr.split("\\s+");
		
		if(phaseIdx>0)
		     phaseNum  = Integer.valueOf(lineStrAry[phaseIdx].substring(7));
		
		lineId    = lineStrAry[lineIdIdx].substring(4);
		fromBusStr = lineStrAry[fromBusIdx].substring(4);
		toBusStr   = lineStrAry[toBusIdx].substring(4);
		lineCodeId = lineStrAry[lineCodeIdx].substring(9);
		lineLength = Double.valueOf(lineStrAry[lengthIdx].substring(7));		
		
		//busId is the substring before the first DOT
		//phases info is defined in the substring after the first DOT
		if(fromBusStr.contains(DOT)){
			fromBusId = fromBusStr.substring(0, fromBusStr.indexOf(DOT));
		    fromBusPhases = fromBusId = fromBusStr.substring( fromBusStr.indexOf(DOT)+1);
		}else{
			fromBusId = fromBusStr;
			
		}
		
		if(toBusStr.contains(DOT)){
			toBusId = toBusStr.substring(0, toBusStr.indexOf(DOT));
		    toBusPhases = toBusId = toBusStr.substring( toBusStr.indexOf(DOT)+1);
		}else{
			fromBusId = toBusStr;
			
		}
		
		
		
		if(distNet.getBus(fromBusId)==null)
			fromBus = ThreePhaseObjectFactory.create3PDStabBus(fromBusId, distNet);
		
		if(distNet.getBus(toBusId)==null)
			toBus = ThreePhaseObjectFactory.create3PDStabBus(toBusId, distNet);
		
		Branch3Phase line = ThreePhaseObjectFactory.create3PBranch(fromBusId, toBusId, "1", distNet);
		line.setBranchCode(AclfBranchCode.LINE);
		// the format of Zmatrix need to be consistent with the number of phases and the phases in use.
		
		LineConfiguration config = this.dataParser.getLineConfigTable().get(lineCodeId);
		Complex3x3 zabc = null;
		Complex3x3 yshuntabc = new Complex3x3();
		
		if(config!=null){
			zabc = config.getZ3x3Matrix();
			
			if(!fromBusPhases.equals(toBusPhases)){
				throw new Error("different phase arrangements on both terminals not support yet, from: "+fromBusPhases+ ", to: "+toBusPhases);
			}
			if(phaseNum==3){
				// no change is needed
			}
			else if(phaseNum==2){
				if(fromBusPhases.equals("1.2")){
					//no change is needed
				}
				else if (fromBusPhases.equals("1.3")){
					//no change is needed
					zabc.ac = zabc.ab;
					zabc.ab = new Complex(0.0);
					
					zabc.ca = zabc.ba;
					zabc.ba = new Complex(0.0);
					
					zabc.cc = zabc.bb;
					zabc.bb = new Complex(0.0);
				}
				else if (fromBusPhases.equals("2.3")){
					
					zabc.cc = zabc.bb;
					
					zabc.bb = zabc.aa;
					zabc.aa = new Complex(0.0);
					
					zabc.bc = zabc.ab;
					zabc.ab = new Complex(0.0);
					
					zabc.cb = zabc.ba;
					zabc.ba = new Complex(0.0);
					
				}
				else{
					throw new Error("phase arrangement not support yet : "+fromBusPhases);
				}
			}
			else if(phaseNum==1){
				// by default, phase = "1"
				
				if(fromBusPhases.equals("2")){
					zabc.bb = zabc.aa;
					zabc.aa = new Complex(0.0);
				}
				else if(fromBusPhases.equals("3")){
					zabc.cc = zabc.aa;
					zabc.aa = new Complex(0.0);
				}
				else{
					throw new Error("phase arrangement not support yet : "+fromBusPhases);
				}
			}
			else{
				throw new Error("phase number must be 1, 2 or 3");
			}
					
		}
		else{
			throw new Error("LineConfiguration definition not found, LineCodeId:"+lineCodeId);
		}
		
		line.setZabc(zabc);
		
		//TODO ShuntY is not considered for this initial implementation 
		//line.setFromShuntYabc(yshuntabc.multiply(0.5));
		//line.setToShuntYabc(yshuntabc.multiply(0.5));
		
	}
	
	
}
