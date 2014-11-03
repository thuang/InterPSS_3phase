package org.ipss.aclf.threePhase.util;

import java.util.function.Function;

import org.ipss.aclf.threePhase.ThreePhaseBranch;
import org.ipss.aclf.threePhase.ThreePhaseXformer;

public class ThreePhaseUtilFunction {
	
	
	public static Function<ThreePhaseBranch, ThreePhaseXformer> threePhaseXfrAptr = bra -> {
		ThreePhaseXformer adpter = ThreePhaseObjectFactory.create3PXformer();
		//adpter.setBranch(bra);
		return adpter;
	};
	
	

}
