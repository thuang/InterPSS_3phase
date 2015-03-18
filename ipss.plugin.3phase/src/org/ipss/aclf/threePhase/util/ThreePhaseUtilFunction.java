package org.ipss.aclf.threePhase.util;

import java.util.function.Function;

import org.ipss.aclf.threePhase.Branch3Phase;
import org.ipss.aclf.threePhase.Transformer3Phase;

public class ThreePhaseUtilFunction {
	
	
	public static Function<Branch3Phase, Transformer3Phase> threePhaseXfrAptr = bra -> {
		Transformer3Phase adpter = ThreePhaseObjectFactory.create3PXformer();
		adpter.set3PBranch(bra);
		return adpter;
	};
	
	

}
