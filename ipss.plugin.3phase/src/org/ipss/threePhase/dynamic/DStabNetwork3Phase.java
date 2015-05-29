package org.ipss.threePhase.dynamic;

import java.util.Hashtable;

import org.interpss.numeric.datatype.Complex3x1;
import org.ipss.threePhase.basic.Network3Phase;

import com.interpss.dstab.DStabilityNetwork;

public interface DStabNetwork3Phase extends Network3Phase, DStabilityNetwork{
	

	
	/**
	 * initialize the three-phase DStab network based on the positive sequence power flow result, 
	 * assuming the three-phase are well-balanced. Specially, the phase shift involved in 
	 * transformers modeled in three-phase  while neglected by the positive sequence modeling has been considered. 
	 * @return true if there is no error
	 */
	public boolean initThreePhaseFromLfResult();
	
	
	/**
	 * This function is to enable the three-phase network object to 
	 * solve the positive sequence network; this requires the three-phase
	 * network object is created with three-sequence data. 
	 * @return
	 */
	public boolean solvePosSeqNetEqn();
	
	public boolean initPosSeqDStabNet();
	
	public Hashtable<String, Complex3x1> get3phaseCustomCurrInjTable();
	
	public void set3phaseCustomCurrInjTable(Hashtable<String, Complex3x1> new3PhaseCurInjTable);

}
