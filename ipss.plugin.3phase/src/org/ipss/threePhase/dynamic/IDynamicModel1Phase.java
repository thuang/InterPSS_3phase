package org.ipss.threePhase.dynamic;

import org.ipss.threePhase.basic.Phase;

import com.interpss.dstab.device.DynamicBusDevice;

public interface IDynamicModel1Phase extends DynamicBusDevice {
	
	
	Phase getPhase();
	
	void  setPhase(Phase p);

}
