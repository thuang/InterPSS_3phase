package org.ipss.aclf.threePhase;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;

import com.interpss.core.acsc.AcscGen;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.mach.DynamicMachine;

public interface Gen3Phase extends DStabGen{
	
	/**
	 * directly set the generator 3x3 impedance matrix on machine MVA base
	 * @param genZAbc -generator 3x3 impedance matrix on machine MVA base
	 */ 
	public  void setZabc(Complex3x3 genZAbc) ;
	
	/**
	 * set the generator 3x3 impedance matrix using three-sequence impedances
	 * 
	 * @param z1  -generator positive sequence impedance on machine MVA base
	 * @param z2  -generator negative sequence impedance on machine MVA base
	 * @param z0  -generator zero sequence impedance on machine MVA base
	 */
	public  void setZabc(Complex z1, Complex z2,Complex z0) ;
	
	/**
	 * 
	 * @param machineMVABase   true for returning the value on machine base, otherwise, return it on system base
	 * @return
	 */
	public Complex3x3 getZabc(boolean machineMVABase);
	
	/**
	 * 
	 * @param machineMVABase   true for returning the value on machine base, otherwise, return it on system base
	 * @return
	 */
	public Complex3x3 getYabc(boolean machineMVABase);
	
	/**
	 * Power = VABC*conj(IgenABC-YgenABC*VABC)
	 * @param unit
	 * @return
	 */
	public Complex3x1 getPower3Phase(UnitType unit);
	
	public  void setPower3Phase(Complex3x1 genPQ,UnitType unit);
	
	public Complex3x1 getIgen3Phase();
	
	
	public Bus3Phase getParentBus();
	
	/**
	 *  Update the genPower and the negative sequence Pe
	 * @return
	 */
	public boolean updateStates();

}
