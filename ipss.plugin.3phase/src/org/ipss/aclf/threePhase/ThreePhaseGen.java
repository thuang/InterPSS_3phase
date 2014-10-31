package org.ipss.aclf.threePhase;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.acsc.AcscGen;

public interface ThreePhaseGen extends AcscGen{
	
	public Complex3x3 getZabc();
	
	public Complex3x3 getYabc();
	
	public Complex3x1 getPowerAbc();

}
