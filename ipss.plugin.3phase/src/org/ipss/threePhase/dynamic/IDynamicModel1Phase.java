package org.ipss.threePhase.dynamic;

import org.apache.commons.math3.complex.Complex;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Phase;

import com.interpss.core.net.NameTag;
import com.interpss.core.net.ref.BusRef;
import com.interpss.dstab.algo.IDynamicSimulation;
import com.interpss.dstab.device.DynamicDevice;

public interface IDynamicModel1Phase extends DynamicDevice {
	
	
	Phase getPhase();
	
	void  setPhase(Phase p);

}
