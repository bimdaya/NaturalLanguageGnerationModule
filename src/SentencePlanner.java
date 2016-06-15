import com.nlg.bean.DataBean;
import com.nlg.common.NLGConstants;
import com.nlg.common.NLGException;
import com.nlg.simplenlg.SPhraseDescriptionGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

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
		DataMapper dataMapper = new DataMapper(xml);
		List<DataBean> dataBeanList = dataMapper.getDataBeanList();
		String clauseType = dataMapper.getClauseType();
		SPhraseDescriptionGenerator sPhraseDescriptionGenerator = new SPhraseDescriptionGenerator();
		Log log = LogFactory.getLog(SentencePlanner.class);

		switch (clauseType) {
			case NLGConstants.WHAT:
			case NLGConstants.WHY:
			case NLGConstants.WHERE:
				return sPhraseDescriptionGenerator
						.finalizeSentence(dataBeanList, dataMapper.getRootElement(), NLGConstants.WHAT,
						                  dataMapper.getRootName());
			case NLGConstants.HOW:
				return sPhraseDescriptionGenerator.finalizeSentence(dataBeanList, "", NLGConstants.HOW, "");
			default:
				String msg = "Invalid question type :  " + clauseType;
				log.error(msg);
				throw new NLGException(msg);
		}
	}

}
