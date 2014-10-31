package org.ipss.aclf.threePhase.impl;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.aclf.threePhase.ThreePhaseBranch;
import org.ipss.aclf.threePhase.ThreePhaseXformer;

import com.interpss.core.acsc.adpter.impl.AcscXformerImpl;

public class ThreePhaseXformerImpl extends AcscXformerImpl implements ThreePhaseXformer{

	private ThreePhaseBranch ph3Branch= null;
	
	private Complex y0 =null;
	private Complex y1 =null;
	
	public ThreePhaseXformerImpl(ThreePhaseBranch threePhBranch){
		
	}
	
	
	@Override
	public void setZabc(Complex3x3 Zabc) {
		this.ph3Branch.setZabc(Zabc);
		
	}

	@Override
	public void setZabc(Complex Z1, Complex Z2, Complex Z0) {
		this.ph3Branch.setZabc(Z1,Z2,Z0);
		
	}

	@Override
	public Complex3x3 getZabc() {
		
		return this.ph3Branch.getZabc();
	}

	@Override
	public Complex3x3 getYabc() {
		
		return this.ph3Branch.getBranchYabc();
	}

	@Override
	public Complex3x3 getYffabc() {
		// TODO
		// using look up table to build the Yff and Y
		
		///
		
		///
		
		return null;
	}

	@Override
	public Complex3x3 getYttabc() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Refer to the paper: M.S.Chen, W.E.Dillon, "Power system modeling," Proc. of IEEE,Vol.62, No.7, 1974
	 * 
	 * 
	 * Y1 corresponding to self and mutual admittance on the Yn side
	 * It is symmetric with :
	 *     Y1ii = (y0+2y1)/3
	 *     Y1ij = (y0-y1)/3
	 * @return
	 */
	private  Complex3x3  getY1(){
		
		if(y1 != null){
			if(y0 == null) y0 = y1;
			Complex Y1ii = (y0.add(y1.multiply(2))).divide(3);
			Complex Y1ij = (y0.subtract(y1)).divide(3);
			return new Complex3x3(Y1ii,Y1ij);
		}
		return null;
	}
	
	
	/**
	 * Y2 corresponding to self and mutual admittance on the Y or delta side
	 * It is symmetric with :
	 *     Y1ii = (2y1)/3
	 *     Y1ij = (-y1)/3
	 * @return
	 */
    private  Complex3x3  getY2(){
    	
    	if(y1 != null){
	    	Complex Y1ii = (y1.multiply(2)).divide(3);
			Complex Y1ij = (y1.multiply(-1)).divide(3);
			return new Complex3x3(Y1ii,Y1ij);
	    }
    	return null;
	}
    
    
	/**
	 * Y3 corresponding to mutual admittance of the Y and delta connections
	 * It is  with the structure: 
	 *              [-y1 y1  0]
	 *    1/sqrt(3)*[0 -y1, y1]
	 *              [y1, 0,-y1]
	 * @return
	 */
    private  Complex3x3  getY3(){
    	if(y1 != null)
    	return new Complex3x3(new Complex[][]{
    			{y1.multiply(-1/Math.sqrt(3)),     y1.divide(Math.sqrt(3)),        new Complex(0.0, 0.0)},
    			{new Complex(0,0),                y1.multiply(-1/Math.sqrt(3)),    y1.divide(Math.sqrt(3)) },
    			{ y1.divide(Math.sqrt(3)),            new Complex(0,0),            y1.multiply(-1/Math.sqrt(3))}});
    	else
    		return null;
    	
   	}

}
