package org.ipss.aclf.threePhase;

import com.interpss.core.aclf.adpter.AclfXformer;
import com.interpss.core.acsc.BusGroundCode;
import com.interpss.core.acsc.XfrConnectCode;

public interface ThreePhAclfXformer extends AclfXformer{
	
	
	public XfrConnectCode getFromSideConnectCode();
	
	public XfrConnectCode getToSideConnectCode();
	
	public void setFromSideConnectCode(XfrConnectCode code);
	
	public void setToSideConnectCode(XfrConnectCode code);
	
	
	public BusGroundCode getFromSideGroundingCode();
	
	public BusGroundCode getToSideGroundingCode();
	
	public void setFromSideGroundingCode(BusGroundCode gCode);
	
	public void setToSideGroundingCode(BusGroundCode gCode);
	

	
	

}
