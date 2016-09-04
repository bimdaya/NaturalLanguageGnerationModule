package com.nlg.simplenlg;

import com.nlg.common.NLGConstants;
import com.nlg.common.NLGException;
import com.nlg.wordnet.WordFormIdentifier;
import simplenlg.features.Feature;
import simplenlg.features.Tense;
import simplenlg.framework.InflectedWordElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

public class TenseChecker {

	public Tense getTense(String verb) throws NLGException {
		String word = verb.split(NLGConstants.SPACE)[0];
		WordFormIdentifier wordFormIdentifier = new WordFormIdentifier();
		String baseForm = wordFormIdentifier.stemWord(word);
		if(word != null){
			Lexicon lexicon = Lexicon.getDefaultLexicon();
			WordElement wordElement = lexicon.getWord(baseForm, LexicalCategory.VERB);
			InflectedWordElement inflectedWordElement = new InflectedWordElement(wordElement);
			Realiser realiser = new Realiser(lexicon);

			inflectedWordElement.setFeature(Feature.TENSE, Tense.PRESENT);
			String tense = realiser.realise(inflectedWordElement).getRealisation();

			if(word.equals(tense) ){
				return Tense.PRESENT;
			}

			inflectedWordElement.setFeature(Feature.TENSE, Tense.PAST);
			tense = realiser.realise(inflectedWordElement).getRealisation();

			if(word.equals(tense) ){
				return Tense.PAST;
			}

			inflectedWordElement.setFeature(Feature.TENSE, Tense.FUTURE);
			tense = realiser.realise(inflectedWordElement).getRealisation();

			if(word.equals(tense) ){
				return Tense.FUTURE;
			}


		}
		return null;
	}
}