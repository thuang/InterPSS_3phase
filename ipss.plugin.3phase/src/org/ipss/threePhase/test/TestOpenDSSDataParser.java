package org.ipss.threePhase.test;

import static org.junit.Assert.assertTrue;

import java.util.Map.Entry;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.LineConfiguration;
import org.ipss.threePhase.data_parser.OpenDSSDataParser;
import org.junit.Test;

public class TestOpenDSSDataParser {
	
	@Test
	public void test_LineCodeParser(){
		OpenDSSDataParser parser = new OpenDSSDataParser();
		parser.parseFeederData("testData\\feeder\\IEEE123\\IEEELineCodes.DSS", "", "", "");
		
		System.out.print("line code table:\n" );
		
		for(Entry<String, LineConfiguration> configSet: parser.getLineConfigTable().entrySet()){
			System.out.println(configSet.getKey()+": " +configSet.getValue().toString());
		}
		
		/*
		 * New linecode.1 nphases=3 BaseFreq=60
			~ rmatrix = [0.086666667 | 0.029545455 0.088371212 | 0.02907197 0.029924242 0.087405303]
			~ xmatrix = [0.204166667 | 0.095018939 0.198522727 | 0.072897727 0.080227273 0.201723485]
			~ cmatrix = [2.851710072 | -0.920293787  3.004631862 | -0.350755566  -0.585011253 2.71134756]
		 */
		LineConfiguration linecode_1 = parser.getLineConfigTable().get("1");
		
		assertTrue(linecode_1.getNphases()==3);
		Complex3x3 zabc1 = new Complex3x3(new Complex[][]{
			{new Complex(0.086666667, 0.204166667), new Complex(0.029545455, 0.095018939), new Complex(0.02907197, 0.072897727)},
			{new Complex(0.029545455, 0.095018939), new Complex(0.088371212, 0.198522727), new Complex(0.029924242, 0.080227273)},
			{new Complex(0.02907197, 0.072897727),  new Complex(0.029924242, 0.080227273) , new Complex(0.087405303, 0.201723485)}});
		
		Complex3x3 yabc1 = new Complex3x3(new Complex[][]{
			{new Complex(0.0, 2.851710072), new Complex(0.0, -0.920293787), new Complex(0.0, -0.350755566)},
			{new Complex(0.0, -0.920293787), new Complex(0.0, 3.004631862), new Complex(0.0, -0.585011253)},
			{new Complex(0.0, -0.350755566),  new Complex(0.0, -0.585011253) , new Complex(0.0, 2.71134756)}});
			
		assertTrue(linecode_1.getZ3x3Matrix().subtract(zabc1).absMax()<1.0E-6);
		assertTrue(linecode_1.getShuntY3x3Matrix().subtract(yabc1).absMax()<1.0E-6);
		
		
		/*
		 * New linecode.7 nphases=2 BaseFreq=60
			~ rmatrix = [0.086666667 | 0.02907197  0.087405303]
			~ xmatrix = [0.204166667 | 0.072897727  0.201723485]
			~ cmatrix = [2.569829596 | -0.52995137  2.597460011]
		 */
		
       LineConfiguration linecode_7 = parser.getLineConfigTable().get("7");
		
		assertTrue(linecode_7.getNphases()==2);
		Complex3x3 zabc7 = new Complex3x3(new Complex[][]{
			{new Complex(0.086666667, 0.204166667), new Complex(0.02907197, 0.072897727), new Complex(0.)},
			{new Complex(0.02907197, 0.072897727), new Complex(0.087405303, 0.201723485), new Complex(0.)},
			{new Complex(0.0),  new Complex(0.0) , new Complex(0.)}});
			
		assertTrue(linecode_7.getZ3x3Matrix().subtract(zabc7).absMax()<1.0E-6);
		
		/*
		 * New linecode.9 nphases=1 BaseFreq=60
			~ rmatrix = [0.251742424]
			~ xmatrix = [0.255208333]
			~ cmatrix = [2.270366128]
		 */
		
        LineConfiguration linecode_9 = parser.getLineConfigTable().get("9");
		
		assertTrue(linecode_9.getNphases()==1);
		Complex3x3 zabc9 = new Complex3x3(new Complex[][]{
			{new Complex(0.251742424, 0.255208333), new Complex(0.0), new Complex(0.)},
			{new Complex(0.0), new Complex(0.0), new Complex(0.)},
			{new Complex(0.0),  new Complex(0.0) , new Complex(0.)}});
		
		Complex3x3 yabc9 = new Complex3x3(new Complex(0,2.270366128), new Complex(0.), new Complex(0.));
			
		assertTrue(linecode_9.getZ3x3Matrix().subtract(zabc9).absMax()<1.0E-6);
		
		assertTrue(linecode_9.getShuntY3x3Matrix().subtract(yabc9).absMax()<1.0E-6);
	}

}
