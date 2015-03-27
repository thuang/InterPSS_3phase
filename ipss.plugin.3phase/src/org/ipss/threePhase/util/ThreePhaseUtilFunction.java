package org.ipss.threePhase.util;

import java.util.function.Function;

import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Transformer3Phase;
import org.ipss.threePhase.dynamic.DStabGen3Phase;

import com.interpss.dstab.DStabGen;

public class ThreePhaseUtilFunction {
	
	
	public static Function<Branch3Phase, Transformer3Phase> threePhaseXfrAptr = bra -> {
		Transformer3Phase adpter = ThreePhaseObjectFactory.create3PXformer();
		adpter.set3PBranch(bra);
		return adpter;
	};
	
	public static Function<DStabGen, DStabGen3Phase> threePhaseGenAptr = gen -> {
		DStabGen3Phase adpter = ThreePhaseObjectFactory.create3PDynGenerator("");
		adpter.setGen(gen);
		return adpter;
	};
	
	

}
