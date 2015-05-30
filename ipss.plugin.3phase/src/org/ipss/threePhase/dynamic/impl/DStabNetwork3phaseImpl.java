package org.ipss.threePhase.dynamic.impl;

import static com.interpss.common.util.IpssLogger.ipssLogger;
import static org.ipss.threePhase.util.ThreePhaseUtilFunction.threePhaseGenAptr;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.numeric.sparse.ISparseEqnComplexMatrix3x3;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Gen3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.dynamic.DStabGen3Phase;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.util.ThreeSeqLoadProcessor;

import com.interpss.common.datatype.Constants;
import com.interpss.common.exp.InterpssException;
import com.interpss.common.util.IpssLogger;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGen;
import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.acsc.AcscBranch;
import com.interpss.core.acsc.AcscBus;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.net.Branch;
import com.interpss.core.net.Bus;
import com.interpss.core.sparse.impl.SparseEqnComplexMatrix3x3Impl;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.impl.DStabilityNetworkImpl;

public class DStabNetwork3phaseImpl extends DStabilityNetworkImpl implements DStabNetwork3Phase {
    
	protected ISparseEqnComplexMatrix3x3 yMatrixAbc = null;
	protected boolean is3PhaseNetworkInitialized = false;
	protected Hashtable<String, Complex3x1> threePhaseCurInjTable = null;
	private boolean isLoadModelConverted = false;
	
	@Override
	public boolean initThreePhaseFromLfResult() {

		/*  
		 * initialize the bus phase voltages. 
		 *   Special attentions need to be paid to the buses within the subtransmission and the distribution system, if any, 
		 *   connected to the LV side of Delta/Yg connected step-down transformer 
		 *   
		 *   steps:
		 *   
		 *   (1) initialization by setting the visited status false
		 *   (2) search the step-down delta/Y connected transformers
		 *   (3) starting from the low voltage side of the transformers, set the phase voltage with 30 deg lagging w.r.t the positive sequenc voltage, 
		 *      by assuming the connection meeting the  U.S. Delta connection standard, with high voltage side leading 30 degree, always 
	     *      Finally, set the visited attribute to be true.
	     *   (4) iterate over all step-down transformers and the connected subtransmissions 
	     *   (5) for the rest of the buses, set the phase votlages directly based on the positive sequence voltage
	     *
	     */
		
		//step (1)
		for(AcscBranch bra: this.getBranchList()){
			bra.setVisited(false);
		}
		for(Bus b: this.getBusList()){
			b.setVisited(false);
			b.setIntFlag(0);
		}
		
		double phaseShiftDeg = 0;
		
		for(AcscBranch bra: this.getBranchList()){
			if(bra.isActive() && bra.isXfr()){
				if((isDeltaConnected(bra.getXfrFromConnectCode()) && 
						!isDeltaConnected(bra.getXfrToConnectCode()))
						
						||
						(!isDeltaConnected(bra.getXfrFromConnectCode()) &&
						    isDeltaConnected(bra.getXfrToConnectCode()))){
					
					 bra.setVisited(true);
					 
					 phaseShiftDeg = -30;
					 Bus3Phase  StartingBus =null;
					 
					 //high voltage side leads 30 deg, always starts from the low voltage side
					 if(bra.getFromAclfBus().getBaseVoltage()>bra.getToAclfBus().getBaseVoltage()){
						 
						 StartingBus = (Bus3Phase) bra.getToAclfBus();
					 }		
					 else {
					
						 StartingBus = (Bus3Phase) bra.getFromAclfBus();
					 }
					 
					    Complex vpos = StartingBus.getVoltage();
						Complex va = vpos.multiply(phaseShiftCplxFactor(phaseShiftDeg));
						Complex vb = va.multiply(phaseShiftCplxFactor(-120));
						Complex vc = va.multiply(phaseShiftCplxFactor(120));
						StartingBus.set3PhaseVoltages(new Complex3x1(va,vb,vc));
						StartingBus.setVoltage(StartingBus.get3SeqVoltage().b_1);
					 
					 Queue<Bus3Phase> q = new  LinkedList<Bus3Phase>();
				     q.add(StartingBus);
				     
				     BFSSubTransmission(phaseShiftDeg,q);
				}
			}
		}
		
		
		
		// initialize the phase voltages of those which are not set before, three-phase generation power output and load
		for(AcscBus b: this.getBusList()){
			
			if(b.isActive() && !b.isVisited()){
				   Complex vpos = b.getVoltage();
					Complex va = vpos;
					Complex vb = va.multiply(phaseShiftCplxFactor(-120));
					Complex vc = va.multiply(phaseShiftCplxFactor(120));
					((Bus3Phase) b).set3PhaseVoltages(new Complex3x1(va,vb,vc));
			}
				
			//initialize the 3p power output of generation;
			if(b.isGen()){
				for(AclfGen gen: b.getContributeGenList()){
					if(gen instanceof Gen3Phase){
						Gen3Phase ph3Gen = (Gen3Phase) gen;
						Complex phaseGen = gen.getGen();// phase gen and 3-phase gen are of the same value in PU
						ph3Gen.setPower3Phase(new Complex3x1(phaseGen,phaseGen,phaseGen), UnitType.PU);
					}
				}
			}
			
			// initialize the load 3-phase power
			if(b.isLoad()){
				for(AclfLoad load: b.getContributeLoadList()){
					if(load instanceof Load3Phase){
						Load3Phase ph3Load = (Load3Phase) load; 
						Complex phaseLoad = load.getLoad(b.getVoltageMag()); // phase load and 3-phase load are of the same value in PU
						
						ph3Load.set3PhaseLoad(new Complex3x1(phaseLoad,phaseLoad,phaseLoad));
					}
				}
			}
			
		}
		
		
		
		return is3PhaseNetworkInitialized= true;
	}
	
	private boolean isDeltaConnected(XfrConnectCode code){
		return code ==XfrConnectCode.DELTA ||
				code== XfrConnectCode.DELTA11;
	}
	
	private void BFSSubTransmission (double phaseShiftDeg, Queue<Bus3Phase> onceVisitedBuses){
		
		//Retrieves and removes the head of this queue, or returns null if this queue is empty.
	    while(!onceVisitedBuses.isEmpty()){
			Bus3Phase  startingBus = onceVisitedBuses.poll();
			startingBus.setVisited(true);
			startingBus.setIntFlag(2);
			
			if(startingBus!=null){
				  for(Branch connectedBra: startingBus.getBranchList()){
						if(connectedBra.isActive() && !connectedBra.isVisited()){
							try {
								Bus findBus = connectedBra.getOppositeBus(startingBus);
								
								//update status
								connectedBra.setVisited(true);
								
								//for first time visited buses
								
								if(findBus.getIntFlag()==0){
									findBus.setIntFlag(1);
									onceVisitedBuses.add((Bus3Phase) findBus);
									
									// update the phase voltage
									Complex vpos = ((AclfBus)findBus).getVoltage();
									Complex va = vpos.multiply(phaseShiftCplxFactor(phaseShiftDeg));
									Complex vb = va.multiply(phaseShiftCplxFactor(-120.0d));
									Complex vc = va.multiply(phaseShiftCplxFactor(120.0d));
									
									((Bus3Phase) findBus).set3PhaseVoltages(new Complex3x1(va,vb,vc));
									 ((AclfBus)findBus).setVoltage(((Bus3Phase) findBus).get3SeqVoltage().b_1);
								}
							} catch (InterpssException e) {
								
								e.printStackTrace();
							}
							
						}
				 }
			 
			}
			
	      }
	}
	
	private Complex phaseShiftCplxFactor(double shiftDeg){
		return new Complex(Math.cos(shiftDeg/180.0d*Math.PI),Math.sin(shiftDeg/180.0d*Math.PI));
	}

	@Override
	public ISparseEqnComplexMatrix3x3 formYMatrixABC() throws Exception {
		
		// check if load model is converted
		if(!this.isLoadModelConverted )
			    convertLoadModel();
		
		yMatrixAbc = new SparseEqnComplexMatrix3x3Impl(getNoBus());
		
		for(DStabBus b:this.getBusList()){
			if(b instanceof Bus3Phase){
				int i = b.getSortNumber();
				Bus3Phase ph3Bus = (Bus3Phase) b;
				yMatrixAbc.setA(ph3Bus.getYiiAbc() ,i, i);
			}
			else
				throw new Exception("The processing bus # "+b.getId()+"  is not a threePhaseBus");
		}
		
		for (AcscBranch bra : this.getBranchList()) {
			if (bra.isActive()) {
				if(bra instanceof Branch3Phase){
					Branch3Phase ph3Branch = (Branch3Phase) bra;
					int i = bra.getFromBus().getSortNumber(),
						j = bra.getToBus().getSortNumber();
					yMatrixAbc.addToA( ph3Branch.getYftabc(), i, j );
					yMatrixAbc.addToA( ph3Branch.getYtfabc(), j, i );
				}
				else
					throw new Exception("The processing branch #"+bra.getId()+"  is not a threePhaseBranch");
			}
			
		}
		
		
		setYMatrixDirty(true);
	
		return yMatrixAbc;
	}
	
	private void convertLoadModel() {
		for ( DStabBus busi : getBusList() ) {
			   //only the active buses will be initialized
				if(busi.isActive()){
					//init three sequence load
					ThreeSeqLoadProcessor.initEquivLoadY120(busi);
				}
		}
		this.isLoadModelConverted = true;
		
	}

	@Override
	public ISparseEqnComplexMatrix3x3 getYMatrixABC(){
		return this.yMatrixAbc;
	}

	@Override
	public boolean run3PhaseLoadflow() {
		throw new UnsupportedOperationException();
	}

	
	@Override
	public boolean solveNetEqn() {
  		try {
  			
  			if(isYMatrixDirty()){
	  			getYMatrixABC().luMatrix(Constants.Matrix_LU_Tolerance);
				setYMatrixDirty(false);
  			}
  			
		  	// Calculate and set generator injection current
			for( Bus b : getBusList()) {
				DStabBus bus = (DStabBus)b;

				if(bus.isActive()){
					
					Complex3x1 iInject = new Complex3x1();

					if(bus.getContributeGenList().size()>0){
						 for(AclfGen gen: bus.getContributeGenList()){
						      if(gen.isActive() && gen instanceof DStabGen){
						    	  DStabGen dynGen = (DStabGen)gen;
						    	  if( dynGen.getMach()!=null){
						    		  DStabGen3Phase gen3P = threePhaseGenAptr.apply(dynGen);
						    		  iInject = iInject.add(gen3P.getIgen3Phase());
						    	  }
						    	 
						       }
						  }
				    }
				  
				  if(iInject == null){
					  throw new Error (bus.getId()+" current injection is null");
				  }
				  
				  // add external/customized bus current injection
				  if(this.get3phaseCustomCurrInjTable()!=null){
					  iInject = iInject.add(this.get3phaseCustomCurrInjTable().get(bus.getId()));
				  }

				  getYMatrixABC().setBi(iInject, bus.getSortNumber());
				}
			}
			
			//add customized bus current injection, which is used in hybrid simulation or 
			//for adding user new defined models, which is represented as current injections in dynamic simulation
			
			//addCustomBusCurrentInj();
			
			// ISparseEqnComplexMatrix3x3  Yabc = getYMatrixABC();
			// System.out.println(Yabc.getSparseEqnComplex());
		   
			getYMatrixABC().solveEqn();

			// update bus voltage and machine Pe
			for( Bus b : getBusList()) {
				DStabBus bus = (DStabBus)b;
				if(bus.isActive()){
					Complex3x1 vabc = getYMatrixABC().getX(bus.getSortNumber());
					
					//System.out.println("Bus, Vabc:"+b.getId()+","+vabc.toString());
					
					if(!vabc.a_0.isNaN()){
                    
						if(bus instanceof Bus3Phase){
							Bus3Phase bus3P = (Bus3Phase) bus;
							 bus3P.set3PhaseVoltages(vabc);
							 
							 // update the positive sequence voltage
							 Complex v = bus3P.get3SeqVoltage().b_1;
							 bus.setVoltage(v);
							// System.out.println("posV @ bus :"+v.toString()+","+bus.getId());
							
                         }
					
					    bus.updateDynamicAttributes(false);
					}
					else
						 throw new Error (bus.getId()+" solution voltage is NaN");
				}
			}
  			
  		} 
  		catch (IpssNumericException e) {
  			ipssLogger.severe(e.toString());
  			return false;
  		}
  		catch (InterpssException e) {
  			ipssLogger.severe(e.toString());
  			return false;
  		}
		return true;
	}

	@Override
	public boolean initDStabNet() {
		boolean initFlag = true;
		IpssLogger.getLogger().info("Start three-phase DStabNetwork initialization...");
		
		
	  	//TODO this is a must step, otherwise the system cannot be initialized properly
		if(!is3PhaseNetworkInitialized)
	  	     initThreePhaseFromLfResult();
		
		for ( DStabBus busi : getBusList() ) {

			if( busi instanceof Bus3Phase){
			    Bus3Phase bus =(Bus3Phase) busi;

			   //only the active buses will be initialized
				if(busi.isActive()){
					// set bus initial vaule 
					bus.setInitLoad(new Complex(0.0,0.0));
					bus.setInitVoltMag(bus.getVoltageMag());
					
					// init bus dynamic signal calculation, 
					// for example, bus Frequency measurement
					bus.initStates();
					
					
					//initialize the bus generator
					for(AclfGen gen: bus.getContributeGenList()){
						if(gen.isActive() && gen instanceof DStabGen){
							DStabGen dynGen = (DStabGen) gen;
							if(dynGen.getMach()!=null){
								dynGen.getMach().calMultiFactors();
							    if(!dynGen.getMach().initStates(bus))
								   initFlag = false;
							}
						}
					}
					
					
					//initialize the bus load
					//change load from power to admittance, in three-sequence, then to three-phase 	
						
				}
				
				
				
			}
		}
		
		//form the Ymatrix
		try {
			formYMatrixABC();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			initFlag = false;
		}
		
		return initFlag;
	}

	@Override
	public boolean solvePosSeqNetEqn() {
		
		return  super.solveNetEqn();
	}
	
	@Override
	public boolean initPosSeqDStabNet() {
		
		return super.initDStabNet();
	}

	@Override
	public Hashtable<String, Complex3x1> get3phaseCustomCurrInjTable() {
		
		return this.threePhaseCurInjTable;
	}

	@Override
	public void set3phaseCustomCurrInjTable(
			Hashtable<String, Complex3x1> new3PhaseCurInjTable) {
		this.threePhaseCurInjTable = new3PhaseCurInjTable;
		
	}



}
