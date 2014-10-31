package org.ipss.aclf.threePhase;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.acsc.PhaseCode;

public interface ThreePhaseLoad extends AclfLoad {
	
	/**
	 * get three phase load equivalent 3x3 admittance matrix in pu
	 * @return
	 */
	public Complex3x3 getEquivYabc();
	
	/**
	 *  get three phase loads in pu
	 * @return
	 */
	public Complex3x1  get3PhaseLoad();
	
	
	/**
	 * 
	 * @param threePhaseLoad  three phase loads in pu
	 * @return
	 */
	public void  set3PhaseLoad(Complex3x1 threePhaseLoad);
	
	
	/**
	 * 
	 * @param phase
	 * @return
	 */
	public Complex   getPhaseLoad(PhaseCode phase);
	
	public void  setPhaseLoad(Complex phaseLoad, PhaseCode phase);

}
