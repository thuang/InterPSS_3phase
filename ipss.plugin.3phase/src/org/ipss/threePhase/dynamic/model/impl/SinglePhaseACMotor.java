package org.ipss.threePhase.dynamic.model.impl;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Phase;
import org.ipss.threePhase.dynamic.model.DynLoadModel1Phase;

import com.interpss.common.util.IpssLogger;
import com.interpss.dstab.algo.DynamicSimuMethod;

public class SinglePhaseACMotor extends DynLoadModel1Phase {
	
	
	
	   //Model data
		
		private double p=0,q=0;  // load pq values without considering load characterisitc factors
		private double P0 = 0.0,Q0 =0.0; // initial load exponential characteristic factors
		private  double pac =0.0,qac = 0.0; // actual load pq after considering load characterisitc factors
		// there are 3 stages: 0 - running; 1 -  stall ; 2- a fraction of motors restart
		private int stage = 0;
		private double powerFactor = 0.97;
		
		
		
		//stalling setting
		private double Vstall = 0.6;
		private double Rstall = 0.1240;
		private double Xstall = 0.1140;
		private double Tstall = 0.033;
		
		private double LFadj = 0.0;
		
		private double Kp1 = 0.0;
		private double Np1 = 1.0;
		
		private double Kq1 = 6.0;
		private double Nq1 = 2.0;
		
		private double Kp2 = 12.0;
		private double Np2 = 3.2;
		
		
		private double Kq2 = 11.0;
		private double Nq2 = 2.5;
		
		//breaking point
		private double Vbrk =0.86;

		
		// restart
		private double Frst = 0.0;
		private double Vrst = 0.9;
		private double Trst = 0.4;
		
		// real and reactive power frequency sensitivity
		private double CmpKpf = 1.0;
		private double CmpKqf = -3.3;
		
		
		//UVRelay
		private double fuvr = 0.0;
		private double vtr1 = 0.0;
		private double ttr1 = 999.0;
		private double vtr2 = 0.0;
		private double ttr2 = 999.0;
		
		//Contractor setting
		private double	Vc1off = 0.5;
		private double	Vc2off = 0.4;
		private double	Vc1on  = 0.6;
		private double	Vc2on  = 0.5;
		
		//Thermal relay setting
		private double	Tth =  999.0;
		private double	Th1t = 999.0;
		private double	Th2t = 999.0;

		
		//Affiliated control component
		//UV Relay
		
		//Contractor
		
		//Thermal relays
		
		// Timers for relays and internal controls
		
		private double UVRelayTimer1 = 0.0;
		private double UVRelayTimer2 = 0.0;
		
		//Timers for stalling and recovery 
		private double acStallTimer = 0.0;
		private double acRestartTimer = 0.0;


		
		
		
		public SinglePhaseACMotor(){
			
		}
		
		public SinglePhaseACMotor(String Id){
			this.id = Id;
		}
		
		/**
		 * create an instance of SinglePhaseACMotor
		 * @param bus
		 * @param Id
		 */
		public SinglePhaseACMotor(Bus3Phase bus,String Id){
			this.setBus(bus);
			this.id = Id;
			
		}
		

		@Override
		public boolean initStates() {
	       boolean flag = true;
			
	       this.equivY = this.getEquivY();
			
			//TODO the initLoad is the toal load at the bus ,include constant Z and I load
			//In the future, this may need to be update to consider the constant P load only
	        Complex busTotalLoad = this.getBus().getInitLoad();
	        
	        
	        // TODO assuming three-phase balanced
			pac = busTotalLoad.getReal()*this.loadPercent/100.0d;
			qac = pac*Math.tan(Math.acos(this.powerFactor));
			
			
			// pac and qac is the initial power
			this.setInitLoadPQ(new Complex(pac,qac));
			this.setLoadPQ(new Complex(pac,qac));
			
			// if mva is not defined and loading factor is available
			if(this.getMVABase()==0.0){
				if(this.loadFactor >0 && this.loadFactor<=1.0)
	                    IpssLogger.getLogger().fine("AC motor MVABase will be calculated based on load factor");
				else 
					this.loadFactor = 1.0;
				// phase mva base is the 1/3 of the system 3phaes mva base
				this.setMVABase(this.pac*this.getBus().getNetwork().getBaseMva()/3.0d/this.loadFactor);
			}
			
			
			//Check whether a compensation is needed. If yes, calculate the compensation shuntY

			// if bus.loadQ < ld1pac.q, then compShuntB = ld1pac.q-bus.loadQ
			if(qac >busTotalLoad.getImaginary()){
				 double v = this.getBusPhaseVoltage().abs();
				 double b = (qac - busTotalLoad.getImaginary())/v/v;
				 this.compensateShuntY = new Complex(0,b);
			}
			
			
			// update the Vstall and Vbrk if necessary
			//Vstall(adj) = Vstall*(1+LFadj*(CompLF-1))
			//Vbrk(adj) = Vbrk*(1+LFadj*(CompLF-1))
			
		   // Calcuate the P0 and Q0 at stage 0
			P0 = 1 - Kp1*Math.pow((1-Vbrk),Np1);
			Q0 = Math.sqrt(1 - this.powerFactor*this.powerFactor)/this.powerFactor - 
					this.Kq1*Math.pow((1.0-Vbrk),Nq1);
			
			Complex loadPQFactor = calcLoadCharacterFactor();
			p = pac/loadPQFactor.getReal();
			q = qac/loadPQFactor.getImaginary();
			
			return flag;
		}
		
		

		
		/**
		 * The thermal protection heating increase is modeled as 
		 * differential equation, thus it must be represented with the nextStep();
		 * 
		 * The stall timer as well as the recovery timer are also counted and updated in this method
		 */
		@Override
		public boolean nextStep(double dt, DynamicSimuMethod method) {
			boolean flag = true;
			
			// stage update 
			if(acStallTimer>=Tstall){
				stage = 1;
			}
			
			// switch to restart stage
			if (stage == 1  && Frst>0.0 && acRestartTimer >= Trst)
				stage = 2;
			
			// check whether the ac motor is stalled or not
			double v = this.getBusPhaseVoltage().abs();
			
			if(v<=this.Vstall){
				acStallTimer += dt;
			}
			else{
				acStallTimer = 0.0;
			}
			

			// update restart counter
			if(stage == 1 && v>this.Vrst && Frst>0.0){
				acRestartTimer +=dt; 
			}
			else
				acRestartTimer = 0.0;
			
		
			
			// thermal overload protection
			/*
			 * When the motor is stalled, the ¡°temperature¡± of the motor is computed by
				integrating I^2 R through the thermal time constant Tth.  If the temperature reaches Th2t, all of the load is
				tripped.   If the temperature is between  Th1t and  Th2t, a linear fraction of the load is tripped.   The
				¡°termperatures¡± of the ¡°A¡± and ¡°B¡± portions of the load are computed separately.  The fractions of the ¡°A¡±
				and ¡°B¡± parts of the load that have not been tripped by the thermal protection is output as ¡° fthA¡± and
				¡°fthB¡±, respectively.	
			 */
				
				
			// contractor
			/*
			 *  Contactor ¨C If the voltage drops to below Vc2off, all of the load is tripped; if the voltage is between
				Vc1off and Vc2off, a linear fraction of the load is tripped.  If the voltage later recovers to above Vc2on, all
				of the motor is reconnected; if the voltage recovers to between Vc2on and Vc1on, a linear fraction of the
				load is reconnected.  The fraction of the load that has not been tripped by the contactor is output as ¡°fcon¡±.
			 */
			
			return flag;
		}
		
		
		private Complex calcLoadCharacterFactor(){
			// this can be replaced by a protected method getBusVoltageMag(), which is applicable to both pos-seq and single-phase
			double v = getBusPhaseVoltage().abs();
			// exponential factor
			double pfactor = 0.;
			double qfactor = 0.;
			if(v>this.Vbrk  & stage == 0){
				pfactor  = P0 +Kp1*Math.pow((v-Vbrk),Np1);
				qfactor  = Q0 +Kq1*Math.pow((v-Vbrk),Nq1);
		    }
			//TODO remove the constraint v> Vstall &
			else if( v<Vbrk & stage == 0){
				pfactor = P0 +Kp2*Math.pow((Vbrk-v),Np2);
				qfactor = Q0 +Kq2*Math.pow((Vbrk-v),Nq2);
			}
			//TODO how about v<= Vstall? modeled as a constant impedance Zstall??
			
			
			// consider the frequency dependence
			if(pfactor !=0.0 ||qfactor!=0){
				double dFreq = getBus().getFreq()-1.0;
				pfactor = pfactor*(1+CmpKpf*dFreq);
				qfactor = qfactor*(1+CmpKqf*dFreq/Math.sqrt(1-powerFactor*powerFactor));
			}
			
			return new Complex(pfactor,qfactor);
		}
		
		
		private Complex getBusPhaseVoltage(){
			Complex3x1 vabc = ((Bus3Phase)this.getBus()).get3PhaseVotlages();
			
			switch(this.connectPhase){
			case A: 
				return vabc.a_0;
			case B:
				return vabc.b_1;
			default:
				return vabc.c_2;
			}
			
		}
		
	
		@Override
		public Complex getCompCurInj() {
			this.currInj = new Complex(0.0d,0.0d);
			
			// when loadPQFactor = 0, it means the AC is stalled, thus no compensation current
			if(stage ==1) 
				return  this.currInj;
			else{
				
				Complex loadPQFactor = calcLoadCharacterFactor();
				// p+jq is pu based on system
				Complex pq = new Complex(p*loadPQFactor.getReal(),q*loadPQFactor.getImaginary());
				
				
				//TODO replace the pos-seq voltage with phase voltage
				Complex v = this.getBusPhaseVoltage();
				double vmag = v.abs();
				Complex compPower = pq.subtract(this.equivY.multiply(vmag*vmag).conjugate());
				
				// I = -conj( (p+j*q - conj(v^2*this.equivY))/v)
				
				// consider the situation where the load bus voltage is very low
				if(vmag<1.0E-4)
					 this.currInj = new Complex(0.0);
				else
				   this.currInj= compPower.divide(v).conjugate().multiply(-1.0d);
			}
			//IpssLogger.getLogger().fine
			//if(this.connectPhase == Phase.A)
			System.out.println("AC motor -"+this.getId()+"@"+this.getBus().getId()+", Phase - "+this.connectPhase+", dyn current injection: "+this.currInj);
			return this.currInj;
		}
	
		
		@Override
		public Object getOutputObject() {
		     return this.getCompCurInj();
		}
		
		
		@Override
		public Complex getEquivY() {
			Complex zstall = new Complex(this.Rstall,this.Xstall);
			Complex y = new Complex(1.0,0).divide(zstall);
			this.equivY = y.multiply(this.mva/this.getBus().getNetwork().getBaseMva()*3.0d);
			
			return this.equivY;
		}
	
		
	
		@Override
		public Complex getLoadPQ() {
			Complex loadPQFactor = calcLoadCharacterFactor();
			double v = this.getBusPhaseVoltage().abs();
			// when loadPQFactor = 0, it means the AC is stalled, thus no compensation current
			if(loadPQFactor.abs() ==0.0) 
				  this.loadPQ =this.getEquivY().multiply( v*v).conjugate();
			else
				this.loadPQ = new Complex(p*loadPQFactor.getReal(),q*loadPQFactor.getImaginary());
			
			return this.loadPQ;
			
		}
	
		
		@Override
		public String getScripts() {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public void setScripts(String value) {
			throw new UnsupportedOperationException();
			
		}

	     
		public double getPac() {
			return pac;
		}

		public void setPac(double pac) {
			this.pac = pac;
		}

		public double getQac() {
			return qac;
		}

		public void setQac(double qac) {
			this.qac = qac;
		}

		public int getStage() {
			return stage;
		}

		public void setStage(int stage) {
			this.stage = stage;
		}

		public double getPowerFactor() {
			return powerFactor;
		}

		public void setPowerFactor(double powerFactor) {
			this.powerFactor = powerFactor;
		}

		public double getVstall() {
			return Vstall;
		}

		public void setVstall(double vstall) {
			Vstall = vstall;
		}

		public double getRstall() {
			return Rstall;
		}

		public void setRstall(double rstall) {
			Rstall = rstall;
		}

		public double getXstall() {
			return Xstall;
		}

		public void setXstall(double xstall) {
			Xstall = xstall;
		}

		public double getTstall() {
			return Tstall;
		}

		public void setTstall(double tstall) {
			Tstall = tstall;
		}

		public double getLFadj() {
			return LFadj;
		}

		public void setLFadj(double lFadj) {
			LFadj = lFadj;
		}

		public double getKp1() {
			return Kp1;
		}

		public void setKp1(double kp1) {
			Kp1 = kp1;
		}

		public double getNp1() {
			return Np1;
		}

		public void setNp1(double np1) {
			Np1 = np1;
		}

		public double getKq1() {
			return Kq1;
		}

		public void setKq1(double kq1) {
			Kq1 = kq1;
		}

		public double getNq1() {
			return Nq1;
		}

		public void setNq1(double nq1) {
			Nq1 = nq1;
		}

		public double getKp2() {
			return Kp2;
		}

		public void setKp2(double kp2) {
			Kp2 = kp2;
		}

		public double getNp2() {
			return Np2;
		}

		public void setNp2(double np2) {
			Np2 = np2;
		}

		public double getKq2() {
			return Kq2;
		}

		public void setKq2(double kq2) {
			Kq2 = kq2;
		}

		public double getNq2() {
			return Nq2;
		}

		public void setNq2(double nq2) {
			Nq2 = nq2;
		}

		public double getVbrk() {
			return Vbrk;
		}

		public void setVbrk(double vbrk) {
			Vbrk = vbrk;
		}

		public double getFrst() {
			return Frst;
		}

		public void setFrst(double frst) {
			Frst = frst;
		}

		public double getVrst() {
			return Vrst;
		}

		public void setVrst(double vrst) {
			Vrst = vrst;
		}

		public double getTrst() {
			return Trst;
		}

		public void setTrst(double trst) {
			Trst = trst;
		}

		public double getCmpKpf() {
			return CmpKpf;
		}

		public void setCmpKpf(double cmpKpf) {
			CmpKpf = cmpKpf;
		}

		public double getCmpKqf() {
			return CmpKqf;
		}

		public void setCmpKqf(double cmpKqf) {
			CmpKqf = cmpKqf;
		}

		public double getFuvr() {
			return fuvr;
		}

		public void setFuvr(double fuvr) {
			this.fuvr = fuvr;
		}

		public double getVtr1() {
			return vtr1;
		}

		public void setVtr1(double vtr1) {
			this.vtr1 = vtr1;
		}

		public double getTtr1() {
			return ttr1;
		}

		public void setTtr1(double ttr1) {
			this.ttr1 = ttr1;
		}

		public double getVtr2() {
			return vtr2;
		}

		public void setVtr2(double vtr2) {
			this.vtr2 = vtr2;
		}

		public double getTtr2() {
			return ttr2;
		}

		public void setTtr2(double ttr2) {
			this.ttr2 = ttr2;
		}

		public double getVc1off() {
			return Vc1off;
		}

		public void setVc1off(double vc1off) {
			Vc1off = vc1off;
		}

		public double getVc2off() {
			return Vc2off;
		}

		public void setVc2off(double vc2off) {
			Vc2off = vc2off;
		}

		public double getVc1on() {
			return Vc1on;
		}

		public void setVc1on(double vc1on) {
			Vc1on = vc1on;
		}

		public double getVc2on() {
			return Vc2on;
		}

		public void setVc2on(double vc2on) {
			Vc2on = vc2on;
		}

		public double getTth() {
			return Tth;
		}

		public void setTth(double tth) {
			Tth = tth;
		}

		public double getTh1t() {
			return Th1t;
		}

		public void setTh1t(double th1t) {
			Th1t = th1t;
		}

		public double getTh2t() {
			return Th2t;
		}

		public void setTh2t(double th2t) {
			Th2t = th2t;
		}

		public double getUVRelayTimer1() {
			return UVRelayTimer1;
		}

		public void setUVRelayTimer1(double uVRelayTimer1) {
			UVRelayTimer1 = uVRelayTimer1;
		}

		public double getUVRelayTimer2() {
			return UVRelayTimer2;
		}

		public void setUVRelayTimer2(double uVRelayTimer2) {
			UVRelayTimer2 = uVRelayTimer2;
		}

	
	
	

}
