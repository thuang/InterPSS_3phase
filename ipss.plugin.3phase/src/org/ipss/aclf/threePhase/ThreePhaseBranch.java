package org.ipss.aclf.threePhase;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.acsc.AcscBranch;

public interface ThreePhaseBranch extends AcscBranch{
	
    public void setZabc(Complex3x3 Zabc);
    
    public void setZabc(Complex Z1, Complex Z2, Complex Z0);
	
	public Complex3x3 getZabc() ;
	
	public Complex3x3 getBranchYabc() ;
	
	public Complex3x3 getFromShuntYabc();
	public Complex3x3 getToShuntYabc();
	
	public void setFromShuntYabc( Complex3x3 fYabc) ;
	
	public void setToShuntYabc( Complex3x3 tYabc) ;
	

	public Complex3x3 getYffabc();
	
	public Complex3x3 getYttabc();
	
	

}
