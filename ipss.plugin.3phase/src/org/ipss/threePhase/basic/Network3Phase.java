package org.ipss.threePhase.basic;

import org.interpss.numeric.sparse.ISparseEqnComplexMatrix3x3;

public interface Network3Phase {
	

	
	
	public ISparseEqnComplexMatrix3x3 formYMatrixABC() throws Exception;
	
	public ISparseEqnComplexMatrix3x3 getYMatrixABC();
	
	
	public boolean run3PhaseLoadflow();
	
	

}
