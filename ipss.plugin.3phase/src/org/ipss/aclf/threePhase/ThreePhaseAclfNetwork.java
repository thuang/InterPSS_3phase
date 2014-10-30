package org.ipss.aclf.threePhase;

import org.interpss.numeric.sparse.ISparseEqnMatrix3x3;

import com.interpss.core.aclf.AclfNetwork;

public interface ThreePhaseAclfNetwork extends AclfNetwork {
	
	
	public boolean initThreePhaseFromLfResult();
	
	
	public ISparseEqnMatrix3x3 formYabc();
	
	
	public boolean run3PhaseLoadflow();

}
