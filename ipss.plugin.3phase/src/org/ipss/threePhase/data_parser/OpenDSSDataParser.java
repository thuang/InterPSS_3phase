package org.ipss.threePhase.data_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.ieee.odm.common.IFileReader;
import org.ieee.odm.common.ODMException;
import org.ieee.odm.common.ODMLogger;
import org.ieee.odm.common.ODMTextFileReader;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.LineConfiguration;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;

import com.interpss.core.net.Branch;
import com.interpss.core.net.NetworkType;

public class OpenDSSDataParser {
	
	// Line configuration table
	protected Hashtable<String,LineConfiguration> lineConfigTable = null;
	
	protected DStabNetwork3Phase distNet = null;
	protected OpenDSSLineCodeParser lineCodeParser = null;
	protected OpenDSSLineParser lineParser = null;
	protected OpenDSSLoadParser loadParser = null;
	protected OpenDSSTransformerParser xfrParser = null;
	protected OpenDSSCapacitorParser capParser = null;
	protected OpenDSSRegulatorParser regulatorParser = null;
	
    public OpenDSSDataParser(){
    	//create and initialize the distribution network model
    	if(this.distNet == null){
			 this.distNet = ThreePhaseObjectFactory.create3PhaseDStabNetwork();
			 this.distNet.setNetworkType(NetworkType.DISTRIBUTION);
		}
    	
    	this.lineCodeParser = new  OpenDSSLineCodeParser (this);
    	this.lineParser = new  OpenDSSLineParser (this);
    	this.loadParser = new  OpenDSSLoadParser (this);
    	this.capParser =  new  OpenDSSCapacitorParser (this);
    	this.xfrParser =  new  OpenDSSTransformerParser (this);
    	//TODO tentatively, treat regulator as a fixed tap transformer
    	this.regulatorParser = new  OpenDSSRegulatorParser(this); 
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
	
	
	
 	public OpenDSSLineCodeParser getLineCodeParser() {
		return lineCodeParser;
	}

	public OpenDSSLineParser getLineParser() {
		return lineParser;
	}

	public OpenDSSLoadParser getLoadParser() {
		return loadParser;
	}

	public OpenDSSTransformerParser getXfrParser() {
		return xfrParser;
	}

	public OpenDSSCapacitorParser getCapacitorParser() {
		return capParser;
	}

	public OpenDSSRegulatorParser getRegulatorParser() {
		return regulatorParser;
	}

	public boolean parseFeederData(String folderPath,String feederFile){
    	 
    	 boolean no_error = true;
    	 
    	 //parse the master file, first create an network object as well as the sourceBus
			
		 String str ="", nextLine = "";
	     int lineCnt = 0;
	     boolean useLastLineString = false;
	     
	     List<String> redirectFiles  = new ArrayList<>();
	     
    	 if(!feederFile.equals("")){
    		 try {
    			    String fullFilePath = folderPath+"\\"+feederFile;
 				    final File file = new File(fullFilePath);
    				final InputStream stream = new FileInputStream(file);
    				final BufferedReader din = new BufferedReader(new InputStreamReader(stream));
    				IFileReader reader = new ODMTextFileReader(din);
    				
    				ODMLogger.getLogger().info("Start to parse feeder file and create the parser object # " + fullFilePath);

    		    	do {
    		    		if(useLastLineString){
    		    			str = nextLine;
    		    			useLastLineString = false;
    		    		}
    		    		else{
    		    			str = reader.readLine(); 
    		                lineCnt++;
    		    		}
    		    		System.out.println("Parsing: "+str);
    		        	if (str != null && !str.trim().equals("")) {
    		        		str = str.trim();
    		        		if(str.startsWith("!") || str.startsWith("//")){
    		        			//bypass the comment
    		        		}
    		        		else if(str.startsWith("New")||str.startsWith("new")){	
    		        			
    		        			//Consider in-line comment using !
    		        			if(str.indexOf("!")>0){
    		        				str = str.substring(0, str.indexOf("!"));
    		        			}
    		        			
    		        			String[] tempAry = str.split("\\s+");
    		        			if(tempAry[1].contains("object=")||tempAry[1].contains("Object=")){
    		        				tempAry[1] = tempAry[1].substring(7);
    		        			}
    		        			if(tempAry[1].contains("Circuit.") ||tempAry[1].contains("circuit.")){
    		        				//create a network object and add the source bus
    		        				String circuitId = tempAry[1].substring(8);
    		        				this.getDistNetwork().setId(circuitId);
    		        				
    		        				String sourceBusId = "";
    		        				double basekv = 0.0;
    		        				double volt_pu = 1.0;
    		        				Bus3Phase sourceBus = null;
    		        				
    		        				if(!str.contains("basekv=")){
    		        				  nextLine  = reader.readLine().trim();
    		        				  lineCnt++;
    		        				  
    		        				  int continueIdx = nextLine.indexOf("~");
    		        				  if(continueIdx==0){
    		        					  nextLine = nextLine.substring(continueIdx+1).trim();
    		        					  String[] tempAry2 = nextLine.split("\\s+");
    		        					  
    		        					  for(int i=0;i<tempAry2.length;i++ ){
    		        						  if(tempAry2[i].contains("basekv=")){
    		        							  basekv = Double.valueOf(tempAry2[i].substring(7));
    		        						  }
    		        						  else if(tempAry2[i].contains("Bus1=")){
    		        							  sourceBusId = tempAry2[i].substring(4);
    		        						  }
    		        						  else if(tempAry2[i].contains("pu=")){
    		        							  volt_pu = Double.valueOf(tempAry2[i].substring(3));
    		        						  }
    		        					  }
    		        					  
    		        				  }
    		        				  else{
    		        					  
    		        					  throw new Error("basekv is not defined for the source bus and no contiunation data");
    		        				  }
    		        				  
    		        				  // create the source bus
    		        				  if(sourceBusId.length()>0){
    		        					  if(distNet.getBus(sourceBusId)==null)
    		        							sourceBus = ThreePhaseObjectFactory.create3PDStabBus(sourceBusId, distNet);
    		        					  
    		        					  sourceBus.setBaseVoltage(basekv, UnitType.kV);
    		        					  
    		        					  sourceBus.setVoltageMag(basekv);
    		        				  }
    		        				  else{
    		        					  throw new Error("source bus name is not properly parse! # "+ nextLine);
    		        				  }
    		        				}
    		        			}
    		        				
    		        			else if(tempAry[1].contains("Line.") ||tempAry[1].contains("line.")){
    		        				this.lineParser.parseLineData(str);
    		        			}
                                else if(tempAry[1].contains("Transformer.") ||tempAry[1].contains("transformer.")){
                                	String [] xfrStrAry = new String[3];
                                	xfrStrAry[0] = str;
                                	
                                	String[] nextStrAry = getNextDataInputString(reader);
                                	if(nextStrAry[0]==null){
                                		break;
                                	}
                                	nextLine = nextStrAry[0].trim();
                                	lineCnt = lineCnt + Integer.valueOf(nextStrAry[1]);
                                	
                                	if(nextLine.startsWith("~")){
                                		xfrStrAry[1] = nextLine;
                                		// by default there are three line strings for one transformer definition
                                		nextLine = reader.readLine().trim();
                                    	lineCnt++;
                                    	if(nextLine.startsWith("~")){
                                    		xfrStrAry[2] = nextLine;
                                    	}
                                    	else{
                                    		useLastLineString = true;
                                    	}
                                	}
                                	else{
                                		useLastLineString = true;
                                	}
                                	
                                	
    		        				if(useLastLineString) // if true, it means all xfr data is in one line
    		        					this.xfrParser.parseTransformerDataOneLine(str);
    		        				else
                                	   this.xfrParser.parseTransformerDataMultiLines(xfrStrAry);
    		        			}
    		        			else if(tempAry[1].contains("Load.") ||tempAry[1].contains("load.")){
    		        				this.loadParser.parseLoadData(str);
    		        			}
                                else if(tempAry[1].contains("Capacitor.") ||tempAry[1].contains("capacitor.")){
    		        				this.capParser.parseCapDataString(str);
    		        			}
                                
                                else{
                                	ODMLogger.getLogger().severe("Non-supported object for line # "+str);
                                }
    		        		}
    		        		else if(str.startsWith("redirect")||str.startsWith("Redirect")){
    		        			ODMLogger.getLogger().info(str);
    		        			String redictFileName = str.split("\\s+")[1];
    		        			if(redictFileName.toLowerCase().contains("linecode")){
    		        				no_error=this.lineCodeParser.parseLineCodeFile(folderPath+"\\"+redictFileName);
    		        			}
    		        			else{
    		        				no_error = no_error&&parseFile(folderPath,redictFileName);
    		        			}
    		        		}
    		        		else{
    		        			ODMLogger.getLogger().severe("Non-supported syntax/data model in line # "+lineCnt+"  :\n "+str);
    		        		}
    		        	}
    		    	}while(str!=null);
    		 } catch (Exception e) {
    			    ODMLogger.getLogger().severe("processing line #"+str);
    				ODMLogger.getLogger().severe(e.toString());
    				
    				e.printStackTrace();
    				return false;
    		}// end of try
    	  } // end of if file name is not empty
    	 
    	 
    	 
    		 
    	 
    	 
    	 //parse the lines, transformers, regulators, loads and capacitors
    	 
    	 
    	 boolean initFlag=initNetwork();
    	 
    	 no_error=no_error&initFlag;
    	 
    	 return no_error;
     }
     
     private boolean parseFile(String folderPath, String fileName){
    	 
    	 boolean no_error = true;
    	 String str ="", nextLine = "";
	     int lineCnt = 0;
	     boolean useLastLineString = true;
	     
	     List<String> redirectFiles  = new ArrayList<>();
	     
    	 if(!fileName.equals("")){
    		 try {
    			    String fullFilePath = folderPath+"\\"+fileName;
    				final File file = new File(fullFilePath);
    				final InputStream stream = new FileInputStream(file);
    				final BufferedReader din = new BufferedReader(new InputStreamReader(stream));
    				IFileReader reader = new ODMTextFileReader(din);
    				
    				ODMLogger.getLogger().info("Start to parse file: " + fullFilePath);
    				
    		    	do {
    		    		if(useLastLineString){
    		    			str = nextLine;
    		    			useLastLineString =false;
    		    		}
    		    		else{
    		    			str = reader.readLine(); 
    		                lineCnt++;
    		    		}
    		    		System.out.println("Parsing :" +str);
    		        	if (str != null && !str.trim().equals("")) {
    		        		str = str.trim();
    		        		if(str.startsWith("!") || str.startsWith("//")){
    		        			//bypass the comment
    		        		}
    		        		else if(str.startsWith("New")||str.startsWith("new")){	
    		        			String[] tempAry = str.split("\\s+");
    		        			if(tempAry[1].contains("object=")||tempAry[1].contains("Object=")){
    		        				tempAry[1] = tempAry[1].substring(7);
    		        			}
    		        			if(tempAry[1].contains("Circuit.") ||tempAry[1].contains("circuit.")){
    		        				//create a network object and add the source bus
    		        				String circuitId = tempAry[1].substring(8);
    		        				this.getDistNetwork().setId(circuitId);
    		        				
    		        				String sourceBusId = "";
    		        				double basekv = 0.0;
    		        				double volt_pu = 1.0;
    		        				Bus3Phase sourceBus = null;
    		        				
    		        				if(!str.contains("basekv=")){
    		        				  nextLine  = reader.readLine();
    		        				  lineCnt++;
    		        				  
    		        				  int continueIdx = nextLine.indexOf("~");
    		        				  if(continueIdx>0){
    		        					  nextLine = nextLine.substring(continueIdx+1);
    		        					  String[] tempAry2 = nextLine.split("\\s+");
    		        					  
    		        					  for(int i=0;i<tempAry2.length;i++ ){
    		        						  if(tempAry2[i].contains("basekv=")){
    		        							  basekv = Double.valueOf(tempAry2[i].substring(7));
    		        						  }
    		        						  else if(tempAry2[i].contains("Bus1=")){
    		        							  sourceBusId = tempAry2[i].substring(4);
    		        						  }
    		        						  else if(tempAry2[i].contains("pu=")){
    		        							  volt_pu = Double.valueOf(tempAry2[i].substring(3));
    		        						  }
    		        					  }
    		        					  
    		        				  }
    		        				  else{
    		        					  no_error = false;
    		        					  throw new Error("basekv is not defined for the source bus and no contiunation data");
    		        				  }
    		        				  
    		        				  // create the source bus
    		        				  if(sourceBusId.length()>0){
    		        					  if(distNet.getBus(sourceBusId)==null)
    		        							sourceBus = ThreePhaseObjectFactory.create3PDStabBus(sourceBusId, distNet);
    		        					  
    		        					  sourceBus.setBaseVoltage(basekv, UnitType.kV);
    		        					  
    		        					  sourceBus.setVoltageMag(basekv);
    		        				  }
    		        				  else{
    		        					  no_error = false;
    		        					  throw new Error("source bus name is not properly parse! # "+ nextLine);
    		        				  }
    		        				}
    		        			}
    		        				
    		        			else if(tempAry[1].contains("Line.") ||tempAry[1].contains("line.")){
    		        				this.lineParser.parseLineData(str);
    		        			}
                                else if(tempAry[1].contains("Transformer.") ||tempAry[1].contains("transformer.")){
                                	String [] xfrStrAry = new String[3];
                                	xfrStrAry[0] = str;
                                	
                                	
                                	String[] nextStrAry = getNextDataInputString(reader);
                                	if(nextStrAry[0]==null){
                                		break;
                                	}
                                	nextLine = nextStrAry[0].trim();
                                	lineCnt = lineCnt + Integer.valueOf(nextStrAry[1]);
                                	
                                	if(nextLine.startsWith("~")){
                                		xfrStrAry[1] = nextLine;
                                		// by default there are three line strings for one transformer definition
                                		nextLine = reader.readLine().trim();
                                    	lineCnt++;
                                    	if(nextLine.startsWith("~")){
                                    		xfrStrAry[2] = nextLine;
                                    	}
                                    	else{
                                    		useLastLineString = true;
                                    	}
                                	}
                                	else{
                                		useLastLineString = true;
                                	}
                                	
                                	
    		        				if(useLastLineString) // all data in one line
    		        					this.xfrParser.parseTransformerDataOneLine(str);
    		        				else
                                	   this.xfrParser.parseTransformerDataMultiLines(xfrStrAry);
    		        			}
    		        			else if(tempAry[1].contains("Load.") ||tempAry[1].contains("load.")){
    		        				this.loadParser.parseLoadData(str);
    		        			}
                                else if(tempAry[1].contains("Capacitor.") ||tempAry[1].contains("capacitor.")){
    		        				this.capParser.parseCapDataString(str);
    		        			}
                                
                                else{
                                	ODMLogger.getLogger().severe("Non-supported object for line # "+str);
                                }
    		        		}
    		        		else if(str.startsWith("redirect")||str.startsWith("Redirect")){
    		        			ODMLogger.getLogger().info(str);
    		        		}
    		        		else{
    		        			ODMLogger.getLogger().severe("Non-supported syntax/data model in line # "+lineCnt+"  :\n "+str);
    		        		}
    		        	}
    		    	}while(str!=null);
    		    	
    		    	ODMLogger.getLogger().info("End of parsing file: " + fullFilePath);
    		 } catch (Exception e) {
    			 
    			    ODMLogger.getLogger().severe("processing line #"+str);
    				ODMLogger.getLogger().severe(e.toString());
    				
    				e.printStackTrace();
    				return false;
    		}// end of try
    		 
    		 
    	  } // end of if file name is not empty
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
     
     /**
      * skip all the comment lines (including in-line comments and block comments) to get the next data input line string, so that processing can be performed to check
      * if the next data input string is a valid input or only comments
      * 
      * The first return string is the data string, while the second return string is the skip line numbers
      * @param din
      * @param useLastLineString
      * @return String[2] 
      * @throws ODMException 
      */
     private String[] getNextDataInputString(IFileReader reader) throws ODMException{
    	 String dataString = null;
    	 int skipLineNum = 0;
    	 do{
    		 dataString =reader.readLine();
    		 if(dataString!=null){
    			 dataString = dataString.trim().toLowerCase();
    			 if(dataString.trim().length()>0){
    				 if(dataString.startsWith("!") ||dataString.startsWith("//")){
    					 //it is a comment line, skip
    					 skipLineNum++;
    				 }
    				 else if(dataString.startsWith("/*")){
    					 //keep search until find the "*/", which denotes the end of the comment block.
    					 skipLineNum++;
    					 if(!dataString.contains("*/")){
    						 
	    					 do{
	    						 dataString =reader.readLine();
	    						 skipLineNum++;
	    						 
	    			    		 if(dataString!=null){
	  
	    			    			 if(dataString.contains("*/")){
	    			    				 break;
	    			    			 }
	    			    			
	    			    		 }
	    			    		 
	    					 }while(dataString !=null);
    					 }
    				 }
    				 else{
    					 // now we find the next non-comment data string;
    					 break;
    				 }
    			 }
    		 }
    		 
    	 }while(dataString !=null);
        
    	String[] returnStr =  new String[]{dataString,Integer.toString(skipLineNum)};
    	
    	return returnStr;		 
    		
     }
     
     public Branch3Phase getBranchByName(String branchName){
    	 for(Branch bra: this.getDistNetwork().getBranchList()){
    		 if(bra.getName().equals(branchName)){
    			 return (Branch3Phase) bra;
    		 }
    	 }
    	 return null;
     }
    
	
}




