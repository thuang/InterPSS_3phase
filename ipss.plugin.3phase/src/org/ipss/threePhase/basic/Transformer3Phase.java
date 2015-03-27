package org.ipss.threePhase.basic;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;

import com.interpss.core.acsc.adpter.AcscXformer;

public interface Transformer3Phase extends AcscXformer{
	
	    public void set3PBranch(Branch3Phase ph3Branch);
	    
	    public void setZabc(Complex3x3 Zabc);
	    
	    public void setZabc(Complex Z1, Complex Z2, Complex Z0);
		
		public Complex3x3 getZabc() ;
		
		public Complex3x3 getYabc() ;
		
		public Complex3x3 getYffabc();
		
		public Complex3x3 getYttabc();
	    
        public Complex3x3 getYftabc();
		
		public Complex3x3 getYtfabc();

	
	

}
