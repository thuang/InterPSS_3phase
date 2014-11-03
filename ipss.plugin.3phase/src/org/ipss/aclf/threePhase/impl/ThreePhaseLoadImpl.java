package org.ipss.aclf.threePhase.impl;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.aclf.threePhase.ThreePhaseLoad;

import com.interpss.core.aclf.impl.AclfLoadImpl;
import com.interpss.core.acsc.PhaseCode;

public class ThreePhaseLoadImpl extends AclfLoadImpl implements ThreePhaseLoad {

	Complex3x1 ph3Load = new Complex3x1();
	Complex3x3 equivYabc = new Complex3x3();
	
	@Override
	public Complex3x3 getEquivYabc() {
		double v = this.getParentBus().getVoltageMag();
		equivYabc.aa = ph3Load.a_0.conjugate().divide(v*v);
		equivYabc.bb = ph3Load.b_1.conjugate().divide(v*v);
		equivYabc.cc = ph3Load.c_2.conjugate().divide(v*v);
		
		return equivYabc;
	}

	@Override
	public Complex getPhaseLoad(PhaseCode phase) {
		
		switch(phase){
			case A: return ph3Load.a_0;
			case B: return ph3Load.b_1;
			case C: return ph3Load.c_2;
		}
		
		return null;
	}

	@Override
	public void setPhaseLoad(Complex phaseLoad, PhaseCode phase) {
		switch(phase){
		case A:  ph3Load.a_0 =phaseLoad;
		case B:  ph3Load.b_1 =phaseLoad;
		case C:  ph3Load.c_2 =phaseLoad;
	}

	}

	@Override
	public Complex3x1 get3PhaseLoad() {
		return ph3Load;
	}

	@Override
	public void set3PhaseLoad(Complex3x1 threePhaseLoad) {
		
		 ph3Load = threePhaseLoad;
	}

}
