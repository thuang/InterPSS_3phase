package org.ipss.threePhase.basic;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;

public class IEEEFeederLineCode {
	
	/**
	 * New linecode.mtx601 nphases=3 BaseFreq=60 
		~ rmatrix = (0.3465 | 0.1560 0.3375 | 0.1580 0.1535 0.3414 ) 
		~ xmatrix = (1.0179 | 0.5017 1.0478 | 0.4236 0.3849 1.0348 ) 
		~ units=mi 
	 */
	
	
	public static Complex3x3 zMtx601 = new Complex3x3(new Complex[][]{{new Complex(0.3465,1.0179), new Complex(0.1560,0.5017), new Complex(0.1580,0.4236)}, 
	                                                                  {new Complex(0.1560,0.5017), new Complex(0.3375,1.0478), new Complex(0.1535,0.3849)},
	                                                                  {new Complex(0.1580,0.4236), new Complex(0.1535,0.3849),new Complex(0.3414,1.0348)}});
	
	
   /**
    * New linecode.mtx602 nphases=3 BaseFreq=60 
	~ rmatrix = (0.7526 | 0.1580 0.7475 | 0.1560 0.1535 0.7436 ) 
	~ xmatrix = (1.1814 | 0.4236 1.1983 | 0.5017 0.3849 1.2112 ) 
	~ units=mi 
    */
	public static Complex3x3 zMtx602 = new Complex3x3(new Complex[][]{{new Complex(0.7526,1.1814),new Complex(0.1580,0.4236),new Complex(0.1560,0.5017)},
			                                                          {new Complex(0.1580,0.4236),new Complex(0.7475,1.1983),new Complex(0.1535,0.3849)},
			                                                          {new Complex(0.1560,0.5017),new Complex(0.1535,0.3849),new Complex(0.7436,1.2112)}});
			                                   
	
	
	
	/**
	 * New linecode.mtx606 nphases=3 BaseFreq=60 
	~ rmatrix = (0.7982 | 0.3192 0.7891 | 0.2849 0.3192 0.7982 ) 
	~ xmatrix = (0.4463 | 0.0328 0.4041 | -0.0143 0.0328 0.4463 ) 
	~ Cmatrix = [257 | 0 257 | 0 0 257]
	~ units=mi 
	 */
	public static Complex3x3 zMtx606 = new Complex3x3(new Complex[][]{{new Complex(0.7982,0.4463),new Complex(0.3192,0.0328),new Complex(0.2849,-0.0143)},
                                                                      {new Complex(0.3192,0.0328),new Complex(0.7891,0.4041),new Complex(0.3192,0.0328)},
                                                                      {new Complex(0.2849,-0.0143),new Complex(0.3192,0.0328),new Complex(0.7982,0.4463 )}});

	
}
