package org.ipss.threePhase.dynamic;

import org.apache.commons.math3.complex.Complex;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Phase;

import com.interpss.core.net.ref.BusRef;
import com.interpss.dstab.algo.IDynamicSimulation;

public interface IDynamicModel1Phase extends BusRef<Bus3Phase>,IDynamicSimulation {
	
	
	Complex getEquivY(boolean modelMVABase);
	
	Phase getPhase();
	
	void  setPhase(Phase p);

}
