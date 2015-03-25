package org.ipss.threePhase.basic.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.LimitType;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Gen3Phase;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.interpss.common.exp.InterpssException;
import com.interpss.common.util.IpssLogger;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.acsc.AcscGen;
import com.interpss.core.acsc.BusScGrounding;
import com.interpss.core.acsc.impl.AcscGenImpl;
import com.interpss.core.net.DataCheckConfiguration;
import com.interpss.core.net.NameTag;
import com.interpss.dstab.impl.DStabGenImpl;
import com.interpss.dstab.mach.DynamicMachine;

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
			  return this.zAbc.mulitply(this.getZMultiFactor());
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
	    		  return this.yAbc.mulitply(1/this.getZMultiFactor());
	           
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
	

	@Override
	public Complex3x1 getIgen3Phase() {
		 // Complex3x1(a0,b1,c2)
		return this.igen3Ph = Complex3x1.z12_to_abc(new Complex3x1(new Complex(0,0),getMach().getIgen(), new Complex(0,0)));
	}

	
	
	/**
	 * Pe_neg = Ig_neg^2*R2n
	 * @return
	 */
	private double calcNegativeSeqPe(){
		double Pe_neg = 0;
		
		Complex z2 = this.getMach().getParentGen().getNegGenZ();
		
		Complex v2 = parentBus3P.get3SeqVotlages().c_2;
		if(z2!=null && z2.abs()>0){
			if(z2.getReal()>this.getMach().getRa()){
		     double Rr = (z2.getReal()-this.getMach().getRa())*2;
		     Complex i2 = v2.divide(z2);
		     Pe_neg = i2.abs()*i2.abs()*Rr/2;
			}
			else
				throw new Error("Machine Negative sequence data Error: Real part of NegGenZ must be larger than Ra, as R2 = Ra+ Rr/2");
		}
		return Pe_neg;
	}

	@Override
	public boolean updateStates() {
		
		this.getMach().setPe_NegSeq(calcNegativeSeqPe());
		
		//TODO udpate GenPowerAbc
		return true;
	}

	@Override
	public boolean initDStabMach() {
		
		boolean flag =true;
		
		this.mach.calMultiFactors();
		
		if(this.getMach() ==null){
			//TODO
			//convert the generation to equiv load;
			IpssLogger.getLogger().severe("Gen3Phase has no machine model, genId, busId: "+this.getId()+","+this.getParentBus().getId());
		}
		else
		    flag = this.getMach().initStates(this.getMach().getDStabBus());
		
		
		return flag;
	}


	
}

	

