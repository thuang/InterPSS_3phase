package org.ipss.threePhase.basic;

import java.util.List;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.dynamic.model.DynLoadModel1Phase;

import com.interpss.core.aclf.AclfBus;
import com.interpss.core.acsc.AcscBus;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.dynLoad.DynLoadModel;

public interface Bus3Phase extends DStabBus {
	
	public Complex3x1 get3PhaseVotlages();
	
    public void set3PhaseVoltages(Complex3x1 vabc);
    
    
    public Complex3x3 getYiiAbc();
    
    // TODO in the next phase, put all dynamic models in one array;
    // or we still need to separate the generators and loads;
    public List<DynLoadModel1Phase> getPhaseADynLoadList();
    
    public List<DynLoadModel1Phase> getPhaseBDynLoadList();
    
    public List<DynLoadModel1Phase> getPhaseCDynLoadList();

}
