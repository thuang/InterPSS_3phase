package org.ipss.dynamic.threePhase.impl;

import org.interpss.numeric.datatype.Complex3x1;
import org.ipss.dynamic.threePhase.Machine3Phase;

import com.interpss.core.net.Network;
import com.interpss.dstab.mach.MachineData;
import com.interpss.dstab.mach.MachineType;
import com.interpss.dstab.mach.impl.DynamicMachineImpl;
import com.interpss.dstab.mach.impl.MachineImpl;

public class Machine3PhaseImpl extends DynamicMachineImpl implements Machine3Phase{

	private Complex3x1 igen3Ph = null;
	private Complex3x1 genPe3Ph = null;
	private Complex3x1 genQ3Ph = null;
	
	
	private double wB = 2*Math.PI*60.0d; // 60Hz as default
	private double rf = 0;
	private double rD = 0;
	
	private double r1q = 0;
	private double r2q = 0;
	
	private double Xd = 0;
	private double Xq = 0;
	private double Xad = 0;
	private double Xaq = 0;
	
	//field winding
	private double Xf = 0;
	private double Xfd = 0;
	
	private double XD = 0;
	private double X1q = 0;
	private double X2q = 0;
	
	
	public Machine3PhaseImpl(){
		Network net = this.getDStabBus().getNetwork();
		wB = 2*Math.PI*net.getFrequency();
	}

	@Override
	public Complex3x1 getIgen3Phase() {
		
		return this.igen3Ph;
	}

	@Override
	public Complex3x1 getGenPe3Phase() {
		
		return this.genPe3Ph;
	}

	@Override
	public void setGenPe3Phase(Complex3x1 genPe) {
		this.genPe3Ph = genPe;

	}

	@Override
	public Complex3x1 getGenQ3Phase() {

		return this.genQ3Ph;
	}

	@Override
	public void setGenQ3Phase(Complex3x1 genQ) {
		this.genQ3Ph = genQ;

	}
	
	private void calculatePrimitiveParameters(){
		MachineData mData = getMachData();
		
		Xd = getXd();
		Xq = getXq();
		double Xl = getXl();
		this.Xad = Xd - Xl;
		this.Xaq = Xq - Xl;
		
		double Xd1 = mData.getXd1();
		double Xd11 = mData.getXd11();
		
		double Xq1 = mData.getXq1();
		double Xq11 = mData.getXq11();
		
		// field winding
		this.Xf = this.Xad*this.Xad/(Xd-Xd1);
		this.Xfd = this.Xf - this.Xad; // or Xfd =1/( 1/(Xd1-Xl)- 1/Xad)
		
		//Tdop = (Xad + Xfd)/WB/rf
		this.rf = (Xad + Xfd)/wB/mData.getTd01();
		
		// D axis damper winding
		this.XD = 1/(1/(Xd11-Xl)-1/Xad-1/this.Xfd);
		
		//Tdopp = (X1d + 1/(1/Xfd+1/Xad))/WB/rD
		this.rD = (XD + 1/(1/Xfd+1/Xad))/wB/mData.getTd011();
				
		
		// Q axis damper winding. 
		// NOTE: the calculation is different depending the machine type
		// Q axis 1st damper winding is valid for round rotor machine only
		if(getMachType()==MachineType.EQ11_SALIENT_POLE){
			
			//Xq11 = Xl + 1/(1/Xaq + 1/X2q)
			
			this.X2q = 1/(1/(Xq11- Xl)-1/Xaq); 
			
			//Tqo11 = (Xaq + X2q)/r2q
			
			this.r2q = (Xaq + X2q)/wB/mData.getTq011();
		}
		else if(getMachType()==MachineType.EQ11_ED11_ROUND_ROTOR){
			
			//Xq1 = Xl + 1/(1/Xaq + 1/X2q)
			
			this.X1q = 1/(1/(Xq1- Xl)-1/Xaq);
			
			//Tqo1 = (Xaq + X1q)/wB/r1q
			
			this.r1q = (Xaq + X1q)/wB/mData.getTq01();
			
			
			//Xq11 = Xl + 1/(1/Xaq + 1/X1q+ 1/X2q)
			
			this.X2q = 1/(1/(Xq11- Xl)-1/Xaq-1/X1q);
			
			//Tqo11 = X2q+1/(1/Xaq + 1/X1q)/wB/r2q
			this.r2q = (X2q+1/(1/Xaq + 1/X1q))/wB/mData.getTq011();
			
		}
		
		

	}

}
