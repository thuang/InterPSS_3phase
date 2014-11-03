package org.ipss.aclf.threePhase.impl;

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
import org.ipss.aclf.threePhase.ThreePhaseGen;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.acsc.AcscGen;
import com.interpss.core.acsc.BusScGrounding;
import com.interpss.core.acsc.impl.AcscGenImpl;
import com.interpss.core.net.DataCheckConfiguration;
import com.interpss.core.net.NameTag;

public class ThreePhaseGenImpl extends AcscGenImpl implements ThreePhaseGen {
	
	private Complex3x3   zAbc = null;
	private Complex3x3   yAbc = null;
	private Complex3x1   puPowerAbc = null;

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
	public Complex3x1 getPowerAbc(UnitType unit) {
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
	public void setPowerAbc(Complex3x1 genPQAbc,UnitType unit) {
		
		switch(unit){
		 case PU:   this.puPowerAbc =   genPQAbc; break;
		 case mVA:  this.puPowerAbc =   genPQAbc.multiply(3.0/this.getMvaBase()); break;
		 case kVA:  this.puPowerAbc =   genPQAbc.multiply(3.0/1000.0/this.getMvaBase());break;
		 default: try {
				throw new Exception("The unit should be PU, mVA or kVA");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}



	
}

	

