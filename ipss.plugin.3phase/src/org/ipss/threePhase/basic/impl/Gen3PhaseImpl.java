package org.ipss.threePhase.basic.impl;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Gen3Phase;

import com.interpss.dstab.impl.DStabGenImpl;

public class Gen3PhaseImpl extends DStabGenImpl implements Gen3Phase {
	
	private Complex3x3   zAbc = null;
	private Complex3x3   yAbc = null;
	private Complex3x1   puPowerAbc = null;
	
	private Bus3Phase parentBus3P = null;

	private Complex3x1 igen3Ph = null;


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public void setZabc(Complex3x3 genZAbc) {
		this.zAbc = genZAbc;
		
	}

	@Override
	public void setZabc(Complex z1, Complex z2, Complex z0) {
		 this.zAbc = new Complex3x3(z1,z2,z0).ToAbc();
		
	}


	@Override
	public Complex3x3 getZabc(boolean machineMVABase) {
		if(this.zAbc == null)
			if(this.getPosGenZ()!=null && this.getZeroGenZ()!=null){
				setZabc(this.getPosGenZ(),this.getNegGenZ(),this.getZeroGenZ());
			}
		if(!machineMVABase) 
			  return this.zAbc.multiply(this.getZMultiFactor());
		return this.zAbc;
	}
	
	

	@Override
	public Complex3x3 getYabc(boolean machineMVABase) {
		
	     if(yAbc==null && getZabc(true)!=null)
			 yAbc=getZabc(true).inv();
		
	     if(yAbc!=null) 
	    	   if(machineMVABase)
				  return yAbc;
	    	   else
	    		  return this.yAbc.multiply(1/this.getZMultiFactor());
	           
		return null;
	}
	
	

	@Override
	public Complex3x1 getPower3Phase(UnitType unit) {
		switch(unit){
		 case PU: return this.puPowerAbc;
		 case mVA: return this.puPowerAbc.multiply(this.getMvaBase()/3.0);
		 case kVA: return this.puPowerAbc.multiply(this.getMvaBase()*1000.0/3.0);
		 default: try {
				throw new Exception("The unit should be PU, mVA or kVA");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.puPowerAbc;
	}
	
	@Override
	public void setPower3Phase(Complex3x1 genPQ,UnitType unit) {
		
		switch(unit){
		 case PU:   this.puPowerAbc =   genPQ; break;
		 case mVA:  this.puPowerAbc =   genPQ.multiply(3.0/this.getMvaBase()); break;
		 case kVA:  this.puPowerAbc =   genPQ.multiply(3.0/1000.0/this.getMvaBase());break;
		 default: try {
				throw new Exception("The unit should be PU, mVA or kVA");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
	

	public Bus3Phase getParentBus(){
		if(super.getParentBus() instanceof Bus3Phase)
		     return (Bus3Phase) super.getParentBus();
		else
			try {
				throw new Exception("The parent bus is not a Bus3Phase");
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			return null;
	}
	


	
}

	

