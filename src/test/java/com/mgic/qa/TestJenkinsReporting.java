package com.mgic.qa;

import org.junit.Test;

public class TestJenkinsReporting {

	@Test
	public void test() throws Exception {
		JenkinsReporting jr = new JenkinsReporting();
		jr.printBuildDetails();
	}

}
