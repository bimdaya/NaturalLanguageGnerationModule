package com.nlg.wordnet;

import com.nlg.common.CommonUtil;
import com.nlg.common.NLGConstants;
import com.nlg.common.NLGException;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.MorphologicalProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Handle stems of words
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class Stemmer {
	static Log log = LogFactory.getLog(Stemmer.class);
	private static volatile Stemmer instance;
	private static Dictionary dictionary;
	private static MorphologicalProcessor morphologicalProcessor;

	static {
		try {

			JWNL.initialize(new FileInputStream(NLGConstants.PROPERTY_FILE_PATH));
			dictionary = Dictionary.getInstance();
			morphologicalProcessor = dictionary.getMorphologicalProcessor();

		} catch (FileNotFoundException e) {
			String msg = "Property file is not found in the path : " + NLGConstants.PROPERTY_FILE_PATH;
			log.error(msg);
			throw new RuntimeException(msg, e);
		} catch (JWNLException e) {
			String msg = "Property file can not be initialized.";
			log.error(msg);
			throw new RuntimeException(msg, e);
		}

	}

	public static Stemmer getInstance() {
		if (instance == null) {
			synchronized (Stemmer.class) {
				if (instance == null)
					instance = new Stemmer();
			}
		}
		return instance;
	}

	/**
	 * Retrieve base form of a given word
	 *
	 * @param word Word to be stemmed
	 * @return Base form of a given word
	 * @throws NLGException
	 */
	public String stemWord(String word) throws NLGException {
		IndexWord indexWord;
		try {
			word = word.toLowerCase();
			indexWord = morphologicalProcessor.lookupBaseForm(POS.VERB, word);
			if (indexWord != null)
				//if WordNet contains base form of the word as a verb
				return indexWord.getLemma();
			indexWord = morphologicalProcessor.lookupBaseForm(POS.NOUN, word);
			if (indexWord != null)
				//if WordNet contains base form of the word as a noun
				return indexWord.getLemma();
			indexWord = morphologicalProcessor.lookupBaseForm(POS.ADJECTIVE, word);
			if (indexWord != null)
				//if WordNet contains base form of the word as an adjective
				return indexWord.getLemma();
			indexWord = morphologicalProcessor.lookupBaseForm(POS.ADVERB, word);
			if (indexWord != null)
				//if WordNet contains base form of the word as an adverb
				return indexWord.getLemma();
		} catch (JWNLException e) {
			String msg = "Error while retrieving base form of the word : " + word;
			log.error(msg);
			throw new NLGException(msg, e);
		}
		return word;
	}

	/**
	 * Check whether a given word is a noun
	 *
	 * @param word Word to be checked
	 * @return true if it is a noun, else false
	 * @throws NLGException
	 */
	public boolean isNoun(String word) throws NLGException {
		try {
			if ((morphologicalProcessor.lookupBaseForm(POS.NOUN, word.toLowerCase())) != null) {
				return true;
			}
		} catch (JWNLException e) {
			String msg = "Error while retrieving base form of the word : " + word;
			log.error(msg);
			throw new NLGException(msg, e);
		}
		return false;
	}

	/**
	 * Check whether a given word is a verb
	 *
	 * @param word Word to be checked
	 * @return true if it is a verb, else false
	 * @throws NLGException
	 */
	public boolean isVerb(String word) throws NLGException {
		try {
			if ((morphologicalProcessor.lookupBaseForm(POS.VERB, word.toLowerCase())) != null)
				return true;
		} catch (JWNLException e) {
			String msg = "Error while retrieving base form of the word : " + word;
			log.error(msg);
			throw new NLGException(msg, e);
		}
		return false;
	}

	/**
	 * Check whether a given word is an adverb
	 *
	 * @param word Word to be checked
	 * @return true if it is a noun, else false
	 * @throws NLGException
	 */
	public boolean isAdverb(String word) throws NLGException {
		try {
			if ((morphologicalProcessor.lookupBaseForm(POS.ADVERB, word.toLowerCase())) != null)
				return true;
		} catch (JWNLException e) {
			String msg = "Error while retrieving base form of the word : " + word;
			log.error(msg);
			throw new NLGException(msg, e);
		}
		return false;
	}

	/**
	 * Retrieve the relevant adverb for a given adjective
	 *
	 * @param adjective Adjective
	 * @return Adverb if there is one, else null
	 * @throws NLGException
	 */
	public String convertToAdverb(String adjective) throws NLGException {
		try {
			adjective = adjective.toLowerCase();
			IndexWord indexWord = morphologicalProcessor.lookupBaseForm(POS.ADVERB, adjective);

			if (indexWord != null) {
				//if the given word is an adjective get all the synonym details from wordnet
				Synset[] synsets = indexWord.getSenses();
				for (Synset synset : synsets) {
					//get the list of synonyms
					Word[] words = synset.getWords();
					String adverb = words[0].getLemma();
					for (Word word : words) {
						adverb = word.getLemma();
						//check if the synonym contains character sequence of the given adjective
						if (adverb.contains(adjective.substring(0, adjective.length() - 2))) {
							//check if the synonym contains 'ly' at the end
							if (adverb.contains(CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH,
							                                                            "is_adverb")))
								return adverb;
						}
					}
					return adverb;
				}
			}
		} catch (JWNLException e) {
			String msg = "Error while retrieving base form of the adjective : " + adjective;
			log.error(msg);
			throw new NLGException(msg, e);
		}
		return null;
	}

	/**
	 * Retrieve relevant pronoun for a given noun
	 *
	 * @param word noun
	 * @return pronoun for the given noun
	 * @throws NLGException
	 */
	public String getPronoun(String word) throws NLGException {
		String objectPronoun =
				CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "common_adverb");
		try {
			IndexWord indexWord = dictionary.lookupIndexWord(POS.NOUN, word);

			if (indexWord != null) {
				//if the given word is a noun get all the synonym details from wordnet
				Synset[] synsets = indexWord.getSenses();

				//load properties file
				Properties properties = new Properties();
				InputStream inputStream =
						getClass().getClassLoader().getResourceAsStream(NLGConstants.PRONOUN_FILE_PATH);

				for (Synset synset : synsets) {
					StringBuilder synsetStringBuilder = new StringBuilder();

					//load root node of the synonym tree
					PointerTargetTree tree = PointerUtils.getInstance().getHypernymTree(synset);
					synsetStringBuilder.append(String.valueOf(tree.getRootNode()));

					//append each child to the string
					tree.getRootNode().getChildTreeList().forEach(synsetStringBuilder::append);

					if (inputStream != null) {
						try {
							properties.load(inputStream);
							//load property key values
							Enumeration enumerationKeys = properties.keys();
							while (enumerationKeys.hasMoreElements()) {
								String key = (String) enumerationKeys.nextElement();
								//load values according to key and split them
								String[] values = properties.getProperty(key).split(NLGConstants.COMMA);
								for (String value : values)
									if (synsetStringBuilder.toString().contains(value))
										//if synsets contain value of a key
										return key;
							}

							return objectPronoun;

						} catch (IOException e) {
							String msg =
									"property file " + NLGConstants.PRONOUN_FILE_PATH + " not found in the classpath";
							log.error(msg);
							throw new NLGException(msg, e);
						}
					} else {
						String msg = "property file " + NLGConstants.PRONOUN_FILE_PATH + " not found in the classpath";
						log.error(msg);
						throw new NLGException(msg);
					}

				}
			}
			return null;

		} catch (JWNLException e) {
			String msg = "Error while retrieving adverb for the word : " + word;
			log.error(msg);
			throw new NLGException(msg);
		}
	}

}
