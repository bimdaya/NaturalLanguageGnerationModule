package com.nlg.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Handle property classes
 *
 * @author NLG Module
 * @version 1.0.0
 */
public class CommonUtil {
	static Log log = LogFactory.getLog(CommonUtil.class);
	private static CommonUtil instance;

	public static CommonUtil getInstance() {
		if (instance == null) {
			synchronized (CommonUtil.class) {
				if (instance == null)
					instance = new CommonUtil();
			}
		}
		return instance;
	}

	/**
	 * Retrieve properties relevant to a given key from property file
	 *
	 * @param path property file path
	 * @param key  property key
	 * @return value relevant to property
	 * @throws NLGException
	 */
	public String loadProperties(String path, String key) throws NLGException {
		//load properties file
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
		Properties properties = new Properties();
		try {

			properties.load(inputStream);

		} catch (IOException e) {
			String msg = "property file " + path + " not found in the classpath";
			log.error(msg);
			throw new NLGException(msg, e);
		}

		String output = properties.getProperty(key);

		if (output == null) {
			String msg = "property file " + path + " does not contain a key value called " + key;
			log.error(msg);
			throw new NLGException(msg);
		}

		return output;
	}
}
