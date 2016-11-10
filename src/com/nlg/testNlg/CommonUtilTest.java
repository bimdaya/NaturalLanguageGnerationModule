package com.nlg.testNlg;

import com.nlg.common.CommonUtil;
import com.nlg.common.NLGConstants;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by bimali on 11/8/16.
 */
public class CommonUtilTest {


	@Test
	public void testLoadProperties() throws Exception {
		String tag = CommonUtil.getInstance().loadProperties(NLGConstants.DATABEAN_FILE_PATH, "close_question_tag");
		assertEquals(tag,"</question_type>");
	}
}