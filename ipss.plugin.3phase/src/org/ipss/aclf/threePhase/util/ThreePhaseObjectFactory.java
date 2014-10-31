package org.ipss.aclf.threePhase.util;

import org.ipss.aclf.threePhase.ThreePhaseXformer;
import org.ipss.aclf.threePhase.impl.ThreePhaseXformerImpl;

public class ThreePhaseObjectFactory {
	
	public static ThreePhaseXformer create3PXformer(){
	   ThreePhaseXformer ph3Xfr = new ThreePhaseXformerImpl();
	   return ph3Xfr;
	}

}
