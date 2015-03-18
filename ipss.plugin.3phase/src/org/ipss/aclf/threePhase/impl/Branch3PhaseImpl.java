package org.ipss.aclf.threePhase.impl;



import static org.ipss.aclf.threePhase.util.ThreePhaseUtilFunction.threePhaseXfrAptr;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.aclf.threePhase.Branch3Phase;
import org.ipss.aclf.threePhase.Transformer3Phase;

import com.interpss.core.acsc.impl.AcscBranchImpl;
import com.interpss.dstab.impl.DStabBranchImpl;

public class Branch3PhaseImpl extends DStabBranchImpl implements Branch3Phase{
   
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
		Complex3x3 yff = null;
		if(!isXfr())
			yff= this.getBranchYabc().add(this.getFromShuntYabc());
		else{
			Transformer3Phase ph3Xformer = this.to3PXformer();
			yff = ph3Xformer.getYffabc();
		}
	        
	    return yff;
	}

	@Override
	public Complex3x3 getYttabc() {
		Complex3x3 ytt = null;
		if(!isXfr())
		    ytt = this.getBranchYabc().add(this.getToShuntYabc());
		else{
			Transformer3Phase ph3Xformer = this.to3PXformer();
			ytt = ph3Xformer.getYttabc();
		}
	    
		return ytt;
	}

	@Override
	public Complex3x3 getYftabc() {
		Complex3x3 yft = null;
		if(!isXfr())
		    yft = this.getBranchYabc().mulitply(-1);
		else{
			Transformer3Phase ph3Xformer = this.to3PXformer();
			yft = ph3Xformer.getYftabc();
		}
	    
		return yft;
	}

	@Override
	public Complex3x3 getYtfabc() {
		Complex3x3 ytf = null;
		if(!isXfr())
		    ytf = this.getBranchYabc().mulitply(-1);
		else{
			Transformer3Phase ph3Xformer = this.to3PXformer();
			ytf = ph3Xformer.getYtfabc();
		}
	    
		return ytf;
	}

	@Override
	public Complex3x1 CurrentAbcIntoNetFromSide() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Complex3x1 CurrentAbcIntoNetToSide() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transformer3Phase to3PXformer() {
		
		return threePhaseXfrAptr.apply(this);
	}

}
