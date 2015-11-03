package org.ipss.threePhase.basic.impl;

import static org.ipss.threePhase.util.ThreePhaseUtilFunction.threePhaseGenAptr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.dynamic.model.DStabGen3Phase;
import org.ipss.threePhase.dynamic.model.DynLoadModel1Phase;
import org.ipss.threePhase.util.ThreeSeqLoadProcessor;

import com.interpss.core.aclf.AclfGen;
import com.interpss.core.net.Branch;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.dynLoad.DynLoadModel;
import com.interpss.dstab.impl.DStabBusImpl;



public class Bus3PhaseImpl extends DStabBusImpl implements Bus3Phase{
	
	private Complex3x1 Vabc = null;
	private Complex3x3 shuntYabc = null;
	Complex3x3 yiiAbc = new Complex3x3();
	
	private List<DynLoadModel1Phase> phaseADynLoadList;
	
	private List<DynLoadModel1Phase> phaseBDynLoadList;
	
	private List<DynLoadModel1Phase> phaseCDynLoadList;
	
	private List<Load3Phase> threePhaseLoadList = null;
	private Complex3x1 load3PhEquivCurInj = null;
	private Complex3x1 equivCurInj3Phase = null;

	@Override
	public Complex3x1 get3PhaseVotlages() {
		
		return  this.Vabc;
	}

	@Override
	public void set3PhaseVoltages(Complex3x1 vabc) {
		this.Vabc = vabc;
		super.setThreeSeqVoltage(Complex3x1.abc_to_z12(Vabc));
		
	}
	
	@Override
	public Complex3x1 getThreeSeqVoltage() {
		  if(this.threeSeqVoltage.abs() ==0.0){
		    if(this.Vabc!=null)
			  this.threeSeqVoltage = Complex3x1.abc_to_z12(this.Vabc);
		    else
		    	this.threeSeqVoltage.b_1 = this.getVoltage();
		  }
		   return this.threeSeqVoltage;
	}

	@Override
	public void setThreeSeqVoltage(Complex3x1 v120) {
		super.setThreeSeqVoltage(v120);
		this.Vabc =Complex3x1.z12_to_abc(v120);  // all voltages are saved in three-phase, in order to make sure data consistency
	}

	@Override
	public Complex3x3 getYiiAbc() {
		//always start from zero
		yiiAbc = new Complex3x3();
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
		 
		 //TODO 06/16/2016 EquivLoadYabc does not limit to load buses, otherwise buses with sequence shuntY cannot be correctly modeled
		
//		    if(ThreeSeqLoadProcessor.getEquivLoadYabc(this).abs()>1.0E-4)
//		          System.out.println(this.getId()+" eqvYabc abs() = "+ThreeSeqLoadProcessor.getEquivLoadYabc(this).abs());
//		    
		     yiiAbc= yiiAbc.add(ThreeSeqLoadProcessor.getEquivLoadYabc(this));
		 
		
		     
		   //NOTE: The contribution from the generators has been considered in the  <EquivLoadYabc> above    
		
		// the generators
//		 for(AclfGen gen:this.getContributeGenList()) {
//			 if(gen.isActive()){
//				 if(gen instanceof DStabGen){
//					 DStabGen dynGen = (DStabGen) gen;
//					 DStabGen3Phase gen3P = threePhaseGenAptr.apply(dynGen);
//					 yiiAbc = yiiAbc.add(gen3P.getYabc(false));
//				 }
//				 
//			 }
//		 }
        
		

		return yiiAbc;
	}

	@Override
	public List<DynLoadModel1Phase> getPhaseADynLoadList() {
		
		if(this.phaseADynLoadList == null)
			this.phaseADynLoadList = new ArrayList<DynLoadModel1Phase>();
		return this.phaseADynLoadList;
	}

	@Override
	public List<DynLoadModel1Phase> getPhaseBDynLoadList() {
		if(this.phaseBDynLoadList == null)
			this.phaseBDynLoadList = new ArrayList<DynLoadModel1Phase>();
		
		return this.phaseBDynLoadList;
	}

	@Override
	public List<DynLoadModel1Phase> getPhaseCDynLoadList() {
		if(this.phaseCDynLoadList == null)
			this.phaseCDynLoadList = new ArrayList<DynLoadModel1Phase>();
		return this.phaseCDynLoadList;
	}

	@Override
	public List<Load3Phase> getThreePhaseLoadList() {
		if(threePhaseLoadList ==null)
			threePhaseLoadList = new ArrayList<>();
		return threePhaseLoadList;
	}

	
	private Complex3x1 calcLoad3PhEquivCurInj() {
		this.load3PhEquivCurInj = new Complex3x1();
		if(this.isLoad() && this.getThreePhaseLoadList().size()>0){
			if (Vabc == null ||Vabc.abs()<1.0E-5) 
				Vabc = new Complex3x1(new Complex(1,0),new Complex(-Math.sin(Math.PI/6),-Math.cos(Math.PI/6)),new Complex(-Math.sin(Math.PI/6),Math.cos(Math.PI/6)));
			for(Load3Phase load:this.getThreePhaseLoadList()){
				this.load3PhEquivCurInj=this.load3PhEquivCurInj.add(load.getEquivCurrInj(Vabc));
			}
		}
		return this.load3PhEquivCurInj;
		
	}

	@Override
	public Complex3x1 calc3PhEquivCurInj() {
		calcLoad3PhEquivCurInj();
		
		this.equivCurInj3Phase = this.load3PhEquivCurInj;
		return this.equivCurInj3Phase;
	}



}
