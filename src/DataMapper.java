import com.nlg.bean.DataBean;
import com.nlg.common.CommonUtil;
import com.nlg.common.NLGConstants;
import com.nlg.common.NLGException;
import com.nlg.wordnet.Stemmer;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Map xml data to the structure
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class DataMapper {
	static Log log = LogFactory.getLog(DataMapper.class);
	private static String rootElement;
	private static String clauseType;
	private static List<DataBean> dataBeanList;
	private static Stemmer stemmer = Stemmer.getInstance();
	private static String rootName;

	public DataMapper(String xmlData) throws NLGException {
		init(xmlData);
	}

	/**
	 * set data structure details
	 *
	 * @param xmlData xml string
	 * @throws NLGException
	 */
	private void init(String xmlData) throws NLGException {
		//initialize class objects
		rootElement = NLGConstants.NULL;
		clauseType = null;
		dataBeanList = new ArrayList<>();
		rootName = NLGConstants.NULL;

		//get the clause type from the xml data
		String closingTag =
				CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "close_question_tag");
		int length = xmlData.lastIndexOf(closingTag);
		clauseType = xmlData.substring(
				CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "open_question_tag").length(),
				length).toUpperCase();

		xmlData = xmlData.substring(length + closingTag.length());

		DOMParser parser = new DOMParser();
		try {
			parser.parse(new InputSource(new java.io.StringReader(xmlData)));
			Document document = parser.getDocument();

			Element element = document.getDocumentElement();
			rootElement = element.getNodeName().toLowerCase();

			NodeList nodes = element.getChildNodes();
			switch (clauseType) {
				case NLGConstants.WHAT:
				case NLGConstants.WHY:
				case NLGConstants.WHERE:
					setDataBeanListOfWhat(nodes);
					break;
				case NLGConstants.HOW:
					setDataBeanListOfHow(nodes);
					break;
				default:
					throw new NLGException("Invalid question type : " + clauseType);
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
	 * return list of data beans
	 *
	 * @return list of data beans
	 * @throws NLGException
	 */
	public List<DataBean> getDataBeanList() throws NLGException {
		if (dataBeanList.isEmpty()) {
			String msg = "Data Bean list is not generated.";
			log.error(msg);
			throw new NLGException(msg);
		}
		return dataBeanList;
	}

	/**
	 * return root element of the data beans
	 *
	 * @return root element
	 * @throws NLGException
	 */
	public String getRootElement() throws NLGException {
		if (rootElement == null) {
			String msg = "Root element is not created.";
			log.error(msg);
			throw new NLGException(msg);
		}
		return rootElement;
	}

	/**
	 * return root name of the data beans
	 *
	 * @return root name
	 */
	public String getRootName() throws NLGException {
		return rootName;
	}

	/**
	 * retrieve clause type of the answer
	 *
	 * @return Clause Type
	 * @throws NLGException
	 */
	public String getClauseType() throws NLGException {
		if (clauseType == null) {
			String msg = "Clause type is not created.";
			log.error(msg);
			throw new NLGException(msg);
		}
		return clauseType;
	}

	/**
	 * Split a given set of words by '_'
	 *
	 * @param phrase set of words
	 * @return string
	 */
	private String splitWords(String phrase) {
		String[] words = phrase.split(NLGConstants.UNDERSCORE);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(phrase);
		if (words.length > 1) {
			stringBuilder.delete(0, phrase.length());
			for (String word : words) {
				stringBuilder.append(word).append(NLGConstants.SPACE);
			}
		}
		return stringBuilder.toString().trim();
	}

	/**
	 * identify adjectives and set the relevant adverbs of a sentence
	 *
	 * @param dataBean data bean
	 * @return modified data bean
	 * @throws NLGException
	 */
	private DataBean setSentenceAdverb(DataBean dataBean) throws NLGException {
		if (!stemmer.stemWord(dataBean.getVerb()).equals(NLGConstants.VERB_BE)) {
			String[] adverbs = dataBean.getObject().split(NLGConstants.SPACE);
			for (String adverb : adverbs) {
				if (stemmer.isAdverb(adverb)) {
					dataBean.setObject(dataBean.getObject().replace(adverb, stemmer.convertToAdverb(adverb)));
				}
			}
		}
		return dataBean;
	}

	/**
	 * set subject, verb and object of a sentence to a data bean
	 *
	 * @param subject subject of the sentence
	 * @param verb    verb of the sentence
	 * @param object  object of the sentence
	 * @return data bean
	 */
	private DataBean setDataBean(String subject, String verb, String object) {
		DataBean dataBean = new DataBean();
		dataBean.setSubject(subject);
		dataBean.setVerb(verb);
		dataBean.setObject(object);
		return dataBean;
	}

	/**
	 * set the data bean list of an answer for question type 'WHAT'
	 *
	 * @param nodes xml node list
	 * @throws NLGException
	 */
	private void setDataBeanListOfWhat(NodeList nodes) throws NLGException {
		for (int i = 0; i < nodes.getLength(); i++) {

			String subject = nodes.item(i).getNodeName().toLowerCase();
			String object = nodes.item(i).getTextContent().toLowerCase();

			DataBean dataBean = new DataBean();
			subject = splitWords(subject);
			object = splitWords(object);

			String firstWordOfSubject = subject.split(NLGConstants.SPACE)[0];

			if (subject.contains(
					CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "error_description"))) {

				String firstWordOfObject = object.split(NLGConstants.SPACE)[0];
				//if the sentence is a description of something
				if (stemmer.isVerb(firstWordOfObject)) {

					//if the description contains a verb first
					dataBean =
							setDataBean(rootElement, firstWordOfObject, object.substring(firstWordOfObject.length()));
				} else {
					//if the description does not contains a verb first set the verb as a BE verb
					dataBean = setDataBean(rootElement, CommonUtil.getInstance()
					                                              .loadProperties(NLGConstants.DATABEAN_FILE_PATH,
					                                                              "description_verb"),
					                       "\'" + object + "\'");

				}

			} else if (subject.contains(
					CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "file_name")) ||
			           subject.contains(CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH,
			                                                                    "error_id"))) {
				//if node is a name or an id of the root element set the object as the root name
				rootName = object;
				continue;
			} else if (stemmer.isVerb(firstWordOfSubject.toLowerCase())) {
				switch (object) {
					case NLGConstants.TRUE:
						//if object is true get the subject without verb and set as object
						object = subject.substring(firstWordOfSubject.length(), subject.length());
						dataBean = setDataBean(rootElement, firstWordOfSubject, object);
						break;
					case NLGConstants.FALSE:
						//if object is false get the subject without verb and set as object
						object = subject.substring(firstWordOfSubject.length(), subject.length());
						dataBean = setDataBean(rootElement, firstWordOfSubject, object);
						//set data bean negation
						dataBean.setNegation(true);
						break;
					default:
						dataBean = setDataBean(rootElement, subject, object);
						break;
				}

			} else if (stemmer.isNoun(firstWordOfSubject.toLowerCase())) {
				//if the node does not contain a verb, set the verb in 'be' form
				dataBean = setDataBean(subject, NLGConstants.VERB_BE, object);
			}

			dataBean = setSentenceAdverb(dataBean);
			dataBeanList.add(dataBean);
		}
	}

	/**
	 * set the data bean list of an answer of question type 'WHY' and 'WHERE'
	 *
	 * @param nodes xml node list
	 */
	private void setDataBeanListOfHow(NodeList nodes) throws NLGException {
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
				DataBean dataBean = new DataBean();
				dataBean.setSubject(descriptions[count]);
				dataBean.setObject(commands[count]);
				dataBean.setVerb(null);
				dataBeanList.add(dataBean);
			}
		}

	}

}
