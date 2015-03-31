package org.ipss.threePhase.dynamic.impl;

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
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.dynamic.DStabGen3Phase;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.net.DataCheckConfiguration;
import com.interpss.core.net.NameTag;
import com.interpss.core.net.impl.NameTagImpl;
import com.interpss.dstab.DStabGen;

public class DStabGen3PhaseImpl extends NameTagImpl implements DStabGen3Phase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private  DStabGen dynGen =null;
	private Complex3x3   zAbc = null;
	private Complex3x1   puPowerAbc = null;
	
	@Override
	public DStabGen getGen() {
		
		return this.dynGen;
	}

	@Override
	public void setGen(DStabGen gen) {
		this.dynGen = gen;
		
	}

	
	@Override
	public void setZabc(Complex3x3 genZAbc) {
		Complex3x3 genZ120 = genZAbc.To120();
		this.dynGen.setPosGenZ(genZ120.aa);
		this.dynGen.setNegGenZ(genZ120.bb);
		this.dynGen.setZeroGenZ(genZ120.cc);
		
	}

	@Override
	public void setZ120(Complex z1, Complex z2, Complex z0) {
		this.dynGen.setPosGenZ(z1);
		this.dynGen.setNegGenZ(z2);
		this.dynGen.setZeroGenZ(z0);
		
	}


	@Override
	public Complex3x3 getZabc(boolean machineMVABase) {
		this.zAbc = null;
		if(this.dynGen.getPosGenZ()!=null && this.dynGen.getZeroGenZ()!=null){
			this.zAbc = new Complex3x3(this.dynGen.getPosGenZ(),this.dynGen.getNegGenZ(),this.dynGen.getZeroGenZ()).ToAbc();
		}
		if(!machineMVABase) 
			  return this.zAbc.mulitply(this.dynGen.getZMultiFactor());
		return this.zAbc;
	}
	
	

	@Override
	public Complex3x3 getYabc(boolean machineMVABase) {
		return getZabc(machineMVABase).inv();
	}
	
	@Override
	public Complex3x1 getPower3Phase(UnitType unit) {
		
		//Power = VABC*conj(IgenABC-YgenABC*VABC)
		// pu on system mva base
		Complex3x1 Vabc = ((Bus3Phase)this.dynGen.getParentBus()).get3PhaseVotlages();
		Complex3x1 IinjABC =  getIgen3Phase().subtract(getYabc(false).mulitply(Vabc)); 
		this.puPowerAbc.a_0 = Vabc.a_0.multiply(IinjABC.a_0.conjugate());
		this.puPowerAbc.b_1 = Vabc.b_1.multiply(IinjABC.b_1.conjugate());
		this.puPowerAbc.c_2 = Vabc.c_2.multiply(IinjABC.c_2.conjugate());
		
		double MVABASE =   this.dynGen.getParentBus().getNetwork().getBaseMva();
		switch(unit){
		 case PU: return this.puPowerAbc;
		 case mVA: 
			 return this.puPowerAbc.multiply(MVABASE);
		 case kVA: return this.puPowerAbc.multiply(MVABASE *1000.0);
		 default: try {
				throw new Exception("The unit should be PU, mVA or kVA");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.puPowerAbc;
	}
	
	

	@Override
	public Complex3x1 getIgen3Phase() {
		 // Complex3x1(a0,b1,c2)
		return Complex3x1.z12_to_abc(new Complex3x1(new Complex(0,0),this.dynGen.getMach().getIgen(), new Complex(0,0)));
	}

	
	
}
