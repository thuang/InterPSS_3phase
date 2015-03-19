package org.ipss.dynamic.threePhase.impl;

import static com.interpss.common.util.IpssLogger.ipssLogger;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.ipss.aclf.threePhase.Bus3Phase;
import org.ipss.dynamic.threePhase.Machine3Phase;

import com.interpss.common.exp.InterpssRuntimeException;
import com.interpss.core.net.Network;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.mach.DynamicMachine;
import com.interpss.dstab.mach.MachineData;
import com.interpss.dstab.mach.MachineType;
import com.interpss.dstab.mach.impl.DynamicMachineImpl;
import com.interpss.dstab.mach.impl.MachineImpl;

public class Machine3PhaseImpl implements Machine3Phase{
	
	private Bus3Phase parentBus3P = null;
	private DynamicMachine mach = null;

	private Complex3x1 igen3Ph = null;
	private Complex3x1 genPe3Ph = null;
	private Complex3x1 genQ3Ph = null;
	
	
	
	public Machine3PhaseImpl(DynamicMachine mach){
	    this.mach = mach;
	}

	@Override
	public Complex3x1 getIgen3Phase() {
		 // Complex3x1(a0,b1,c2)
		return this.igen3Ph = Complex3x1.z12_to_abc(new Complex3x1(new Complex(0,0),getMachine().getIgen(), new Complex(0,0)));
	}

	@Override
	public Complex3x1 getGenPe3Phase() {
		
		return this.genPe3Ph;
	}

	@Override
	public void setGenPe3Phase(Complex3x1 genPe) {
		this.genPe3Ph = genPe;

	}

	@Override
	public Complex3x1 getGenQ3Phase() {

		return this.genQ3Ph;
	}

	@Override
	public void setGenQ3Phase(Complex3x1 genQ) {
		this.genQ3Ph = genQ;

	}
	
	
	/**
	 * Pe_neg = Ig_neg^2*R2n
	 * @return
	 */
	private double getNegativeSeqPe(){
		double Pe_neg = 0;
		
		Complex z2 = this.getMachine().getParentGen().getNegGenZ();
		
		Complex v2 = parentBus3P.get3SeqVotlages().c_2;
		if(z2!=null && z2.abs()>0){
			if(z2.getReal()>this.getMachine().getRa()){
		     double Rr = (z2.getReal()-this.getMachine().getRa())*2;
		     Complex i2 = v2.divide(z2);
		     Pe_neg = i2.abs()*i2.abs()*Rr/2;
			}
			else
				throw new Error("Machine Negative sequence data Error: Real part of NegGenZ must be larger than Ra, as R2 = Ra+ Rr/2");
		}
		return Pe_neg;
	}

	@Override
	public DynamicMachine getMachine() {
		
		return this.mach;
	}

	@Override
	public void setMachine(DynamicMachine dynMach) {
		this.mach=dynMach;
		
	}

	@Override
	public Bus3Phase getParentBus3Phase() {
		
		return this.parentBus3P;
	}
	
}
