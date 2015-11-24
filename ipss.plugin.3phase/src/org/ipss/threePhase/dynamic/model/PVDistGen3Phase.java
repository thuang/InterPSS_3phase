package org.ipss.threePhase.dynamic.model;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.ComplexFunc;
import org.ipss.threePhase.basic.Gen3Phase;

import com.interpss.core.acsc.AcscBus;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.algo.DynamicSimuMethod;

/**
 * PV Distributed generation dynamic model.
 * Currently it supports constant PQ mode with converter current limit considered.
 * 
 * The effect of negative sequence is modeled as constant negative sequence impedance. 
 * @author Qiuhua Huang
 *
 */
public class PVDistGen3Phase extends DynGenModel3Phase{
	
	private Complex  genPQInit = null;  // based on the positive sequence
	private Complex  posSeqGenPQ = null;
	private Complex  genCurSource = null;
	private double   currLimit = 9999;
	private Complex  Ipq_pos = null;

	
	private double TR = 0.01 ;
	private double vtmeasured = 0;
	
	// under and over voltage protection 
	private double underVoltTripStart = -1.0; // below this voltage, generation starts to trip
	private double underVoltTripAll = -1.0;	// below  this voltage, all generation are tripped	
	private double overVoltTripStart = 99.0; // below this voltage, generation starts to trip
	private double overVoltTripAll = 99.0;	// below  this voltage, all generation are tripped
	
	//TODO control model, constant PQ or Volt/Var control
	private double underFreqTripStart = -1.0; // below this voltage, generation starts to trip
	private double underFreqTripAll = -1.0;	// below  this voltage, all generation are tripped	
	private double overFreqTripStart = 99.0; // below this voltage, generation starts to trip
	private double overFreqTripAll = 99.0;	// below  this voltage, all generation are tripped
	
	
	public PVDistGen3Phase(){
		
	}
	
	public PVDistGen3Phase(Gen3Phase gen) {
		this.parentGen = gen;
		gen.setDynamicGenDevice(this);
	}

	public double getCurrLimit(){
		return currLimit;
	}
	 
	public void   setCurrLimit(double Ilimit){
		this.currLimit = Ilimit;
	}
	 
	 
	 // obtain the initial positive sequence power flow from the power flow result
	 public void setPosSeqGenPQ(Complex genPQ){
		 this.posSeqGenPQ = genPQ;
	 }
	 
	 
	 public Complex getPosSeqGenPQ(){
		 // if the positive sequence genPQ is not set, then use the phase A genPQ, assuming that all three-phase are the same
		 if(this.posSeqGenPQ == null) {
			 this.posSeqGenPQ = this.getParentGen().getGen();
		 }
		 
		 return this.posSeqGenPQ; 
	 }
	 
	 @Override
	 public boolean initStates(DStabBus abus){
		 if(this.getPosSeqGenPQ() == null)
			 return false;
		 this.genPQInit = new Complex(this.posSeqGenPQ.getReal(),this.posSeqGenPQ.getImaginary());
		 this.vtmeasured = getPosSeqVt().abs();
		 
		 return true;
	 }
	 
	 public Complex getInitGenPQ(){
		 return this.genPQInit;
	 }
	 
	 // for dynamic simulation, in the nextStep, update the equivalent current injection
     public boolean nextStep(double dt, DynamicSimuMethod method){
    	 //TODO for the simplified inverter-based PV generation, the only dynamic components is the terminal voltage measurement
    	 
    	 double dVtm_dt = (getPosSeqVt().abs()-this.vtmeasured)/this.TR;
    	 
    	 this.vtmeasured = this.vtmeasured + dVtm_dt*dt;
    	 return true;
     }
     
     private Complex getPosSeqVt(){
    	 Complex vt = ((AcscBus)this.getParentGen().getParentBus()).getThreeSeqVoltage().b_1;
    	 return vt;
     }
     
     // calculate the positive current Injection
     // The logic of the following implementation  is based on the "WECC specifications for modeling distributed generation 
     // in power flow and dynamics" report
     private Complex calcPosSeqCurInjection(){
    	 
    	 double freq = ((DStabBus)this.getParentGen().getParentBus()).getFreq();
    	 
    	 
    	 //calculate Idq with reference to the terminal voltage angle
    	 double Ip_ord1 = this.getInitGenPQ().getReal()/this.vtmeasured;
    	 
    	 double Iq_ord1 = this.getInitGenPQ().getImaginary()/this.vtmeasured;
    	 
    	 //
    	 double Ia_order = Math.sqrt(Ip_ord1*Ip_ord1+Iq_ord1*Iq_ord1);
    	 
    	 
    	 double Ip_prod = Ip_ord1;
    	 double Iq_prod = Iq_ord1;
    	 
    	 if(Ia_order > this.currLimit){
    		 
    		 double ratio = this.currLimit/Ia_order;
    		 
    	     Ip_prod = Ip_ord1*ratio;
    	     Iq_prod = Iq_ord1*ratio;
    	 }
    	 
    	 
    	 // applied the protection to the Idq value
    	 
    	 double fvl = getUnderValueProtectionOutput(this.underVoltTripAll,this.underVoltTripStart,this.vtmeasured);
    	 
    	 double fvh = getOverValueProtectionOutput(this.overVoltTripStart,this.overVoltTripAll,this.vtmeasured);
    	 
    	 
	     double ffl = getUnderValueProtectionOutput(this.underFreqTripAll,this.underFreqTripStart,freq);
    	 
    	 double ffh = getOverValueProtectionOutput(this.overFreqTripStart,this.overFreqTripAll, freq);
    	 
    	 double protectCutRatio = fvl*fvh*ffl*ffh;
    	 
    	 Ip_prod =  Ip_prod*protectCutRatio ;
    	 Iq_prod =  Iq_prod*protectCutRatio ;
    	 
         this.Ipq_pos = new Complex(Ip_prod,Iq_prod);
    	 
    	 //transfer Idq to Ir_x based on network reference frame;
    	
    	 // |IR|    | cos(Theta)  -sin(Theta)|  |IP|
    	 // |IX|  = | sin(Theta)  cos(Theta) |  |IQ|
    	 
    	 double vtAng = ComplexFunc.arg(getPosSeqVt());
    	 double Ir =  Ip_prod*Math.cos(vtAng)-Iq_prod*Math.sin(vtAng);
    	 double Ix =  Ip_prod*Math.sin(vtAng)-Iq_prod*Math.cos(vtAng);
    	 
    	 Complex effectiveCurrInj = new Complex(Ir,Ix);
    	 
    	 //TODO consider the positive sequence power drawn by the equivalent Ypos at the terminal
    	 //if(this.getParentGen().getPosGenZ()!=null && this.getParentGen().getPosGenZ())
    		 
    		 
    	 genCurSource = effectiveCurrInj;
    	 
         return genCurSource;
    	 
     }
     
     private double getUnderValueProtectionOutput(double lowValue, double upValue, double input){
    	 // check the values 
    	 if(lowValue >=upValue){
    		 return 1.0;
    	 }
    	 	 
    	 if(input <=lowValue) return 0;
    	 else if(input >=upValue) return 1;
    	 else{
    		 return (input-lowValue)/(upValue-lowValue);
    	 }
     }
     
     private double getOverValueProtectionOutput(double lowValue, double upValue, double input){
    	// check the values 
    	 if(lowValue >=upValue){
    		 return 1.0;
    	 }
    		 
    	 if(input <=lowValue) return 1;
    	 else if(input >=upValue) return 0;
    	 else{
    		 return (upValue-input)/(upValue-lowValue);
    	 }
     }
     
	 
	 // set the positive sequence equivalent current injection as the output object
     
     public Object getOutputObject(){
    	 
         this.calcPosSeqCurInjection();
    	 return this.genCurSource;
     }
     
     public Complex getPosSeqIpq() {
    	 if(Ipq_pos==null) calcPosSeqCurInjection();
 		return Ipq_pos;
 	}
     
    public Complex  getPosSeqGenCurSource(){
    	return this.genCurSource;
    }
    
    public void    setPosSeqGenCurSource( Complex Igen){
    	this.genCurSource = Igen;
    }

 	public void setPosSeqIpq(Complex ipq_pos) {
 		Ipq_pos = ipq_pos;
 	}

 	public double getUnderVoltTripStart() {
 		return underVoltTripStart;
 	}

 	public void setUnderVoltTripStart(double underVoltTripStart) {
 		this.underVoltTripStart = underVoltTripStart;
 	}

 	public double getUnderVoltTripAll() {
 		return underVoltTripAll;
 	}

 	public void setUnderVoltTripAll(double underVoltTripAll) {
 		this.underVoltTripAll = underVoltTripAll;
 	}

 	public double getOverVoltTripStart() {
 		return overVoltTripStart;
 	}

 	public void setOverVoltTripStart(double overVoltTripStart) {
 		this.overVoltTripStart = overVoltTripStart;
 	}

 	public double getOverVoltTripAll() {
 		return overVoltTripAll;
 	}

 	public void setOverVoltTripAll(double overVoltTripAll) {
 		this.overVoltTripAll = overVoltTripAll;
 	}

 	public double getUnderFreqTripStart() {
 		return underFreqTripStart;
 	}

 	public void setUnderFreqTripStart(double underFreqTripStart) {
 		this.underFreqTripStart = underFreqTripStart;
 	}

 	public double getUnderFreqTripAll() {
 		return underFreqTripAll;
 	}

 	public void setUnderFreqTripAll(double underFreqTripAll) {
 		this.underFreqTripAll = underFreqTripAll;
 	}

 	public double getOverFreqTripStart() {
 		return overFreqTripStart;
 	}

 	public void setOverFreqTripStart(double overFreqTripStart) {
 		this.overFreqTripStart = overFreqTripStart;
 	}

 	public double getOverFreqTripAll() {
 		return overFreqTripAll;
 	}

 	public void setOverFreqTripAll(double overFreqTripAll) {
 		this.overFreqTripAll = overFreqTripAll;
 	}
     
}
