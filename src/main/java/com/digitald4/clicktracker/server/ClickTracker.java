package com.digitald4.clicktracker.server;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.jdbc.DBConnector;
import com.digitald4.common.jdbc.DBConnectorThreadPoolImpl;
import com.digitald4.common.storage.DAOCloudDataStore;
import com.digitald4.common.storage.DAOProtoSQLImpl;
import com.digitald4.common.storage.GenericStore;
import com.digitald4.common.storage.Store;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "API Service Servlet", urlPatterns = {"/ct/*"})
public class ClickTracker extends HttpServlet {
	private enum DataStore {
		mysql,
		cloudDatastore
	}
	private static final String STANDARD_LANG = "lang=";
	private static final String JW_ORG_LANG = "wtlocale=";
	private static final DataStore selectedDatastore = DataStore.cloudDatastore;
	private final DBConnector connector;
	private final Store<Click> clickStore;

	public ClickTracker() {
		if (selectedDatastore == DataStore.mysql) {
			connector = new DBConnectorThreadPoolImpl();
			clickStore = new GenericStore<>(new DAOProtoSQLImpl<>(Click.class, getConnector()));
		} else {
			connector = null;
			clickStore = new GenericStore<>(new DAOCloudDataStore<>(Click.class));
		}
	}

	public ClickTracker(DBConnector connector, Store<Click> clickStore) {
		this.connector = connector;
		this.clickStore = clickStore;
	}

	public DBConnector getConnector() {
		return connector;
	}

	public void init() {
		if (connector != null) {
			ServletContext sc = getServletContext();
			getConnector().connect(sc.getInitParameter("dbdriver"),
					sc.getInitParameter("dburl"),
					sc.getInitParameter("dbuser"),
					sc.getInitParameter("dbpass"));
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String requestUrl = request.getParameter("url");
		String requestLanguage = getRequestLanguage(requestUrl);
		String acceptLanguage = getAcceptLanguage(request);
		String redirectUrl = getRedirctUrl(requestUrl, requestLanguage, acceptLanguage);
		try {
			clickStore.create(Click.newBuilder()
					.setUrl(requestUrl)
					.setRecorded(System.currentTimeMillis())
					.setIpAddress(request.getRemoteAddr())
					.setAcceptLanguage(acceptLanguage)
					.setRequestLanguage(requestLanguage)
					.setRedirectUrl(redirectUrl)
					.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			response.sendRedirect(redirectUrl);
			// response.getWriter().print("");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static String getRequestLanguage(String url) {
		String requestLang = "";
		if (url.contains(JW_ORG_LANG)) {
			requestLang = url.substring(url.indexOf(JW_ORG_LANG));
		} else if (url.contains(STANDARD_LANG)) {
			requestLang = url.substring(url.indexOf(STANDARD_LANG));
		}
		if (requestLang.contains("&")) {
			requestLang = requestLang.substring(0, requestLang.indexOf("&"));
		}
		return requestLang;
	}

	private static String getAcceptLanguage(HttpServletRequest request) {
		String acceptLanguage = request.getHeader("Accept-Language");
		if (acceptLanguage == null) {
			return "";
		}
		if (acceptLanguage.contains(",")) {
			return acceptLanguage.substring(0, acceptLanguage.indexOf(","));
		}
		return acceptLanguage;
	}

	private static String getRedirctUrl(String url, String requestLanguage, String acceptLanguage) {
		if (url.contains(JW_ORG_LANG)
				|| requestLanguage.isEmpty() && (acceptLanguage.isEmpty() || acceptLanguage.equals("en-US"))) {
			return url;
		}
		if (!requestLanguage.isEmpty()) {
			return url.replace(STANDARD_LANG, JW_ORG_LANG);
		}
		return url + "?" + JW_ORG_LANG + acceptLanguage;
	}
}
