package com.photostory.communication;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import x.br.com.dina.ui.custom.activity.util.Debug;

public class AlternativeRequestReview implements AlternativeBackProcessCallback {

	protected ActionRequest action;
	protected Object model;
	protected DefaultHandlerRoot xmlParser;
	protected int functinNumber;
	protected boolean parseAfterRequest;
	protected String result;

	public AlternativeRequestReview() {
		parseAfterRequest = false;
	}

	public void setFunctionNumber(final int number) {
		Debug.debug(getClass(), "set function number = " + number);
		this.functinNumber = number;
	}

	public void setXMLParser(DefaultHandlerRoot parser) {
		this.xmlParser = parser;
		parseAfterRequest = true;
	}

	public void setParseAfterRequest(boolean value) {
		this.parseAfterRequest = value;
	}

	public void setAction(ActionRequest action) {
		this.action = action;
	}

	public Object getModel() {
		return model;
	}

	public void onFinish() {
		action.onFinishRequest(model, functinNumber, result);
	}

	public void process(AlternativeRequest... api) {
		AlternativeRequest ggl = api[0];

		try {
			result = ggl.execute();

			if (parseAfterRequest) {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				InputStream in = new ByteArrayInputStream(
						result.getBytes("UTF-8"));
				parser.parse(in, xmlParser);
				model = xmlParser.getModel();
			}
		} catch (Exception ex) {
			Debug.debug(getClass(), "error", ex);
		}
	}
}
