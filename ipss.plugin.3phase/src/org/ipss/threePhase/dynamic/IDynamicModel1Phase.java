package org.ipss.threePhase.dynamic;

import com.interpss.core.acsc.PhaseCode;
import com.interpss.dstab.device.DynamicBusDevice;

public interface IDynamicModel1Phase extends DynamicBusDevice {
	
	
	PhaseCode getPhase();
	
	void  setPhase(PhaseCode p);

}
