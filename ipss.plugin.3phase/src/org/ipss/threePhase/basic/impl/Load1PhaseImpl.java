package org.ipss.threePhase.basic.impl;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.DistLoadType;
import org.ipss.threePhase.basic.Load1Phase;
import org.ipss.threePhase.basic.LoadConnectionType;

import com.interpss.core.aclf.impl.AclfLoadImpl;
import com.interpss.core.acsc.PhaseCode;

public class Load1PhaseImpl extends AclfLoadImpl implements Load1Phase {
	
	DistLoadType loadType = DistLoadType.CONST_PQ; // by default constant PQ
	LoadConnectionType loadConnectType = LoadConnectionType.Single_Phase_Wye; // by default three-phase wye;
	double nominalKV = 0; 

	PhaseCode  ph = PhaseCode.A;  //connected phase(s)
	
	Complex3x3 equivYabc = new Complex3x3();
	
	Complex3x1 equivCurInj = null;
	

	@Override
	public void setPhaseCode(PhaseCode phCode) {
		this.ph = phCode;
		
	}

	@Override
	public PhaseCode getPhaseCode() {
		
		return this.ph;
	}
    
	@Override
	public Complex3x3 getEquivYabc() {
		
		return this.equivYabc;
	}

	@Override
	public Complex3x1 getEquivCurrInj(Complex3x1 vabc) {
		equivCurInj  = new Complex3x1();
		
		Bus3Phase bus = ((Bus3Phase)this.getParentBus());
		
		
		Complex vt = null;
		double vmag=1.0;
		switch (this.loadConnectType){
		  case Single_Phase_Wye:
			   if(this.ph==PhaseCode.A){
				   vt = vabc.a_0;
				   vmag = vt.abs();
				   equivCurInj.a_0=this.getLoad(vmag).divide(vt).conjugate().multiply(-1);
			   }
			   else if(this.ph==PhaseCode.B){
				   
				   vt = vabc.b_1;
				   vmag = vt.abs();
				   equivCurInj.b_1=this.getLoad(vmag).divide(vt).conjugate().multiply(-1);
			   }
			   else if(this.ph==PhaseCode.C){
				   
				   vt = vabc.c_2;
				   vmag = vt.abs();
				   equivCurInj.c_2=this.getLoad(vmag).divide(vt).conjugate().multiply(-1);
				   
			   }
			   else{
				   throw new Error("Connection type and phases are not consisent!! Bus, load id,connectType, phases: "
			          +bus.getId()+","+this.getId()+","+this.loadConnectType+","+this.ph);
			   }
			   
			   
			   break;
			   
			   /*
			    * phase1
			    *   0-------
			    *           |
			    *          load
			    * phase2    |
			    *   0-------
			    */
		  case Single_Phase_Delta:
			   if(this.ph==PhaseCode.AB){
				   vt=vabc.a_0.subtract(vabc.b_1);
				   vmag = vt.abs();
				   equivCurInj.a_0=this.getLoad(vmag).divide(vt).conjugate().multiply(-1);
				   equivCurInj.b_1=equivCurInj.a_0.multiply(-1);
				   
			   }
			   else if(this.ph==PhaseCode.BC){
				   vt = vabc.b_1.subtract(vabc.c_2);
				   vmag = vt.abs();
				   equivCurInj.b_1=this.getLoad(vmag).divide(vt).conjugate().multiply(-1);
				   equivCurInj.c_2=equivCurInj.b_1.multiply(-1);
			   }
			   else if(this.ph==PhaseCode.AC){
				   vt = vabc.a_0.subtract(vabc.c_2);
				   vmag = vt.abs();
				   equivCurInj.a_0=this.getLoad(vmag).divide(vt).conjugate().multiply(-1);
				   equivCurInj.c_2=equivCurInj.a_0.multiply(-1);
			   }
			   else{
				   throw new Error("Connection type and phases are not consisent!! Bus, load id,connectType, phases: "
			          +bus.getId()+","+this.getId()+","+this.loadConnectType+","+this.ph);
			   }
			  
			   break;
			  
		  default:
			  throw new Error("No supported load connection type:"+this.loadConnectType);
			  
		}
		
		
		return equivCurInj;
	}

	@Override
	public void setLoadModelType(DistLoadType loadModelType) {
		this.loadType = loadModelType;
		
	}

	@Override
	public DistLoadType getLoadModelType() {
		
		return this.loadType;
	}

	@Override
	public void setLoadConnectionType(LoadConnectionType loadConnectType) {
		this.loadConnectType = loadConnectType;
		
	}

	@Override
	public LoadConnectionType getLoadConnectionType() {
		
		return this.loadConnectType;
	}

	@Override
	public void setNominalKV(double ratedkV) {
		this.nominalKV = ratedkV;
		
	}

	@Override
	public double getNominalKV() {
		
		return this.nominalKV;
	}

}