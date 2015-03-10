package org.ipss.aclf.threePhase;

import org.interpss.numeric.sparse.ISparseEqnComplexMatrix3x3;

import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.acsc.AcscNetwork;

public interface Network3Phase extends AcscNetwork {
	
	
	public boolean initThreePhaseFromLfResult();
	
	
	public ISparseEqnComplexMatrix3x3 formYabc() throws Exception;
	
	
	public boolean run3PhaseLoadflow();

}
