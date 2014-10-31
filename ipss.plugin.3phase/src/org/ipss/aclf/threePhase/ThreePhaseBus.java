package org.ipss.aclf.threePhase;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.acsc.AcscBus;

public interface ThreePhaseBus extends AcscBus {
	
	public Complex3x1 getPhaseVotlages();
	
    public void setPhaseVoltages(Complex3x1 vabc);
    
    
    public Complex3x3 getYiiAbc();
    
    

}
