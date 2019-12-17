package com.phoenix.finance.web;

import javax.servlet.http.HttpServletRequest;

public interface Controller {

	String JSP_PATH = "/WEB-INF/view/";

	/**
	 * @return entity used by a controller
	 */
	Object getModel(HttpServletRequest req);

}
