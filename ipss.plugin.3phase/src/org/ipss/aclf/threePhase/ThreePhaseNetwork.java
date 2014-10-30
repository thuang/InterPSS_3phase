package org.ipss.aclf.threePhase;

import org.interpss.numeric.sparse.ISparseEqnMatrix3x3;

import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.acsc.AcscNetwork;

public interface ThreePhaseNetwork extends AcscNetwork {
	
	
	public boolean initThreePhaseFromLfResult();
	
	
	public ISparseEqnMatrix3x3 formYabc();
	
	
	public boolean run3PhaseLoadflow();

}
