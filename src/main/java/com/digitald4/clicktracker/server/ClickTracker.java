package com.digitald4.clicktracker.server;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.jdbc.DBConnector;
import com.digitald4.common.jdbc.DBConnectorThreadPoolImpl;
import com.digitald4.common.storage.DAOCloudDataStore;
import com.digitald4.common.storage.DAOProtoSQLImpl;
import com.digitald4.common.storage.GenericStore;
import com.digitald4.common.storage.Store;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClickTracker extends HttpServlet {
	private enum DataStore {
		mysql,
		cloudDatastore
	}
	private static final String LOCALE = "locale=";
	private static final DataStore selectedDatastore = DataStore.cloudDatastore;

	/**
	 * Set of request parameters used by ClickTracker
	 */
	private static final Set<String> paramSet = new HashSet<>();
	static {
		paramSet.add("url");
	}
	private final DBConnector connector;
	private final Store<Click> clickStore;

	public ClickTracker() {
		if (selectedDatastore == DataStore.mysql) {
			connector = new DBConnectorThreadPoolImpl();
			clickStore = new GenericStore<>(new DAOProtoSQLImpl<>(Click.class, getConnector()));
		} else {
			connector = null;
			Store<Click> temp = null;
			try {
				temp = new GenericStore<>(new DAOCloudDataStore<>(Click.class));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				clickStore = temp;
			}
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
		StringBuffer requestBuffer = new StringBuffer(request.getParameter("url"));
		// Add back to the url any parameters that were turned into request parameters.
		request.getParameterMap().forEach((key, values) -> {
			if (!paramSet.contains(key)) { // Filter out parameters for this servlet.
				requestBuffer.append("&").append(key).append("=").append(values[0]);
			}
		});

		String requestUrl = requestBuffer.toString();
		String acceptLanguage = getAcceptLanguage(request);
		String redirectUrl = getRedirctUrl(requestUrl, acceptLanguage);
		if (clickStore != null) {
			try {
				clickStore.create(Click.newBuilder()
						.setUrl(requestUrl.toString())
						.setRecorded(System.currentTimeMillis())
						.setIpAddress(request.getRemoteAddr())
						.setAcceptLanguage(acceptLanguage)
						.setRedirectUrl(redirectUrl)
						.build());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			response.sendRedirect(redirectUrl);
			// response.getWriter().print("");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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

	private static String getRedirctUrl(String url, String acceptLanguage) {
		if (url.contains(LOCALE) || acceptLanguage.isEmpty() || acceptLanguage.equals("en-US")) {
			return url;
		}
		if (acceptLanguage.contains("-")) {
			acceptLanguage = acceptLanguage.substring(0, acceptLanguage.indexOf("-"));
		}
		return url + "?" + LOCALE + acceptLanguage;
	}
}
