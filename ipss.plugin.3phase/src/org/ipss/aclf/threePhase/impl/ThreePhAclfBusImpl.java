package org.ipss.aclf.threePhase.impl;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.aclf.threePhase.ThreePhAclfBus;

import com.interpss.core.aclf.impl.AclfBusImpl;

public class ThreePhAclfBusImpl extends AclfBusImpl implements ThreePhAclfBus{
	
	private Complex3x1 Vabc = null;

	@Override
	public Complex3x1 getPhaseVotlages() {
		
		return  Vabc;
	}

	@Override
	public void setPhaseVoltages(Complex3x1 vabc) {
		this.Vabc = vabc;
		
	}

	@Override
	public Complex3x3 getYiiAbc() {

        //
		
		
		return null;
	}

}
