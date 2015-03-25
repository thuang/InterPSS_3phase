package org.ipss.threePhase.basic;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.acsc.AcscBus;
import com.interpss.dstab.DStabBus;

public interface Bus3Phase extends DStabBus {
	
	public Complex3x1 get3PhaseVotlages();
	
    public void set3PhaseVoltages(Complex3x1 vabc);
    
	public Complex3x1 get3SeqVotlages();
	
    public void set3SeqVoltages(Complex3x1 v120);
    
    
    public Complex3x3 getYiiAbc();
    
    

}
