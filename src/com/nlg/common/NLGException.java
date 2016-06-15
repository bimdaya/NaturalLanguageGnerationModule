package com.nlg.common;

/**
 * handle exceptions of the NLG module
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class NLGException extends Exception {

	public NLGException() {
	}

	public NLGException(String s) {
		super(s);
	}

	public NLGException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public NLGException(Throwable throwable) {
		super(throwable);
	}
}
