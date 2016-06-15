package com.nlg.bean;

/**
 * Bean class to store details of each sentence
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class DataBean {
	private String subject;
	private String object;
	private String verb;
	//negative format of sentence
	private boolean negation;

	public boolean isPassive() {
		return isPassive;
	}

	public void setPassive(boolean passive) {
		isPassive = passive;
	}

	private boolean isPassive;

	public DataBean() {
		this.negation = false;
	}

	public DataBean(String subject, String object, String verb) {
		this.subject = subject;
		this.object = object;
		this.verb = verb;
		this.negation = false;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public boolean getNegation() {
		return negation;
	}

	public void setNegation(boolean negation) {
		this.negation = negation;
	}

}
