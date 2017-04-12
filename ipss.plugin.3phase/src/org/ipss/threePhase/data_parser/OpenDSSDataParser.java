package org.ipss.threePhase.data_parser;

import java.util.Hashtable;

import org.ipss.threePhase.basic.LineConfiguration;

public class OpenDSSDataParser {
	
	// Line configuration table
	protected Hashtable<String,LineConfiguration> lineConfigTable = null;

	public Hashtable<String, LineConfiguration> getLineConfigTable() {
		return lineConfigTable;
	}

	public void setLineConfigTable(Hashtable<String, LineConfiguration> lineConfigTable) {
		this.lineConfigTable = lineConfigTable;
	}
	
	

}
