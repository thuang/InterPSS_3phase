package org.ipss.dynamic.threePhase;

import org.interpss.numeric.datatype.Complex3x1;

import com.interpss.dstab.mach.DynamicMachine;
import com.interpss.dstab.mach.Machine;

public interface Machine3Phase extends DynamicMachine {
	
	public Complex3x1 getIgen3Phase();
	
	public Complex3x1 getGenPe3Phase();
	
	public void setGenPe3Phase(Complex3x1 genPe);
	
    public Complex3x1 getGenQ3Phase();
	
	public void setGenQ3Phase(Complex3x1 genQ);
	
	

}
