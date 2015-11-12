package org.ipss.threePhase.powerflow.impl;

import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;

import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.BaseAclfNetwork;

public class DistPowerFlowOutFunc {
	
	public static String powerflowResultSummary(BaseAclfNetwork<? extends AclfBus,? extends AclfBranch> distNet){
		StringBuffer sb = new StringBuffer();
		
		sb.append("\n\n==================Distribtuion power flow results============\n\n");
		
		sb.append("Bus results: \n");
		for(AclfBus b:distNet.getBusList()){
			Bus3Phase bus = (Bus3Phase) b;
			sb.append(bus.getId()+","+bus.get3PhaseVotlages().toString()+"\n");
		}
		sb.append("\nBranch results: \n");
		for(AclfBranch bra:distNet.getBranchList()){
			Branch3Phase branch3P = (Branch3Phase) bra;
			sb.append(bra.getId()+", Iabc (from) = "+
			branch3P.getCurrentAbcAtFromSide().toString()+", Iabc (to) = "+ branch3P.getCurrentAbcAtToSide().toString()+"\n");
		}
		
		
		return sb.toString();
	}

}
