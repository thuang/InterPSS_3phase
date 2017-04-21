package org.ipss.threePhase.data_parser;

import java.util.Hashtable;

import org.ipss.threePhase.basic.LineConfiguration;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;

import com.interpss.core.net.NetworkType;

public class OpenDSSDataParser {
	
	// Line configuration table
	protected Hashtable<String,LineConfiguration> lineConfigTable = null;
	
	protected DStabNetwork3Phase distNet = null;
	protected OpenDSSLineCodeParser lineCodeParser = null;
	protected OpenDSSLineParser lineParser = null;
	protected OpenDSSLoadParser loadParser = null;
	protected OpenDSSCapacitorParser capParser = null;
	protected OpenDSSRegulatorParser regulatorParser = null;
	
    public OpenDSSDataParser(){
    	if(this.distNet == null){
			 this.distNet = ThreePhaseObjectFactory.create3PhaseDStabNetwork();
			 this.distNet.setNetworkType(NetworkType.DISTRIBUTION);
		}
    	
    	this.lineCodeParser = new  OpenDSSLineCodeParser (this);
    	
    }
	
	public Hashtable<String, LineConfiguration> getLineConfigTable() {
		if(lineConfigTable == null){
			lineConfigTable = new Hashtable<>();
		}
		return lineConfigTable;
	}

	public void setLineConfigTable(Hashtable<String, LineConfiguration> lineConfigTable) {
		this.lineConfigTable = lineConfigTable;
	}
	
	public DStabNetwork3Phase getDistNetwork(){
		if(this.distNet == null){
			 this.distNet = ThreePhaseObjectFactory.create3PhaseDStabNetwork();
			 this.distNet.setNetworkType(NetworkType.DISTRIBUTION);
		}
		return this.distNet;
	}
	
	
     public boolean parseFeederData(String lineCodeFile, String feederFile, String loadFile, String regulatorFile){
    	 
    	 boolean no_error = true;
    	 //TODO parse the linecode,if any
    	 if(!lineCodeFile.equals(""))
    	      no_error=this.lineCodeParser.parseLineCode(lineCodeFile);
    			 
    	 //parse the master file, first create an network object as well as the sourceBus
    	 if(!feederFile.equals("")){
    		 //TODO
    		 //check if start with  (new) || NEW	
    	 }
    		 
    	 
    	 
    	 //parse the lines, transformers, regulators, loads and capacitors
    	 
    	 
    	 boolean initFlag=initNetwork();
    	 
    	 no_error=no_error&initFlag;
    	 
    	 return no_error;
     }
     
     
     public boolean initNetwork(){
    	 boolean no_error = initBusBasekV() & convertBranchZYMatrixToPU();
    	 return no_error;
     }
     private boolean initBusBasekV(){
    	 boolean no_error = true;
    	 
    	 //TODO
    	 
    	 return no_error;
     }
     
     private boolean convertBranchZYMatrixToPU(){
          boolean no_error = true;
    	 
          //TODO
    	 
    	  return no_error;
     }
    
	
}




