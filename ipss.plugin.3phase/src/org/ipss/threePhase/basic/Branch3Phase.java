package org.ipss.threePhase.basic;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.acsc.AcscBranch;
import com.interpss.dstab.DStabBranch;

public interface Branch3Phase extends DStabBranch{
	
    public void setZabc(Complex3x3 Zabc);
    
    public void setZabc(Complex Z1, Complex Z2, Complex Z0);
	
	public Complex3x3 getZabc() ;
	
	public Complex3x3 getBranchYabc() ;
	
	public Complex3x3 getFromShuntYabc();
	public Complex3x3 getToShuntYabc();
	
	public void setFromShuntYabc( Complex3x3 fYabc) ;
	
	public void setToShuntYabc( Complex3x3 tYabc) ;
	

	public Complex3x3 getYffabc();
	
	public Complex3x3 getYttabc();
	
	public Complex3x3 getYftabc();
	
	public Complex3x3 getYtfabc();
	
	public Complex3x1 getCurrentAbcIntoNetFromSide();
	public void setCurrentAbcIntoNetFromSide(Complex3x1 IabcFromBus);
	
	public Complex3x1 getCurrentAbcIntoNetToSide();
	public void setCurrentAbcIntoNetToSide(Complex3x1 IabcToBus);
	
	public Transformer3Phase to3PXformer();
	
	/**
	 * The mapping matrix relating Vabc of to bus to the Vabc of from bus
	 * @return
	 */
	Complex3x3 getToBusVabc2FromBusVabcMatrix();
	
	/**
	 * The mapping matrix relating current flowing into ToBus to the Vabc of frombus
	 * @return
	 */
	Complex3x3 getToBusIabc2FromBusVabcMatrix();
	
	/**
	 * The mapping matrix relating relating Vabc of ToBus to the current (Iabc) into fromBus
	 * @return
	 */
	Complex3x3 getToBusVabc2FromBusIabcMatrix();
	
	/**
	 * The mapping matrix relating Iabc of ToBus to the current (Iabc) into fromBus
	 * @return
	 */
	Complex3x3 getToBusIabc2FromBusIabcMatrix();
	
	/**
	 * The mapping matrix relating  Vabc of fromBus to the Vabc of toBus
	 * @return
	 */
	Complex3x3 getFromBusVabc2ToBusVabcMatrix();
	
	/**
	 * The mapping matrix relating  Iabc of Tobus to the Vabc of toBus
	 * @return
	 */
	Complex3x3 getToBusIabc2ToBusVabcMatrix();
	

}
