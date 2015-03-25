package org.ipss.dynamic.threePhase;

import org.ipss.aclf.threePhase.Network3Phase;

import com.interpss.dstab.DStabilityNetwork;

public interface DStabNetwork3Phase extends Network3Phase, DStabilityNetwork{
	

	
	
	public boolean initThreePhaseFromLfResult();
	
	public boolean init3PDstabNetwork();
	
	
	public boolean solve3PNetworkEquation();

}
