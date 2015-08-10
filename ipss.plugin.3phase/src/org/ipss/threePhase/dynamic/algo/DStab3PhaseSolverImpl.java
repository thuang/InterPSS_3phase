package org.ipss.threePhase.dynamic.algo;

import org.interpss.numeric.util.NumericUtil;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.dynamic.model.DynLoadModel1Phase;

import com.interpss.common.exp.InterpssException;
import com.interpss.common.msg.IPSSMsgHub;
import com.interpss.common.util.IpssLogger;
import com.interpss.core.aclf.AclfGen;
import com.interpss.core.net.Branch;
import com.interpss.core.net.Bus;
import com.interpss.dstab.DStabBranch;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.algo.DynamicSimuAlgorithm;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.algo.defaultImpl.DStabSolverImpl;
import com.interpss.dstab.common.DStabSimuException;
import com.interpss.dstab.device.DynamicBranchDevice;
import com.interpss.dstab.device.DynamicBusDevice;
import com.interpss.dstab.dynLoad.DynLoadModel;
import com.interpss.dstab.mach.Machine;

public class DStab3PhaseSolverImpl extends DStabSolverImpl {

	public DStab3PhaseSolverImpl(DynamicSimuAlgorithm algo, IPSSMsgHub msg) {
		super(algo, msg);
		
	}
	
	@Override public void nextStep(double time, double dt, DynamicSimuMethod method)  throws DStabSimuException {
		 
		 boolean netSolConverged = true;
		 //maxIterationTimes =1;
		 for(int i=0;i<maxIterationTimes;i++){
			
			// solve net equation
			if (!dstabAlgo.getNetwork().solveNetEqn())
				throw new DStabSimuException("Exception in dstabNet.solveNetEqn()");
			
			for ( Bus busi : dstabAlgo.getNetwork().getBusList() ) {
				DStabBus bus = (DStabBus)busi;
				if(bus.isActive()){
					if(i>=1){
						if(!NumericUtil.equals(bus.getVoltage(),voltageRecTable.get(bus.getId()),this.converge_tol))
							netSolConverged =false;
					}
					voltageRecTable.put(bus.getId(), bus.getVoltage());
				}
			}
			
			// check whether the network solution is converged?
			if(i>=1 && netSolConverged) {
				IpssLogger.getLogger().fine("SimuTime: "+dstabAlgo.getSimuTime()+"\n Network solution converges with "+(i+1)+" iterations");
				break;
			}
			
		 } // END OF for maxIterationTimes loop
			
			// Solve DEqn for all dynamic bus devices
			for (DStabBus b : dstabAlgo.getNetwork().getBusList()) {
				if(b.isActive()){
					
					for (DynamicBusDevice device : b.getDynamicBusDeviceList()) {
						// solve DEqn for the step. This includes all controller's nextStep() call
						if(device.isActive()){
							if (!device.nextStep(dt, method)) {
								throw new DStabSimuException("Error occured, Simulation will be stopped");
							}
						}
					}
					// Solve DEqn for generator 
					if(b.getContributeGenList().size()>0){
						for(AclfGen gen:b.getContributeGenList()){
							if(gen.isActive()){
								Machine mach = ((DStabGen)gen).getMach();
								if(mach!=null && mach.isActive()){
								   if (!mach.nextStep(dt, method)) {
									  throw new DStabSimuException("Error occured when solving nextStep for mach #"+ mach.getId()+ "@ bus - "
								                   +b.getId()+", Simulation will be stopped!");
								   }
								}
							}
						}
					}
					
					//Solve DEqn for dynamic loads, e.g. induction motor, whose dynamics are represented in positive-sequence,
					//for three-phase TS simulation, a wrapper technique is used.
					
					if(b.isLoad() && b.getDynLoadModelList().size()>0){
						for(DynLoadModel load: b.getDynLoadModelList()){
							if(load.isActive())
								if (!load.nextStep(dt, method))
									 throw new DStabSimuException("Error occured when solving nextStep for dynamic load #"+ load.getId()+ "@ bus - "
							                   +b.getId()+", Simulation will be stopped!");
						}
					}
					
					Bus3Phase bus3p = (Bus3Phase) b;
					
					
					// Solve DEqn for single-phase dynamic loads
					if(b.isLoad()){
						
						// phase A
						if(bus3p.getPhaseADynLoadList().size()>0){
					
						   for(DynLoadModel1Phase load1p: bus3p.getPhaseADynLoadList()){
							   if(load1p.isActive() )
								   if (!load1p.nextStep(dt, method))
									 throw new DStabSimuException("Error occured when solving nextStep for dynamic load #"+ load1p.getId()+ "@ bus - "
							                   +b.getId()+", Simulation will be stopped!");
								 
						   }
					   }
						
						// phase B
						if(bus3p.getPhaseBDynLoadList().size()>0){
							
							   for(DynLoadModel1Phase load1p: bus3p.getPhaseBDynLoadList()){
								   if(load1p.isActive() )
									   if (!load1p.nextStep(dt, method))
										 throw new DStabSimuException("Error occured when solving nextStep for dynamic load #"+ load1p.getId()+ "@ bus - "
								                   +b.getId()+", Simulation will be stopped!");
									 
							   }
						}
						
						 // phase C
						if(bus3p.getPhaseCDynLoadList().size()>0){
							
							   for(DynLoadModel1Phase load1p: bus3p.getPhaseCDynLoadList()){
								   if(load1p.isActive() )
									   if (!load1p.nextStep(dt, method))
										 throw new DStabSimuException("Error occured when solving nextStep for dynamic load #"+ load1p.getId()+ "@ bus - "
								                   +b.getId()+", Simulation will be stopped!");
									 
								
							   }
						}
						
					}
					
				}
			}

			// Solve DEqn for all dynamic branch devices
			for (Branch b : dstabAlgo.getNetwork().getBranchList()) {
				DStabBranch branch = (DStabBranch)b;
				for (DynamicBranchDevice device : branch.getDynamicBranchDeviceList()) {
					// solve DEqn for the step. This includes all controller's nextStep() call
					if (!device.nextStep(dt, method)) {
						throw new DStabSimuException("Error occured, Simulation will be stopped");
					}
				}
			}
			
			// update dynamic device attributes, such as the Pe of a generator and calculate dynamic measurement signals - they are secondary signals 
			for ( Bus busi : dstabAlgo.getNetwork().getBusList() ) {
				DStabBus bus = (DStabBus)busi;
				if(bus.isActive()){
					
					// update dynamic attributes of the dynamic devices connected to the bus
					 try {
						bus.updateDynamicAttributes(false);
					} catch (InterpssException e) {
					
						e.printStackTrace();
					}
					
					//calculate dynamic measurement signals
					if (!bus.nextStep(dt, method)) {
						throw new DStabSimuException("Error occured, Simulation will be stopped");
					}
				}
			}
			

		 
		//TODO update the states
		 for (Bus b : dstabAlgo.getNetwork().getBusList()) {
				if(b.isActive()){
					DStabBus bus = (DStabBus)b;
					// Solve DEqn for generator 
					if(bus.getContributeGenList().size()>0){
						for(AclfGen gen:bus.getContributeGenList()){
							if(gen.isActive()){
								Machine mach = ((DStabGen)gen).getMach();
								if(mach!=null && mach.isActive()){
								  mach.backUpStates();
								}
							}
						}
					}
					
					//TODO Solve DEqn for dynamic load, e.g. induction motor
				}
			}

		}
	
	
	

}
