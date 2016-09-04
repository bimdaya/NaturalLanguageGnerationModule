
import com.nlg.dataHandler.ParagraphBean;
import com.nlg.dataHandler.SentenceDataMapper;
import com.nlg.common.NLGException;
import com.nlg.simplenlg.SPhraseDescriptionGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import simplenlg.realiser.english.Realiser;

/**
 * create the sentence paragraph to the xml
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class SentencePlanner {

	/**
	 * generate sentence paragraph
	 *
	 * @param xml xml data
	 * @return paragraph as string
	 * @throws NLGException
	 */
	public String createAnswer(String xml) throws NLGException {
		SentenceDataMapper sentenceDataMapper = new SentenceDataMapper(xml);
		ParagraphBean paragraphBean = sentenceDataMapper.getNewParagraphBean();
		SPhraseDescriptionGenerator sPhraseDescriptionGenerator = new SPhraseDescriptionGenerator();
		Log log = LogFactory.getLog(SentencePlanner.class);

		switch (paragraphBean.getParagraphClauseType()) {
			case HOW:
				return sPhraseDescriptionGenerator.finalizeSentence(paragraphBean);
			case WHAT:
			case WHY:
			case WHERE:
				return sPhraseDescriptionGenerator.finalizeSentence(paragraphBean);

			default:
				String msg = "Invalid question type :  " + paragraphBean.getParagraphClauseType();
				log.error(msg);
				throw new NLGException(msg);
		}
	}

}
