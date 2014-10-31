package org.ipss.aclf.threePhase.impl;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.aclf.threePhase.ThreePhaseBranch;
import org.ipss.aclf.threePhase.ThreePhaseBus;
import org.ipss.aclf.threePhase.ThreePhaseGen;
import org.ipss.aclf.threePhase.ThreePhaseLoad;

import com.interpss.core.aclf.AclfGen;
import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.acsc.AcscBranch;
import com.interpss.core.acsc.impl.AcscBusImpl;
import com.interpss.core.net.Branch;

public class ThreePhaseBusImpl extends AcscBusImpl implements ThreePhaseBus{
	
	private Complex3x1 Vabc = null;
	private Complex3x3 shuntYabc = null;
	Complex3x3 yiiAbc = new Complex3x3();

	@Override
	public Complex3x1 getPhaseVotlages() {
		
		return  Vabc;
	}

	@Override
	public void setPhaseVoltages(Complex3x1 vabc) {
		this.Vabc = vabc;
		
	}

	@Override
	public Complex3x3 getYiiAbc() {
		 
		// contributions from the connected branches
		 for(Branch bra:this.getBranchList()) {
			 if(bra.isActive()){
				 if(bra instanceof ThreePhaseBranch){
					 ThreePhaseBranch thrPhBranch = (ThreePhaseBranch) bra; 
					 if(this.getId().equals(bra.getFromBus().getId()))
					    yiiAbc = yiiAbc.add(thrPhBranch.getYffabc());
					 else
						 yiiAbc = yiiAbc.add(thrPhBranch.getYttabc());
				 }
			 }
			 
		 }
   
        // the  shuntYabc
		 if(shuntYabc != null){
			 yiiAbc= yiiAbc.add(shuntYabc);
		 }
		
		
		// the loads
		 for(AclfLoad load:this.getContributeLoadList()) {
			 if(load.isActive()){
				 if(load instanceof ThreePhaseLoad){
					 ThreePhaseLoad ph3Load = (ThreePhaseLoad) load;
					 yiiAbc = yiiAbc.add(ph3Load.getEquivYabc());
				 }
			 }
		 }
		
		// the generators
		 for(AclfGen gen:this.getContributeGenList()) {
			 if(gen.isActive()){
				 if(gen instanceof ThreePhaseLoad){
					 ThreePhaseGen ph3Load = (ThreePhaseGen) gen;
					 yiiAbc = yiiAbc.add(ph3Load.getYabc());
				 }
			 }
		 }
		

		return yiiAbc;
	}

}
