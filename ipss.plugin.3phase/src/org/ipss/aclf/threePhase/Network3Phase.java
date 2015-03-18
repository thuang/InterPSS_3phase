package org.ipss.aclf.threePhase;

import org.interpss.numeric.sparse.ISparseEqnComplexMatrix3x3;

import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.acsc.AcscNetwork;
import com.interpss.core.acsc.BaseAcscNetwork;

public interface Network3Phase {
	
	
	public boolean initThreePhaseFromLfResult();
	
	
	public ISparseEqnComplexMatrix3x3 formYMatrixABC() throws Exception;
	
	
	public boolean run3PhaseLoadflow();
	
	

}
