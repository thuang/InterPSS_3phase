package org.ipss.threePhase.dynamic.model;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;

import com.interpss.dstab.dynLoad.DynLoadModel;


public abstract class DynLoadModel3Phase extends DynamicModel3Phase implements DynLoadModel {

	
	Complex3x1 initLoadPQ3phase = null;
	
	public Complex3x1 getInitLoadPQ3Phase() {
		
		return this.initLoadPQ3phase;
	}
}
