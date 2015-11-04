package org.ipss.threePhase.basic.impl;



import static org.ipss.threePhase.util.ThreePhaseUtilFunction.threePhaseXfrAptr;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Transformer3Phase;

import com.interpss.dstab.impl.DStabBranchImpl;

public class Branch3PhaseImpl extends DStabBranchImpl implements Branch3Phase{
   
	private Complex3x3 Zabc =null;
	private Complex3x3 Yabc =null;
	private Complex3x3 fromShuntYabc =null;
	private Complex3x3 toShuntYabc =null;
	private Complex3x1 currInjAtFromBus = null;
	private Complex3x1 currInjAtToBus = null; 
	
	private Complex3x3 toBusVabc2FromBusVabcMatrix = null;
	private Complex3x3 toBusIabc2FromBusVabcMatrix = null;
	private Complex3x3 toBusVabc2FromBusIabcMatrix = null;
	private Complex3x3 toBusIabc2FromBusIabcMatrix = null;
	private Complex3x3 fromBusVabc2ToBusVabcMatrix = null;
	private Complex3x3 toBusIabc2ToBusVabcMatrix = null;
	
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
		if(this.toShuntYabc ==null){
			
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
		if(!isXfr()){
			yff= this.getBranchYabc();
		
			
			if(this.getFromShuntYabc()!=null)
		         yff = yff.add(this.getFromShuntYabc());
		}else{
			Transformer3Phase ph3Xformer = this.to3PXformer();
			yff = ph3Xformer.getYffabc();
		}
	        
	    return yff;
	}

	@Override
	public Complex3x3 getYttabc() {
		Complex3x3 ytt = null;
		if(!isXfr()){
			ytt = this.getBranchYabc();
			if(this.getToShuntYabc()!=null)
		         ytt = ytt.add(this.getToShuntYabc());
		}
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
		    yft = this.getBranchYabc().multiply(-1);
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
		    ytf = this.getBranchYabc().multiply(-1);
		else{
			Transformer3Phase ph3Xformer = this.to3PXformer();
			ytf = ph3Xformer.getYtfabc();
		}
	    
		return ytf;
	}

	@Override
	public Complex3x1 getCurrentAbcAtFromSide() {
		
		return this.currInjAtFromBus;
	}

	@Override
	public Complex3x1 getCurrentAbcAtToSide() {
		
		return this.currInjAtToBus;
	}

	@Override
	public Transformer3Phase to3PXformer() {
		
		return threePhaseXfrAptr.apply(this);
	}

	@Override
	public Complex3x3 getToBusVabc2FromBusVabcMatrix() {
		
		if(this.toBusVabc2FromBusVabcMatrix == null){
		    Complex3x3 U = Complex3x3.createUnitMatrix();
		    this.toBusVabc2FromBusVabcMatrix = U.add(this.getZabc().multiply(this.getToShuntYabc()));
		}
		return this.toBusVabc2FromBusVabcMatrix;
	}

	@Override
	public Complex3x3 getToBusIabc2FromBusVabcMatrix() {
		
		return this.toBusIabc2FromBusVabcMatrix = this.getZabc();
	}

	@Override
	public Complex3x3 getToBusVabc2FromBusIabcMatrix() {
		 if(this.toBusVabc2FromBusIabcMatrix ==null){
			 if(this.getFromShuntY()!=null && this.getToShuntY()!=null){
				 Complex3x3 shuntYabc = this.getFromShuntYabc().add(this.getToShuntYabc());
				 this.toBusVabc2FromBusIabcMatrix = shuntYabc.multiply(this.Zabc).multiply(shuntYabc);
				 this.toBusVabc2FromBusIabcMatrix = this.toBusVabc2FromBusIabcMatrix.multiply(1/4).add(shuntYabc);
			 }
			 else this.toBusVabc2FromBusIabcMatrix = new Complex3x3();
		 }
		return this.toBusVabc2FromBusIabcMatrix;
	}

	@Override
	public Complex3x3 getToBusIabc2FromBusIabcMatrix() {
		       if(this.toBusIabc2FromBusIabcMatrix == null)
		    	   this.toBusIabc2FromBusIabcMatrix = getToBusVabc2FromBusVabcMatrix();
		return this.toBusIabc2FromBusIabcMatrix;
	}

	@Override
	public Complex3x3 getFromBusVabc2ToBusVabcMatrix() {
		     if(this.fromBusVabc2ToBusVabcMatrix==null){
		    	 this.fromBusVabc2ToBusVabcMatrix = this.getToBusVabc2FromBusVabcMatrix().inv();
		     }
		return this.fromBusVabc2ToBusVabcMatrix;
	}

	@Override
	public Complex3x3 getToBusIabc2ToBusVabcMatrix() {
		     if(this.toBusIabc2ToBusVabcMatrix==null)
		    	 this.toBusIabc2ToBusVabcMatrix = this.getToBusVabc2FromBusVabcMatrix().inv().
		    	                                      multiply(getToBusIabc2FromBusVabcMatrix());
		return this.toBusIabc2ToBusVabcMatrix;
	}

	@Override
	public void setCurrentAbcAtFromSide(Complex3x1 IabcFromBus) {
		this.currInjAtFromBus = IabcFromBus;
		
	}

	@Override
	public void setCurrentAbcAtToSide(Complex3x1 IabcToBus) {
		this.currInjAtToBus = IabcToBus;
		
	}

}
