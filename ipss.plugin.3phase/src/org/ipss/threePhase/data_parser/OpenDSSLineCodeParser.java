package org.ipss.threePhase.data_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.ieee.odm.common.IFileReader;
import org.ieee.odm.common.ODMLogger;
import org.ieee.odm.common.ODMTextFileReader;
import org.ipss.threePhase.basic.LineConfiguration;

public class OpenDSSLineCodeParser {
	
	private OpenDSSDataParser dataParser = null;
	
	public OpenDSSLineCodeParser(OpenDSSDataParser parser){
		this.dataParser = parser;
	}
	
	public boolean  parseLineCode(String fileName) {
		
		
		try {
			final File file = new File(fileName);
			final InputStream stream = new FileInputStream(file);
			ODMLogger.getLogger().info("Parse input file and create the parser object, " + fileName);

			final BufferedReader din = new BufferedReader(new InputStreamReader(stream));
			IFileReader reader = new ODMTextFileReader(din);
			
	
			
			String str;   
	      	int busCnt = 0;
	      	String lineCodeId = "";
	      	int nphases = 0;
	      	int baseFreq = 60;
	      	
			String[] lineData = null;
			String code_id ="";
			String nphaseStr ="";
			String baseFreqStr ="";
			LineConfiguration lineConfig = null;
	      	
	    	do {
	          	str = din.readLine();   
	        	if (str != null && !str.trim().equals("")) {
	        		str = str.trim();
	        		if(str.startsWith("!")){
	        			//bypass the comment
	        		}
	        		else if(str.startsWith("New")){
	        			//tokenizer by blank
	        			//StringTokenizer st = new StringTokenizer(str);
	        			//while (st.hasMoreTokens()) {
	        			
	        			lineData = str.split("\\s");
	        			code_id =lineData[1];
	        			nphaseStr =lineData[2];
	        			baseFreqStr =lineData[3];
	        			
	        			
	        			// set lineCodeId, nphases and baseFreq
	        			lineCodeId = code_id.substring(code_id.indexOf(".")+1);
	        			
	        			nphases = Integer.valueOf(nphaseStr.substring(nphaseStr.indexOf("=")+1));
	        			
	        			baseFreq = Integer.valueOf(baseFreqStr.substring(baseFreqStr.indexOf("=")+1));
	        			
	        			lineConfig = new LineConfiguration();
	        			lineConfig.setId(lineCodeId);
	        			lineConfig.setNphases(nphases);
	        			this.dataParser.getLineConfigTable().put(lineCodeId, lineConfig);
	        			
	        			
	        		}
	        		else if(str.contains("rmatrix")){
	        			// get the matrix data within the brackets,
	        			
	        			// if it has "|", tokenizer by "|", otherwise it is only one phase data, need to check <nphases>
	        			
	        		}
                    else if(str.contains("xmatrix")){
                    	
                        // get the matrix data within the brackets,
	        			
	        			// if it has "|", tokenizer by "|", otherwise it is only one phase data, need to check <nphases>
	        			
                    	
                    	//since the Zmatrix will be created when processing the rmatrix, so here just update the imaginary part of the Zmatrix
	        			
	        		}
                    else if(str.contains("cmatrix")){
	        			
	        		}
	        		
	        		
	        		
	        	}
	        } while(str != null);
			
			
			return true;
		} catch (Exception e) {
			ODMLogger.getLogger().severe(e.toString());
		
			e.printStackTrace();
			return false;
		}
		
	}

}
