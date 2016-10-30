package com.nlg.testNlg;

import com.nlg.wordnet.WordFormIdentifier;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test WordFormIdentifier class
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class WordFormIdentifierTest {

	private static WordFormIdentifier wordFormIdentifier = WordFormIdentifier.getInstance();

	@Test
	public void testStemWord() throws Exception {
		String actual = wordFormIdentifier.stemWord("ran");
		assertEquals(actual,"run");
	}

	@Test
	public void testIsNoun() throws Exception {
		boolean actual = wordFormIdentifier.isNoun("girl");
		assertEquals(actual,true);
	}

	@Test
	public void testIsVerb() throws Exception {
		boolean actual = wordFormIdentifier.isVerb("identify");
		assertEquals(actual,true);
	}

	@Test
	public void testIsAdverb() throws Exception {
		boolean actual = wordFormIdentifier.isAdverb("quickly");
		assertEquals(actual,true);
	}

	@Test
	public void testConvertToAdverb() throws Exception {
		String actual = wordFormIdentifier.convertToAdverb("quick");
		assertEquals(actual,"quickly");
	}

	@Test
	public void testGetPronoun() throws Exception {
		String actual = wordFormIdentifier.getPronoun("man");
		assertEquals(actual,"he");
	}
}