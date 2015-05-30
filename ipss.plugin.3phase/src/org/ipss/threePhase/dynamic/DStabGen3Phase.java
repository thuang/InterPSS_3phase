package org.ipss.threePhase.dynamic;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;

import com.interpss.core.aclf.ref.GenRef;
import com.interpss.dstab.DStabGen;

public interface DStabGen3Phase extends GenRef<DStabGen>{
	
	
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
	public  void setZ120(Complex z1, Complex z2,Complex z0) ;
	
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
	 * If unit = pu, it is on system MVA base
	 * @param unit
	 * @return
	 */
	public Complex3x1 getPower3Phase(UnitType unit);

	
	/**
	 * Calculate the three phase current injection at the terminal on system basis
	 * @return
	 */
	public Complex3x1 getIgen3Phase();
	
	public Complex3x1 getIinj2Network3Phase();
	
	

}
