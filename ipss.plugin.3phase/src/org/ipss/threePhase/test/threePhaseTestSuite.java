package org.ipss.threePhase.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
	TwoBus_3Phase_Test.class,
	ThreeBus_3Phase_Test.class,
	TestODM3PhaseDstabMapper.class,
	IEEE9Bus_3phase_test.class,
	TestSinglePhaseACMotorModel.class,
	IEEE9_3Phase_1PAC_test.class

	
})
public class threePhaseTestSuite {

}
