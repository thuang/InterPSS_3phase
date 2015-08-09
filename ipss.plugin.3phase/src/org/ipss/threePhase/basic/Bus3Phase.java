package org.ipss.threePhase.basic;

import java.util.List;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.acsc.AcscBus;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.dynLoad.DynLoadModel;

public interface Bus3Phase extends DStabBus {
	
	public Complex3x1 get3PhaseVotlages();
	
    public void set3PhaseVoltages(Complex3x1 vabc);
    
    
    public Complex3x3 getYiiAbc();
    
    public List<DynLoadModel> getPhaseADynLoadList();
    
    public List<DynLoadModel> getPhaseBDynLoadList();
    
    public List<DynLoadModel> getPhaseCDynLoadList();

}
