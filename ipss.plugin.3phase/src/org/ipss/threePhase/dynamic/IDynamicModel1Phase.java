package org.ipss.threePhase.dynamic;

import org.apache.commons.math3.complex.Complex;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Phase;

import com.interpss.core.net.NameTag;
import com.interpss.core.net.ref.BusRef;
import com.interpss.dstab.algo.IDynamicSimulation;

public interface IDynamicModel1Phase extends NameTag, IDynamicSimulation {
	
	
	Phase getPhase();
	
	void  setPhase(Phase p);

}
