package org.ipss.threePhase.basic;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.acsc.PhaseCode;

public interface Load1Phase extends AclfLoad {
	
	public void setPhaseCode (PhaseCode phCode);
	
	public PhaseCode getPhaseCode ();
	
	/**
	 * The Norton equivalent admittance. Used in power flow and short circuit analysis. 
	 * @return 
	 */
	public Complex3x3 getEquivYabc();
	
	/**
	 * calculate the equivalent current injection of the load, mainly used in power flow solution.
	 * @param vabc
	 * @return
	 */
	public Complex3x1 getEquivCurrInj(Complex3x1 vabc);
	
	/**
	 * set the load model type, such as constant PQ, constant current, constant impedance, etc.
	 * @param loadModelType
	 */
	public void setLoadModelType(DistLoadType loadModelType);
	
	/**
	 * get the load model type, such as constant PQ, constant current, constant impedance, etc.
	 * @return
	 */
	public DistLoadType getLoadModelType();
	
	/**
	 * set the load connection type. The connection type could be single phase wye, single phase delta, three phase wye and three phase delta.
	 * @param loadConnectType
	 */
	public void setLoadConnectionType(LoadConnectionType loadConnectType);
	
	/**
	 * get the load connection type. The connection type could be single phase wye, single phase delta, three phase wye and three phase delta.
	 * @return
	 */
	public LoadConnectionType getLoadConnectionType();
	
	/**
	 * set the nominal KV level of the load
	 * @param ratedkV
	 */
	public void setNominalKV(double ratedkV);
	
	/**
	 * return the nominal KV level of the load
	 * @return
	 */
	public double getNominalKV();

}
