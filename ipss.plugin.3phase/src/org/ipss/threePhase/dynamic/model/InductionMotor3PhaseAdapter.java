package org.ipss.threePhase.dynamic.model;

import org.ipss.threePhase.dynamic.IDynamicModel3Phase;

import com.interpss.dstab.dynLoad.InductionMotor;

public interface InductionMotor3PhaseAdapter extends IDynamicModel3Phase {
	
	
	void setInductionMotor(InductionMotor indMotor);
	
	InductionMotor getInductionMotor();

}
