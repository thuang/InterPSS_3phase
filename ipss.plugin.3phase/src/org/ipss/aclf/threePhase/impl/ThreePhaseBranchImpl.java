package org.ipss.aclf.threePhase.impl;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.aclf.threePhase.ThreePhaseBranch;

import com.interpss.core.acsc.impl.AcscBranchImpl;

public class ThreePhaseBranchImpl extends AcscBranchImpl implements ThreePhaseBranch{
   
	private Complex3x3 Zabc =null;
	private Complex3x3 Yabc =null;
	private Complex3x3 fromShuntYabc =null;
	private Complex3x3 toShuntYabc =null;
	
	private static final double z0_to_z1_ratio = 2.5;
	
	@Override
	public void setZabc(Complex3x3 Zabc) {
		this.Zabc = Zabc;
		
	}

	@Override
	public void setZabc(Complex Z1, Complex Z2, Complex Z0) {
		this.Zabc= new Complex3x3(Z1,Z2,Z0).ToAbc();
		
	}

	@Override
	public Complex3x3 getZabc() {
		// if Zabc is not set, initialize it from the three-sequence impedances
		if(Zabc ==null){
			if(this.getZ0()!=null)
				setZabc(getZ(),getZ(),getZ0());
			else
				setZabc(getZ(),getZ(),getZ().multiply(z0_to_z1_ratio));
		}
		
		return this.Zabc;
	}

	@Override
	public Complex3x3 getBranchYabc() {
		if(this.Yabc ==null) Yabc= getZabc().inv();
		return this.Yabc;
	}

	@Override
	public Complex3x3 getFromShuntYabc() {
		// if fromShuntY is not provided, get it from the sequence network
		if(this.fromShuntYabc ==null){
			
			//Ys = (2*Y1+Y0)/3
			Complex Ys = (this.getHShuntY().multiply(2).add(new Complex(0,this.getHB0()))).divide(3);
		    
			//Ym = (Y1-Y0)/3
			
			Complex Ym = (this.getHShuntY().subtract(new Complex(0,this.getHB0()))).divide(3);
			
			this.fromShuntYabc = new Complex3x3(Ys, Ym.multiply(-1));
		
		}
		
		return this.fromShuntYabc;
	}

	@Override
	public Complex3x3 getToShuntYabc() {
		
		// if toShuntY is not provided, get it from the sequence network
		if(this.fromShuntYabc ==null){
			
			//Ys = (2*Y1+Y0)/3
			Complex Ys = (this.getHShuntY().multiply(2).add(new Complex(0,this.getHB0()))).divide(3);
		    
			//Ym = (Y1-Y0)/3
			
			Complex Ym = (this.getHShuntY().subtract(new Complex(0,this.getHB0()))).divide(3);
			
			this.toShuntYabc = new Complex3x3(Ys, Ym.multiply(-1));
		
		}
		
		return this.toShuntYabc;
	}

	@Override
	public void setFromShuntYabc(Complex3x3 fYabc) {
		this.fromShuntYabc = fYabc;
		
	}

	@Override
	public void setToShuntYabc(Complex3x3 tYabc) {
		this.toShuntYabc = tYabc;
		
	}

	@Override
	public Complex3x3 getYffabc() {
		
		return this.getBranchYabc().add(this.getFromShuntYabc());
	}

	@Override
	public Complex3x3 getYttabc() {
		
		return this.getBranchYabc().add(this.getToShuntYabc());
	}

}
