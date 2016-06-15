package com.nlg.simplenlg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.nlg.bean.DataBean;
import com.nlg.common.CommonUtil;
import com.nlg.common.NLGConstants;
import com.nlg.common.NLGException;
import com.nlg.wordnet.Stemmer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import simplenlg.features.Feature;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.NLGFactory;
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
	private static Stemmer stemmer = Stemmer.getInstance();
	private String rootElement = NLGConstants.NULL;
	private String rootElementName = NLGConstants.NULL;

	/**
	 * create sentence using modified SPhraseSpec list
	 *
	 * @param dataBeanList list of data beans
	 * @param root         root element of the xml data
	 * @param clauseType   question type
	 * @param rootName     root element name
	 * @return paragraph
	 * @throws NLGException
	 */
	public String finalizeSentence(List<DataBean> dataBeanList, String root, String clauseType, String rootName)
			throws NLGException {
		if (dataBeanList.isEmpty()) {
			String msg = "No data in the xml.";
			log.error(msg);
			throw new NLGException(msg);
		}
		StringBuilder stringBuffer = new StringBuilder();
		if (!clauseType.equals(NLGConstants.HOW)) {
			//map data beans with SPhraseSpecs except in HOW clause type
			List<SPhraseSpec> sPhraseSpecList = setSPhraseList(dataBeanList);

			if (clauseType.equals(NLGConstants.WHAT)) {
				if (Objects.equals(root, NLGConstants.NULL)) {
					String msg = "Invalid xml content.";
					log.error(msg);
					throw new NLGException(msg);
				}
				//if clause type is WHAT set root element and pronouns
				rootElement = root;
				rootElementName = rootName;
				sPhraseSpecList = setPronounsDeterminers(setConjunctions(sPhraseSpecList));
			}

			//generate sentence
			for (SPhraseSpec sPhraseSpec : sPhraseSpecList) {
				stringBuffer.append(realiser.realiseSentence(sPhraseSpec));
			}
		} else {
			//if the clause type is HOW
			stringBuffer.append("Followings are the steps to follow when fixing the error\n");
			for (DataBean dataBean : dataBeanList) {
				stringBuffer.append(dataBean.getSubject().trim()).append("\t Command : \"")
				            .append(dataBean.getObject().trim()).append("\"\n");
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * set the conjunctions of SPhraseSpec list
	 *
	 * @param sPhraseSpecList list of SPhraseSpec
	 * @return list of SPhraseSpec
	 */
	private List<SPhraseSpec> setConjunctions(List<SPhraseSpec> sPhraseSpecList) throws NLGException {

		ListMultimap<String, SPhraseSpec> duplicatesVerbPhraseMap = findDuplicatesVerbPhrase(sPhraseSpecList);
		List<SPhraseSpec> newVerbPhraseList = new ArrayList<>();
		List<SPhraseSpec> newSentenceList = new ArrayList<>();
		String negativeForm = CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "negative");
		duplicatesVerbPhraseMap.asMap().forEach((word, sPhraseSpec) -> {

			if (sPhraseSpec.size() > 1) {
				//if there are more than one SPhraseSpecs with same subject and object
				SPhraseSpec phraseSpec = new SPhraseSpec(nlgFactory);
				CoordinatedPhraseElement coordinate = nlgFactory.createCoordinatedPhrase();

				//create a new phrase joining objects
				sPhraseSpec.forEach(duplicate -> {
					phraseSpec.setSubject(duplicate.getSubject());
					phraseSpec.setVerb(duplicate.getVerb());
					int count = 0;
					if (duplicate.isNegated()) {
						duplicate.setNegated(true);
						count++;
						if (count != 0) {
							coordinate.addCoordinate(
									negativeForm + NLGConstants.SPACE + realiser.realise(duplicate.getObject()));
						}
					} else
						coordinate.addCoordinate(duplicate.getObject());
				});

				phraseSpec.addComplement(coordinate);
				newVerbPhraseList.add(phraseSpec);

			} else {
				newVerbPhraseList.add(duplicatesVerbPhraseMap.get(word).get(0));
			}

		});

		ListMultimap<String, SPhraseSpec> duplicatesNounPhraseMap = findDuplicatesNounPhrase(newVerbPhraseList);

		duplicatesNounPhraseMap.asMap().forEach((word, sPhraseSpec) -> {

			if (sPhraseSpec.size() > 1) {
				//if there are more than one SPhraseSpecs with same verb and object
				SPhraseSpec phraseSpec = new SPhraseSpec(nlgFactory);
				CoordinatedPhraseElement coordinate = nlgFactory.createCoordinatedPhrase();

				//create a new phrase joining subjects
				sPhraseSpec.forEach(duplicate -> {
					coordinate.addCoordinate(duplicate.getSubject());
					phraseSpec.setVerb(duplicate.getVerb());
					phraseSpec.setObject(duplicate.getObject());
				});

				phraseSpec.setSubject(coordinate);
				newSentenceList.add(phraseSpec);

			} else {
				newSentenceList.add(duplicatesNounPhraseMap.get(word).get(0));
			}

		});
		return newSentenceList;

	}

	/**
	 * find if there are duplicate verbs and objects
	 *
	 * @param sPhraseSpecList list of SPhraseSpec
	 * @return list of SPhraseSpec
	 */
	private ListMultimap<String, SPhraseSpec> findDuplicatesVerbPhrase(List<SPhraseSpec> sPhraseSpecList) {

		ListMultimap<String, SPhraseSpec> duplicates = ArrayListMultimap.create();

		for (SPhraseSpec sPhraseSpec : sPhraseSpecList) {

			String wordSet = realiser.realiseSentence(sPhraseSpec)
			                         .replace(realiser.realise(sPhraseSpec.getObject()).toString(), NLGConstants.SPACE);

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
		ListMultimap<Integer, SPhraseSpec> phraseSpecMap = setOrder(sPhraseSpecList);
		List<SPhraseSpec> newList = new ArrayList<>();

		Random random = new Random();
		int randomCount;

		if (sPhraseSpecList.size() == 0) {
			String msg = "Error while retrieving SPhrase list.";
			log.error(msg);
			throw new NLGException(msg);

		} else {
			List<SPhraseSpec> oldList = phraseSpecMap.get(1);
			if (oldList.size() > 0) {
				for (SPhraseSpec sPhraseSpec : oldList) {
					if (!sPhraseSpec.equals(oldList.get(0))) {
						randomCount = random.nextInt(4);
						if (randomCount / 2 == 0) {
							//set the pronoun as the subject
							sPhraseSpec.setSubject(
									stemmer.getPronoun(realiser.realise(sPhraseSpec.getSubject()).toString()));

						} else {
							//if 2 out of 3 phrases's pronouns are set, add definite article to the subject
							NPPhraseSpec subject = new NPPhraseSpec(nlgFactory);
							subject.setDeterminer(NLGConstants.DETERMINER_DEFINITE_ARTICLE);
							subject.setComplement(sPhraseSpec.getSubject());
							sPhraseSpec.setSubject(realiser.realise(subject));
						}
					} else {
						sPhraseSpec.setSubject(rootElementName);
					}
					newList.add(sPhraseSpec);
				}
			}
			oldList = phraseSpecMap.get(2);
			if (oldList.size() > 0) {
				oldList.forEach((sPhraseSpec) -> {
					if (!(newList.size() == 0)) {
						//if the phrase is not the first sentence in the paragraph add definite article to subject
						NPPhraseSpec subject = new NPPhraseSpec(nlgFactory);
						subject.setDeterminer(NLGConstants.DETERMINER_DEFINITE_ARTICLE);
						subject.setComplement(sPhraseSpec.getSubject());
						sPhraseSpec.setSubject(subject);
					}
					newList.add(sPhraseSpec);
				});
			}
			oldList = phraseSpecMap.get(3);
			if (oldList.size() > 0) {
				oldList.forEach((sPhraseSpec) -> {
					if (!(sPhraseSpec.equals(phraseSpecMap.get(3).get(0)) && (newList.size() == 0))) {
						//if the phrase is not the first phrase of the paragraph and root element is defined before
						NPPhraseSpec subject = new NPPhraseSpec(nlgFactory);
						subject.setDeterminer(NLGConstants.DETERMINER_DEFINITE_ARTICLE);
						subject.setComplement(rootElement);
						//add definite article to the root element
						sPhraseSpec.setObject(realiser.realise(sPhraseSpec.getObject()).toString()
						                              .replaceAll(rootElement, realiser.realise(subject).toString()));
					}
					newList.add(sPhraseSpec);
				});
			}
			oldList = phraseSpecMap.get(4);
			if (oldList.size() > 0) {
				rootElement = realiser.realise(oldList.get(0).getSubject()).toString();
				rootElementName = rootElement;
				//do the same recursively for the new root element
				setPronounsDeterminers(oldList).forEach(newList::add);
			}

		}
		return newList;
	}

	/**
	 * map data bean list to SPhrase list
	 *
	 * @param dataBeanList data bean list
	 * @return SPhraseSpec list
	 * @throws NLGException
	 */
	public List<SPhraseSpec> setSPhraseList(List<DataBean> dataBeanList) throws NLGException {

		List<SPhraseSpec> sPhraseSpecList = new ArrayList<>();

		for (DataBean dataBean : dataBeanList) {
			SPhraseSpec p = new SPhraseSpec(nlgFactory);
			String[] subject = null;
			String word = null;

			if (dataBean.getSubject() != null) {

				subject = dataBean.getSubject().split(NLGConstants.SPACE);
				if (!(dataBean.getSubject().toLowerCase().contains(
						CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "ora_error")) &&
				      subject.length == 1))
					//if the subject is not a single ora error
					word = stemmer.stemWord(subject[subject.length - 1]);
			}
			p.setSubject(dataBean.getSubject());
			p.setVerb(dataBean.getVerb());
			p.setObject(dataBean.getObject());

			if (dataBean.getNegation()) {
				p.setNegated(true);
			}

			if (dataBean.isPassive()) {
				p.setFeature(Feature.PASSIVE, true);
			}

			if ((subject != null && word != null)) {
				if (!word.equals(subject[subject.length - 1]))
					//if subject base form is plural, convert sentence to plural
					p.setPlural(true);
			}
			sPhraseSpecList.add(p);
		}
		return sPhraseSpecList;
	}

}
