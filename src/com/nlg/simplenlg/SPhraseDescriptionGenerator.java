package com.nlg.simplenlg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.nlg.common.ClauseEnum;
import com.nlg.dataHandler.ParagraphBean;
import com.nlg.common.NLGConstants;
import com.nlg.common.NLGException;
import com.nlg.wordnet.WordFormIdentifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import simplenlg.features.Feature;
import simplenlg.features.Tense;
import simplenlg.framework.*;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;

import java.util.*;

/**
 * Handle grammar rules of sentences
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class SPhraseDescriptionGenerator {
	static Log log = LogFactory.getLog(SPhraseDescriptionGenerator.class);
	private static Realiser realiser = new Realiser();
	private static Lexicon lexicon = Lexicon.getDefaultLexicon();
	private static NLGFactory nlgFactory = new NLGFactory(lexicon);
	private static WordFormIdentifier wordFormIdentifier = WordFormIdentifier.getInstance();
	private String rootElement;
	private String rootElementName = NLGConstants.NULL;

	/**
	 * create sentence using modified SPhraseSpec list
	 *
	 * @param paragraphBean list of data beans
	 * @return paragraph
	 * @throws NLGException
	 */
	public String finalizeSentence(ParagraphBean paragraphBean) throws NLGException {
		if (paragraphBean == null) {
			String msg = "Paragraph is not created";
			log.error(msg);
			throw new NLGException(msg);
		}
		StringBuilder stringBuffer = new StringBuilder();
		List<SPhraseSpec> sentenceList = paragraphBean.getSentenceList();
		if (!(paragraphBean.getParagraphClauseType() == ClauseEnum.HOW)) {

			if (paragraphBean.getRootElement() == null) {
				String msg = "Invalid xml content.";
				log.error(msg);
				throw new NLGException(msg);
			}

			//if clause type is WHAT set root element and pronouns
			rootElement = paragraphBean.getRootElement();
			rootElementName = paragraphBean.getRootElementName();
			sentenceList = setObjectConjunctions(sentenceList);
			sentenceList = setSubjectConjunctions(sentenceList);
			sentenceList = setPronounsDeterminers(sentenceList);

			//generate sentence
			for (SPhraseSpec sPhraseSpec : sentenceList) {
				stringBuffer.append(realiser.realiseSentence(sPhraseSpec));
			}
		} else {
			//if the clause type is HOW
			stringBuffer.append("Followings are the steps to follow when fixing the error\n");
			for (SPhraseSpec sentence : sentenceList) {
				stringBuffer.append(realiser.realise(sentence.getSubject()).toString().trim()).append("\t Command : \"")
				            .append(realiser.realise(sentence.getObject()).toString().trim()).append("\"\n");
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * set the conjunctions of SPhraseSpec list
	 *
	 * @param currentSentenceList list of SPhraseSpec
	 * @return list of SPhraseSpec
	 */
	private List<SPhraseSpec> setObjectConjunctions(List<SPhraseSpec> currentSentenceList) throws NLGException {
		int currentSentenceSize = currentSentenceList.size();

		if (currentSentenceSize > 1) {
			int sentenceCount = 1;
			for (int index = 0; index < currentSentenceSize; index++) {
				SPhraseSpec currentSentence = currentSentenceList.get(index);
				SPhraseSpec nextSentence = currentSentenceList.get(sentenceCount);

				//get the subject and verb of the current sentence by replacing object with space in a sentence
				String currentPhraseSpec = realiser.realiseSentence(currentSentence)
				                                   .replace(realiser.realise(currentSentence.getObject()).toString(),
				                                            NLGConstants.SPACE).trim();
				String nextPhraseSpec = realiser.realiseSentence(nextSentence)
				                                .replace(realiser.realise(nextSentence.getObject()).toString(),
				                                         NLGConstants.SPACE).trim();
				if (index == sentenceCount) {
					continue;
				}
				if (currentPhraseSpec.equals(nextPhraseSpec)) {
					//if current and next subject+verb are equal combine objects using 'and'
					SPhraseSpec newSentence = new SPhraseSpec(nlgFactory);
					CoordinatedPhraseElement coordinate = nlgFactory.createCoordinatedPhrase();
					newSentence.setSubject(currentSentence.getSubject());
					newSentence.setVerb(currentSentence.getVerb());

					if (currentSentence.isNegated()) {
						newSentence.setNegated(true);
					}
					coordinate.addCoordinate(currentSentence.getObject());
					coordinate.addCoordinate(nextSentence.getObject());
					newSentence.setObject(coordinate);
					currentSentenceList.remove(--sentenceCount);
					currentSentenceList.set(sentenceCount, newSentence);
					realiser.realiseSentence(newSentence);
					sentenceCount++;
				}
				sentenceCount++;

				if (sentenceCount < index || sentenceCount >= currentSentenceSize - 1) {
					break;
				}
			}
		}
		return currentSentenceList;
	}

	/**
	 * set the conjunctions of SPhraseSpec list
	 *
	 * @param currentSentenceList list of SPhraseSpec
	 * @return list of SPhraseSpec
	 */
	private List<SPhraseSpec> setSubjectConjunctions(List<SPhraseSpec> currentSentenceList) throws NLGException {
		int currentSentenceSize = currentSentenceList.size();

		if (currentSentenceSize > 1) {
			int sentenceCount = 1;
			for (int index = 0; index < currentSentenceSize; index++) {
				SPhraseSpec currentSentence = currentSentenceList.get(index);
				SPhraseSpec nextSentence = currentSentenceList.get(sentenceCount);

				//get the verb + object of the current sentence by replacing object with space in a sentence
				String currentPhraseSpec = realiser.realiseSentence(currentSentence)
				                                   .replace(realiser.realise(currentSentence.getSubject()).toString(),
				                                            NLGConstants.SPACE).trim();
				String nextPhraseSpec = realiser.realiseSentence(nextSentence)
				                                .replace(realiser.realise(nextSentence.getSubject()).toString(),
				                                         NLGConstants.SPACE).trim();
				if (index == sentenceCount) {
					continue;
				}
				if (currentPhraseSpec.equals(nextPhraseSpec) && currentSentence.isNegated()==nextSentence.isNegated()) {
					//if current and next verb+object are equal combine objects using 'and'
					SPhraseSpec newSentence = new SPhraseSpec(nlgFactory);
					CoordinatedPhraseElement coordinate = nlgFactory.createCoordinatedPhrase();
					newSentence.setObject(currentSentence.getSubject());
					newSentence.setVerb(currentSentence.getVerb());

					if (currentSentence.isNegated()) {
						newSentence.setNegated(true);
					}
					coordinate.addCoordinate(currentSentence.getSubject());
					coordinate.addCoordinate(nextSentence.getSubject());
					newSentence.setSubject(coordinate);
					newSentence.setPlural(true);
					currentSentenceList.remove(--sentenceCount);
					currentSentenceList.set(sentenceCount, newSentence);
					realiser.realiseSentence(newSentence);
					sentenceCount++;
				}
				sentenceCount++;

				if (sentenceCount < index || sentenceCount >= currentSentenceSize - 1) {
					break;
				}
			}
		}
		return currentSentenceList;
	}

	/**
	 * find if there are duplicate verbs and objects
	 *
	 * @param sPhraseSpecList list of SPhraseSpec
	 * @return list of SPhraseSpec
	 */
	private ListMultimap<String, SPhraseSpec> findDuplicateObject(List<SPhraseSpec> sPhraseSpecList) {

		ListMultimap<String, SPhraseSpec> duplicates = ArrayListMultimap.create();

		for (SPhraseSpec sPhraseSpec : sPhraseSpecList) {
			//get the subject and verb of the current sentence by replacing object with space in a sentence
			String wordSet = realiser.realiseSentence(sPhraseSpec)
			                         .replace(realiser.realise(sPhraseSpec.getObject()).toString(), NLGConstants.SPACE)
			                         .trim();

			//multi map itself add values to a list according to the key
			duplicates.put(wordSet, sPhraseSpec);

		}
		return duplicates;
	}

	/**
	 * find if there are duplicate verbs and subjects
	 *
	 * @param sPhraseSpecList list of SPhraseSpec
	 * @return list of SPhraseSpec
	 */
	private ListMultimap<String, SPhraseSpec> findDuplicatesNounPhrase(List<SPhraseSpec> sPhraseSpecList) {

		ListMultimap<String, SPhraseSpec> duplicates = ArrayListMultimap.create();

		for (SPhraseSpec sPhraseSpec : sPhraseSpecList) {
			String wordSet = realiser.realiseSentence(sPhraseSpec)
			                         .substring(realiser.realise(sPhraseSpec.getSubject()).toString().length()).trim();
			//multi map itself add values to a list according to the key
			duplicates.put(wordSet, sPhraseSpec);

		}
		return duplicates;
	}

	/**
	 * put SPhraseSpecs according to an order
	 *
	 * @param sPhraseSpecList list of SPhraseSpec
	 * @return list of SPhraseSpec
	 */
	private ListMultimap<Integer, SPhraseSpec> setOrder(List<SPhraseSpec> sPhraseSpecList) {
		ListMultimap<Integer, SPhraseSpec> orderMap = ArrayListMultimap.create();
		sPhraseSpecList.forEach(item -> {
			if (realiser.realise(item.getSubject()).toString().equals(rootElement)) {
				//if subject equals to root element
				orderMap.put(1, item);
			} else if (realiser.realise(item.getSubject()).toString().contains(rootElement)) {
				//if subject has root element
				orderMap.put(2, item);
			} else if (realiser.realise(item.getObject()).toString().contains(rootElement)) {
				//if object contains root element
				orderMap.put(3, item);
			} else {
				orderMap.put(4, item);
			}
		});
		return orderMap;
	}

	/**
	 * set the pronouns and determiners of a sentence
	 *
	 * @param sPhraseSpecList list of SPhraseSpec
	 * @return list of SPhraseSpec
	 * @throws NLGException
	 */
	private List<SPhraseSpec> setPronounsDeterminers(List<SPhraseSpec> sPhraseSpecList) throws NLGException {
		int currentSentenceSize = sPhraseSpecList.size();
		ArrayList<SPhraseSpec> newSentenceList = new ArrayList<>();
		if (currentSentenceSize > 1) {

			int previousSentenceIndex = 0;
			for (int currentIndex = 0; currentIndex < currentSentenceSize; currentIndex++) {
				SPhraseSpec currentSentence = sPhraseSpecList.get(currentIndex);
				SPhraseSpec previousSentence = sPhraseSpecList.get(previousSentenceIndex);

				//get the subject and verb of the current sentence by replacing object with space in a sentence
				String currentPhraseSpec = realiser.realise(currentSentence.getSubject()).toString();
				String previousPhraseSpec = realiser.realise(previousSentence.getSubject()).toString();

				if (currentIndex == 0) {
					if (!currentSentence.isPlural() && rootElementName != null)
						currentSentence.setSubject(rootElementName);
					newSentenceList.add(currentSentence);
					continue;
				} else if (realiser.realise(currentSentence.getSubject()).equals(rootElement)) {
					//if current sentence's subject is root element set the pronoun of the root element
					currentSentence.setSubject(
							wordFormIdentifier.getPronoun(realiser.realise(currentSentence.getSubject()).toString()));
				} else if (currentPhraseSpec.equals(previousPhraseSpec)) {
					//if current sentence subject and previous sentence subject are equal set determiners
					// for current subject
					NPPhraseSpec subject = new NPPhraseSpec(nlgFactory);
					subject.setDeterminer(NLGConstants.DETERMINER_DEFINITE_ARTICLE);
					subject.setComplement(currentSentence.getSubject());
					currentSentence.setSubject(realiser.realise(subject));
				}
				previousSentenceIndex++;
				newSentenceList.add(currentSentence);

			}
		}
		return newSentenceList;

	}

}
