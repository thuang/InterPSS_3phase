package org.ipss.threePhase.basic.impl;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Gen3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.util.ThreeSeqLoadProcessor;

import com.interpss.core.aclf.AclfGen;
import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.acsc.AcscBranch;
import com.interpss.core.acsc.impl.AcscBusImpl;
import com.interpss.core.net.Branch;
import com.interpss.dstab.impl.DStabBusImpl;

public class Bus3PhaseImpl extends DStabBusImpl implements Bus3Phase{
	
	private Complex3x1 Vabc = null;
	private Complex3x3 shuntYabc = null;
	Complex3x3 yiiAbc = new Complex3x3();

	@Override
	public Complex3x1 get3PhaseVotlages() {
		
		return  this.Vabc;
	}

	@Override
	public void set3PhaseVoltages(Complex3x1 vabc) {
		this.Vabc = vabc;
		
	}
	
	@Override
	public Complex3x1 get3SeqVotlages() {
		return Complex3x1.abc_to_z12(this.Vabc);
	}

	@Override
	public void set3SeqVoltages(Complex3x1 v120) {
		this.Vabc =Complex3x1.z12_to_abc(v120);  // all voltages are saved in three-phase, in order to make sure data consistency
	}

	@Override
	public Complex3x3 getYiiAbc() {
		 
		// contributions from the connected branches
		 for(Branch bra:this.getBranchList()) {
			 if(bra.isActive()){
				 if(bra instanceof Branch3Phase){
					 Branch3Phase thrPhBranch = (Branch3Phase) bra; 
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
		
		//Switch shunt
		 
		 
		/*
		// three-phase generic loads, they can be used to define three-phase or single-phase loads
		 for(AclfLoad load:this.getContributeLoadList()) {
			 if(load.isActive()){
				 if(load instanceof Load3Phase){
					 Load3Phase ph3Load = (Load3Phase) load;
					 yiiAbc = yiiAbc.add(ph3Load.getEquivYabc());
				 }
				
			 }
		 }
		 */
		 
		 //the conventional three-sequence load definition, they need to be pre-processed using ThreeSeqLoadProcessor
		// yiiAbc= yiiAbc.add(ThreeSeqLoadProcessor.getEquivLoadYabc(this));
		
		// the generators
		 for(AclfGen gen:this.getContributeGenList()) {
			 if(gen.isActive()){
				 if(gen instanceof Gen3Phase){
					 Gen3Phase ph3Gen = (Gen3Phase) gen;
					 yiiAbc = yiiAbc.add(ph3Gen.getYabc(false));
				 }
			 }
		 }
		

		return yiiAbc;
	}



}
