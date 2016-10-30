package com.nlg.testNlg;

import com.nlg.simplenlg.TenseChecker;
import simplenlg.features.Tense;

import static org.junit.Assert.assertEquals;

/**
 * Test TenseChecker class
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class TenseCheckerTest {

	@org.junit.Test
	public void testGetTense() throws Exception {
		TenseChecker tenseChecker = new TenseChecker();
		Tense tense = tenseChecker.getTense("ate");
		assertEquals(tense, Tense.PAST);

		tense = tenseChecker.getTense("eats");
		assertEquals(tense,Tense.PRESENT);
	}
}