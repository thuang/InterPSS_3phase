package org.ipss.threePhase.basic.impl;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Transformer3Phase;

import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.adpter.impl.AcscXformerImpl;

public class Transformer3PhaseImpl extends AcscXformerImpl implements Transformer3Phase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Branch3Phase ph3Branch= null;
	
	private Complex y0 =null;
	private Complex y1 =null; // transformer primitive leakage admittance of a phase 
	
	private Complex3x3 LVBusVabc2HVBusVabcMatrix = null;
	private Complex3x3 LVBusIabc2HVBusVabcMatrix = null;
	private Complex3x3 LVBusVabc2HVBusIabcMatrix = null;
	private Complex3x3 LVBusIabc2HVBusIabcMatrix = null;
	private Complex3x3 HVBusVabc2LVBusVabcMatrix = null;
	private Complex3x3 LVBusIabc2LVBusVabcMatrix = null;
	
	
	private Complex3x3 turnRatioMatrix = null;
	
	
	public Transformer3PhaseImpl(Branch3Phase threePhBranch){
		this.ph3Branch =threePhBranch;
		
	}
	
	
	public Transformer3PhaseImpl() {
		
	}
    
	@Override
	public void set3PBranch(Branch3Phase ph3Branch) {
		this.ph3Branch = ph3Branch;
		setBranch(ph3Branch);
		
		this.y1 = this.ph3Branch.getY();
		if(this.ph3Branch.getY0()!=null) 
			y0 = this.ph3Branch.getY0();
		else
			y0 = y1;
		
	}

	@Override
	public void setZabc(Complex3x3 Zabc) {
		this.ph3Branch.setZabc(Zabc);
		
	}

	@Override
	public void setZabc(Complex Z1, Complex Z2, Complex Z0) {
		this.ph3Branch.setZabc(Z1,Z2,Z0);
		
	}

	@Override
	public Complex3x3 getZabc() {
		
		return this.ph3Branch.getZabc();
	}

	@Override
	public Complex3x3 getYabc() {
		
		return this.ph3Branch.getBranchYabc();
	}
	
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// using look up table to build the Yff, Ytt, yft, ytf for standard connected transformers
	// referred to Selva S. Moorthy, David Hoadley, "A new phase-coordinate transformer modeling for Ybus Anaysks", IEEE Trans. on Power systems, vol.17, No.4, 2002
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public Complex3x3 getYffabc() {
	    Complex3x3 yffabc = null;
		//Yg
		if(this.ph3Branch.getXfrFromConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED){
			yffabc = getY1().multiply(1/this.ph3Branch.getFromTurnRatio()/this.ph3Branch.getFromTurnRatio());
	
		}
		
		//Y
		else if(this.ph3Branch.getXfrFromConnectCode() == XfrConnectCode.WYE_UNGROUNDED){
			yffabc = getY2().multiply(1/this.ph3Branch.getFromTurnRatio()/this.ph3Branch.getFromTurnRatio());
			
		}
		
		//D
        else if(this.ph3Branch.getXfrFromConnectCode() == XfrConnectCode.DELTA){
        	yffabc = getY2().multiply(1/this.ph3Branch.getFromTurnRatio()/this.ph3Branch.getFromTurnRatio());
    		
		} else
			try {
				throw new Exception("Unsupported connection type at the from side!");
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		

		return yffabc;
	}

	@Override
	public Complex3x3 getYttabc() {
		 Complex3x3 yttabc = null;
			//Yg
			if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED){
				yttabc = getY1().multiply(1/this.ph3Branch.getToTurnRatio()/this.ph3Branch.getToTurnRatio());
		
			}
			
			//Y
			else if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_UNGROUNDED){
				yttabc = getY2().multiply(1/this.ph3Branch.getToTurnRatio()/this.ph3Branch.getToTurnRatio());
				
			}
			
			//D
	        else if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.DELTA){
	        	yttabc = getY2().multiply(1/this.ph3Branch.getToTurnRatio()/this.ph3Branch.getToTurnRatio());
	    		
			} else
				try {
					throw new Exception("Unsupported connection type at the from side!");
				} catch (Exception e) {
					
					e.printStackTrace();
				}
			
			return yttabc;
	}
	
	
	@Override
	public Complex3x3 getYftabc() {
		
		 Complex3x3 yftabc = null;
			//Yg-
		 if(this.ph3Branch.getXfrFromConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED){
			  //YgYg
			    if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED)
				   yftabc = getY1().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
			  //YgY
			    else if (this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_UNGROUNDED)
			    	yftabc = getY2().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
			   //YgD1 
			    else if (this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.DELTA)
			    	yftabc = getY3().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
			   //YgD11  
			    else if (this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.DELTA11)
			    	yftabc = getY3().transpose().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
			}
			
			//Y-
			else if(this.ph3Branch.getXfrFromConnectCode() == XfrConnectCode.WYE_UNGROUNDED ){
				//Yg or Y
				if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED ||
						this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_UNGROUNDED)
					    // note: y2* = y2
				        yftabc = getY2().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
				// D1
				 else if (this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.DELTA)
				    	yftabc = getY3().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
			   // D11   
				 else if (this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.DELTA11)
				    	yftabc = getY3().transpose().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
				
			}
			
			//D
	        else if(this.ph3Branch.getXfrFromConnectCode() == XfrConnectCode.DELTA){
	        	
	        	//D-Yg or Y
				if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED ||
						this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_UNGROUNDED)
	        	     yftabc = getY3().transpose().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
				
				else if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.DELTA)
					 yftabc = getY2().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
	    		
			} 
		 
			//D11
	        else if(this.ph3Branch.getXfrFromConnectCode() == XfrConnectCode.DELTA11){
	        	
	        	//D-Yg or Y
				if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED ||
						this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_UNGROUNDED)
	        	     yftabc = getY3().transpose().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
				
				else if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.DELTA || this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.DELTA11)
					 yftabc = getY2().multiply(-1/this.getFromTurnRatio()/this.getToTurnRatio());
	    		
			} 
	        
	        
	        else
				try {
					throw new Exception("Unsupported connection type at the from side!");
				} catch (Exception e) {
					
					e.printStackTrace();
				}
			
			return yftabc;
	}


	@Override
	public Complex3x3 getYtfabc() {
		
		return getYftabc().transpose();
	}
	
	/**
	 * Refer to the paper: M.S.Chen, W.E.Dillon, "Power system modeling," Proc. of IEEE,Vol.62, No.7, 1974
	 * 
	 * AND paper Selva Moorthy et al "a new phase coordinate transformer model for Ybus analysis", IEEE PWRS Vol.17, No.4, Nov.2002
	 * 
	 * Y1 corresponding to self and mutual admittance on the Yn side
	 * It is symmetric with :
	 *     Y1ii = (y0+2y1)/3
	 *     Y1ij = (y0-y1)/3
	 * @return
	 */
	private  Complex3x3  getY1(){

		if(y1 != null){
			if(y0 == null) y0 = y1;
			Complex Y1ii = (y0.add(y1.multiply(2))).divide(3);
			Complex Y1ij = (y0.subtract(y1)).divide(3);
			return new Complex3x3(Y1ii,Y1ij);
		}
		return null;
	}
	
	
	/**
	 * Y2 corresponding to self and mutual admittance on the Y or delta side
	 * It is symmetric with :
	 *     Y1ii = (2y1)/3
	 *     Y1ij = (-y1)/3
	 * @return
	 */
    private  Complex3x3  getY2(){
    	
    	if(y1 != null){
	    	Complex Y1ii = (y1.multiply(2.0d)).divide(3);
			Complex Y1ij = (y1.multiply(-1.0d)).divide(3);
			return new Complex3x3(Y1ii,Y1ij);
	    }
    	return null;
	}
    
    
	/**
	 * Y3 corresponding to mutual admittance of the Y and delta connections
	 * It is  with the structure: 
	 *              [y1 -y1  0]
	 *    1/sqrt(3)*[0 y1, -y1]
	 *              [-y1, 0,y1]
	 * @return
	 */
    private  Complex3x3  getY3(){
    	if(y1 != null)
    	return new Complex3x3(new Complex[][]{
    			{y1.multiply(Math.sqrt(3)/3),     y1.multiply(-1*Math.sqrt(3)/3),        new Complex(0.0, 0.0)},
    			{new Complex(0,0),                y1.multiply(Math.sqrt(3)/3),    y1.multiply(-1*Math.sqrt(3)/3) },
    			{  y1.multiply(-1*Math.sqrt(3)/3),            new Complex(0,0),            y1.multiply(Math.sqrt(3)/3)}});

		return null;
	}

    //TODO consider to change the naming to HV/LV
	@Override
	public Complex3x3 getLVBusVabc2HVBusVabcMatrix() {
		     //Delta-Delta
		     if(this.isHVDeltaConnectted() && this.isLVDeltaConnectted()){
		    	 
		     }
		     // Delta-Grounded Wye
		     else if (this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
		    	 
		     }
		    
		     // Grounded Wye - Grounded Wye
		     else if (!this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
		    	 this.LVBusVabc2HVBusVabcMatrix = this.getTurnRatioMatrix();
		     }
		    	 
		return this.LVBusVabc2HVBusVabcMatrix;
	}


	@Override
	public Complex3x3 getLVBusIabc2HVBusVabcMatrix() {
		   //Delta-Delta
	     if(this.isHVDeltaConnectted() && this.isLVDeltaConnectted()){
	    	 
	     }
	     // Delta-Grounded Wye
	     else if (this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 
	     }
	    
	     // Grounded Wye - Grounded Wye
	     else if (!this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 this.LVBusIabc2HVBusVabcMatrix = this.getTurnRatioMatrix().multiply(this.getZabc());
	     }
		return this.LVBusIabc2HVBusVabcMatrix;
	}


	@Override
	public Complex3x3 getLVBusVabc2HVBusIabcMatrix() {
		  //Delta-Delta
	     if(this.isHVDeltaConnectted() && this.isLVDeltaConnectted()){
	    	 
	     }
	     // Delta-Grounded Wye
	     else if (this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 
	     }
	    
	     // Grounded Wye - Grounded Wye
	     else if (!this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 this.LVBusVabc2HVBusIabcMatrix = new Complex3x3();
	     }
		return this.LVBusVabc2HVBusIabcMatrix;
	}


	@Override
	public Complex3x3 getLVBusIabc2HVBusIabcMatrix() {
		 //Delta-Delta
	     if(this.isHVDeltaConnectted() && this.isLVDeltaConnectted()){
	    	 
	     }
	     // Delta-Grounded Wye
	     else if (this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 
	     }
	    
	     // Grounded Wye - Grounded Wye
	     else if (!this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 this.LVBusIabc2HVBusIabcMatrix = Complex3x3.createUnitMatrix().multiply(1/this.getTurnRatio());
	     }
		return  this.LVBusIabc2HVBusIabcMatrix;
	}


	@Override
	public Complex3x3 getHVBusVabc2LVBusVabcMatrix() {
		 //Delta-Delta
	     if(this.isHVDeltaConnectted() && this.isLVDeltaConnectted()){
	    	 
	     }
	     // Delta-Grounded Wye
	     else if (this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 
	     }
	    
	     // Grounded Wye - Grounded Wye
	     else if (!this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 this.HVBusVabc2LVBusVabcMatrix = Complex3x3.createUnitMatrix().multiply(1/this.getTurnRatio());
	     }
		return  this.HVBusVabc2LVBusVabcMatrix;
	}


	@Override
	public Complex3x3 getLVBusIabc2LVBusVabcMatrix() {
		//Delta-Delta
	     if(this.isHVDeltaConnectted() && this.isLVDeltaConnectted()){
	    	 
	     }
	     // Delta-Grounded Wye
	     else if (this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 
	     }
	    
	     // Grounded Wye - Grounded Wye
	     else if (!this.isHVDeltaConnectted() && !this.isLVDeltaConnectted()){
	    	 this.LVBusIabc2LVBusVabcMatrix = this.getZabc();
	     }
		return null;
	}
	private double getTurnRatio(){
		return this.ph3Branch.getFromTurnRatio()/this.ph3Branch.getToTurnRatio();
	}
	private Complex3x3 getTurnRatioMatrix(){
		if(turnRatioMatrix ==null){
			
			//Yg-Yg
			if(this.ph3Branch.getXfrToConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED){
				if(this.ph3Branch.getXfrFromConnectCode() == XfrConnectCode.WYE_SOLID_GROUNDED){
					turnRatioMatrix = Complex3x3.createUnitMatrix().multiply(getTurnRatio());
				}
			}
		}
		return turnRatioMatrix; 
	}
	private boolean isHVDeltaConnectted(){
		if(this.ph3Branch.getFromAclfBus().getBaseVoltage() > this.ph3Branch.getToAclfBus().getBaseVoltage()){
			if(this.ph3Branch.getXfrFromConnectCode()==XfrConnectCode.DELTA)
				return true;
		}
		else{
			if(this.ph3Branch.getXfrToConnectCode()==XfrConnectCode.DELTA)
				return true;
		}
		return false;
	}
	
    private boolean isLVDeltaConnectted(){
    	if(this.ph3Branch.getFromAclfBus().getBaseVoltage() < this.ph3Branch.getToAclfBus().getBaseVoltage()){
			if(this.ph3Branch.getXfrFromConnectCode()==XfrConnectCode.DELTA)
				return true;
		}
		else{
			if(this.ph3Branch.getXfrToConnectCode()==XfrConnectCode.DELTA)
				return true;
		}
		return false;
	}
    
    private boolean isHVWindingOnFromBusSide(){
    	if(this.ph3Branch.getFromAclfBus().getBaseVoltage() > this.ph3Branch.getToAclfBus().getBaseVoltage())
    		return true;
    	else
    		return false;
    }
 







}
