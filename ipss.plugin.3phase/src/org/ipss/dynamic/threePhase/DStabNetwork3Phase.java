package org.ipss.dynamic.threePhase;

import org.ipss.aclf.threePhase.Network3Phase;

import com.interpss.dstab.DStabilityNetwork;

public interface DStabNetwork3Phase extends Network3Phase, DStabilityNetwork{
	
	
	public boolean solveNetworkEquation();
	
	
	public boolean initialize4Dstab();

}
