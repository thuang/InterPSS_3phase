package org.ipss.threePhase.util;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.Bus3Phase;

public class ThreeSeqLoadProcessor {
	
	public static Complex3x3 getEquivLoadYabc(Bus3Phase bus){
		Complex loadPQ =bus.getLoadPQ();
		double v2 = bus.getVoltageMag()*bus.getVoltageMag();
		Complex loadEquivY1 = loadPQ.conjugate().divide(v2);
		
		Complex loadEquivY2 = bus.getScLoadShuntY2();
		if(loadEquivY2  == null)
			loadEquivY2 =loadEquivY1;
		
		Complex loadEquivY0 = new Complex(0,0);
		if(bus.getScLoadShuntY0() != null){
			loadEquivY0 = bus.getScLoadShuntY0();
		}
		
		return new Complex3x3(loadEquivY1,loadEquivY2,loadEquivY0);
	}

}
