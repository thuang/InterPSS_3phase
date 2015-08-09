package org.ipss.threePhase.util;

import java.util.function.Function;

import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Transformer3Phase;

import com.interpss.dstab.DStabGen;

import org.ipss.threePhase.basic.Gen3Phase;
import org.ipss.threePhase.dynamic.model.DStabGen3Phase;

public class ThreePhaseUtilFunction {
	
	
	public static Function<Branch3Phase, Transformer3Phase> threePhaseXfrAptr = bra -> {
		Transformer3Phase adpter = ThreePhaseObjectFactory.create3PXformer();
		adpter.set3PBranch(bra);
		return adpter;
	};
	
	public static Function<DStabGen, DStabGen3Phase> threePhaseGenAptr = gen -> {
		DStabGen3Phase adpter = ThreePhaseObjectFactory.create3PDynGenerator(gen.getId());
		adpter.setGen(gen);
		return adpter;
	};
	
	

}
