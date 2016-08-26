package com.nlg.dataHandler;

import com.nlg.common.ClauseEnum;
import simplenlg.phrasespec.SPhraseSpec;

import java.util.List;

/**
 * Bean class to store details of paragraph
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class ParagraphBean {

	private String rootElement;
	private List<SPhraseSpec> sentenceList;
	private ClauseEnum paragraphClauseType;
	private String RootElementName;

	public ParagraphBean() {
	}

	public String getRootElement() {
		return rootElement;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public List<SPhraseSpec> getSentenceList() {
		return sentenceList;
	}

	public void setSentenceList(List<SPhraseSpec> sentenceList) {
		this.sentenceList = sentenceList;
	}

	public ClauseEnum getParagraphClauseType() {
		return paragraphClauseType;
	}

	public void setParagraphClauseType(ClauseEnum paragraphClauseType) {
		this.paragraphClauseType = paragraphClauseType;
	}

	public String getRootElementName() {
		return RootElementName;
	}

	public void setRootElementName(String rootElementName) {
		RootElementName = rootElementName;
	}
}
