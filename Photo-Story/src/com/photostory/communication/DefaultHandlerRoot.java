package com.photostory.communication;

import org.xml.sax.helpers.DefaultHandler;

public abstract class DefaultHandlerRoot extends DefaultHandler{
	public abstract Object getModel();
}
