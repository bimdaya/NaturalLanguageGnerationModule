package com.nlg.dataHandler;

import com.nlg.common.ClauseEnum;
import com.nlg.common.CommonUtil;
import com.nlg.common.NLGConstants;
import com.nlg.common.NLGException;
import com.nlg.wordnet.WordFormIdentifier;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Map xml data to the structure
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class SentenceDataMapper {
	static Log log = LogFactory.getLog(SentenceDataMapper.class);
	private static ParagraphBean newParagraph;
	private static WordFormIdentifier wordFormIdentifier = WordFormIdentifier.getInstance();
	private static Lexicon lexicon = Lexicon.getDefaultLexicon();
	private static NLGFactory nlgFactory = new NLGFactory(lexicon);

	public SentenceDataMapper(String xmlData) throws NLGException {
		init(xmlData);
	}

	/**
	 * set data structure details
	 *
	 * @param xmlData xml string
	 * @throws NLGException
	 */
	private void init(String xmlData) throws NLGException {
		//get the clause type from the xml data
		String closingTag =
				CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "close_question_tag");
		int length = xmlData.lastIndexOf(closingTag);
		String paragraphClauseType = xmlData.substring(
				CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "open_question_tag").length(),
				length).toUpperCase();

		xmlData = xmlData.substring(length + closingTag.length());

		DOMParser parser = new DOMParser();
		try {
			parser.parse(new InputSource(new java.io.StringReader(xmlData)));
			Document document = parser.getDocument();

			Element element = document.getDocumentElement();
			String rootElement = element.getNodeName().toLowerCase();

			newParagraph = new ParagraphBean();
			newParagraph.setRootElement(rootElement);

			NodeList nodes = element.getChildNodes();
			switch (paragraphClauseType) {
				case NLGConstants.WHAT:
					newParagraph.setParagraphClauseType(ClauseEnum.WHAT);
				case NLGConstants.WHY:
					newParagraph.setParagraphClauseType(ClauseEnum.WHY);
				case NLGConstants.WHERE:
					newParagraph.setParagraphClauseType(ClauseEnum.WHERE);
					setDataBeanListOfWhat(nodes);
					break;
				case NLGConstants.HOW:
					newParagraph.setParagraphClauseType(ClauseEnum.HOW);
					setDataBeanListOfHow(nodes);
					break;
				default:
					throw new NLGException("Invalid question type : " + paragraphClauseType);
			}

		} catch (SAXException e) {
			String msg = "Error while parsing xml elements, xml : " + xmlData;
			log.error(msg);
			throw new NLGException(msg, e);
		} catch (IOException e) {
			String msg = "Failed to retrieve xml data from xml : " + xmlData;
			log.error(msg);
			throw new NLGException(msg, e);
		}

	}

	/**
	 * return paragraph information
	 *
	 * @return ParagraphDataBean object
	 * @throws NLGException
	 */
	public ParagraphBean getNewParagraphBean() throws NLGException {
		if (newParagraph == null) {
			String msg = "Paragraph is not initialized.";
			log.error(msg);
			throw new NLGException(msg);
		}
		return newParagraph;
	}

	/**
	 * set the data dataHandler list of an answer for question type 'WHAT'
	 *
	 * @param nodes xml node list
	 * @throws NLGException
	 */
	private void setDataBeanListOfWhat(NodeList nodes) throws NLGException {
		List<SPhraseSpec> sentenceList = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {

			SPhraseSpec sPhraseSpec = new SPhraseSpec(nlgFactory);

			String subject = nodes.item(i).getNodeName().toLowerCase();
			String object = nodes.item(i).getTextContent().toLowerCase();

			subject = subject.replace(NLGConstants.UNDERSCORE, NLGConstants.SPACE);
			object = object.replace(NLGConstants.UNDERSCORE, NLGConstants.SPACE);

			String firstWordOfSubject = subject.split(NLGConstants.SPACE)[0];
			String firstWordOfObject = object.split(NLGConstants.SPACE)[0];

			if (subject.contains(
					CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "error_description"))) {

				//if the sentence is a description of something
				if (wordFormIdentifier.isVerb(firstWordOfObject)) {

					//if the description contains a verb first
					sPhraseSpec.setSubject(newParagraph.getRootElement());
					sPhraseSpec.setVerb(firstWordOfObject);
					sPhraseSpec.setObject(object.substring(firstWordOfObject.length()));

				} else {
					//if the description does not contains a verb first set the verb as a BE verb
					sPhraseSpec.setSubject(newParagraph.getRootElement());
					sPhraseSpec.setObject("\'" + object + "\'");
					sPhraseSpec.setVerb(CommonUtil.getInstance()
					                              .loadProperties(NLGConstants.DATABEAN_FILE_PATH, "description_verb"));
				}

			} else if (subject.contains(
					CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "file_name")) || subject.contains(
					           CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "error_id"))) {
				//if node is a name or an id of the root element set the object as the root name
				newParagraph.setRootElementName(object);
				continue;
			} else if (wordFormIdentifier.isVerb(firstWordOfSubject.toLowerCase())) {
				if (object.toLowerCase().equals(NLGConstants.TRUE)) {
					//if object is true get the subject without verb and set as object
					sPhraseSpec.setSubject(newParagraph.getRootElement());
					sPhraseSpec.setObject(subject.substring(firstWordOfSubject.length(), subject.length()));
					sPhraseSpec.setVerb(firstWordOfSubject);
				} else if (object.toLowerCase().equals(NLGConstants.FALSE)) {
					//if object is false get the subject without verb and set as object
					sPhraseSpec.setSubject(newParagraph.getRootElement());
					sPhraseSpec.setObject(subject.substring(firstWordOfSubject.length(), subject.length()));
					sPhraseSpec.setVerb(firstWordOfSubject);
					sPhraseSpec.setNegated(true);
				} else if (wordFormIdentifier.isAdverb(firstWordOfObject)) {
					sPhraseSpec.setSubject(newParagraph.getRootElement());
					sPhraseSpec.setVerb(subject);
					sPhraseSpec.setObject(
							object.replace(firstWordOfObject, wordFormIdentifier.convertToAdverb(firstWordOfObject)));
				} else {
					sPhraseSpec.setSubject(newParagraph.getRootElement());
					sPhraseSpec.setObject(object);
					sPhraseSpec.setVerb(subject);
				}
			} else if (wordFormIdentifier.isNoun(firstWordOfSubject.toLowerCase())) {
				//if the node does not contain a verb, set the verb in 'be' form
				sPhraseSpec.setSubject(subject);
				sPhraseSpec.setVerb(NLGConstants.VERB_BE);
				sPhraseSpec.setObject(object);
			}
			sentenceList.add(sPhraseSpec);
		}
		newParagraph.setSentenceList(sentenceList);
	}

	/**
	 * set the data dataHandler list of an answer of question type 'WHY' and 'WHERE'
	 *
	 * @param nodes xml node list
	 */
	private void setDataBeanListOfHow(NodeList nodes) throws NLGException {
		List<SPhraseSpec> sentenceList = new ArrayList<>();
		String[] descriptions = null;
		String[] commands = null;

		for (int i = 0; i < nodes.getLength(); i++) {
			String node = nodes.item(i).getNodeName();
			String value = nodes.item(i).getTextContent();
			String separator = NLGConstants.COMMA;

			if (node.equals(
					CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "command_description"))) {
				descriptions = value.split(separator);

			} else if (node
					.equals(CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "command"))) {
				commands = value.split(separator);
			}
		}

		//map each command relevant to description
		if (descriptions != null && commands != null) {
			for (int count = 0; count < descriptions.length; count++) {
				SPhraseSpec sentence = new SPhraseSpec(nlgFactory);
				sentence.setSubject(descriptions[count]);
				sentence.setObject(commands[count]);
				sentence.setVerb(null);
				sentenceList.add(sentence);
			}
			newParagraph.setSentenceList(sentenceList);
		}

	}

}
