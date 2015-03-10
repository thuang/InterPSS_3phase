package org.ipss.aclf.threePhase;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;

import com.interpss.core.acsc.AcscGen;

public interface Gen3Phase extends AcscGen{
	
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
	
	public Complex3x1 getPowerAbc(UnitType unit);
	
	public  void setPowerAbc(Complex3x1 genPQAbc,UnitType unit);

}
