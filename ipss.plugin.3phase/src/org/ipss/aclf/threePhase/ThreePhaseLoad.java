package org.ipss.aclf.threePhase;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.acsc.PhaseCode;

public interface ThreePhaseLoad extends AclfLoad {
	
	public Complex3x3 getEquivYabc();
	
	public Complex   getPhaseLoad(PhaseCode phase);
	
	public void  setPhaseLoad(Complex phaseLoad, PhaseCode phase);

}
