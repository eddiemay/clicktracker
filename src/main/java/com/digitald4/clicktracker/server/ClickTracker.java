package com.digitald4.clicktracker.server;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.jdbc.DBConnectorThreadPoolImpl;
import com.digitald4.common.storage.DAOConnectorImpl;
import com.digitald4.common.storage.DataConnector;
import com.digitald4.common.storage.DataConnectorCloudDS;
import com.digitald4.common.storage.DataConnectorSQLImpl;
import com.digitald4.common.storage.GenericStore;
import com.digitald4.common.storage.Store;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClickTracker extends HttpServlet {
	private static final String LOCALE = "locale=";

	/**
	 * Set of request parameters used by ClickTracker
	 */
	private static final Set<String> paramSet = new HashSet<>();
	static {
		paramSet.add("url");
	}
	private final Store<Click> clickStore;
	private DataConnector dataConnector;

	public ClickTracker() {
		clickStore = new GenericStore<>(new DAOConnectorImpl<>(Click.class, () -> dataConnector));
	}

	public ClickTracker(Store<Click> clickStore) {
		this.clickStore = clickStore;
	}

	public void init() {
		ServletContext sc = getServletContext();
		if (sc.getServerInfo().contains("Tomcat")) {
			// We use MySQL with Tomcat, so if Tomcat, MySQL
			dataConnector = new DataConnectorSQLImpl(new DBConnectorThreadPoolImpl()
					.connect(sc.getInitParameter("dbdriver"),
							sc.getInitParameter("dburl"),
							sc.getInitParameter("dbuser"),
							sc.getInitParameter("dbpass")));
		} else  { // We use CloudDataStore with AppEngine.
			dataConnector = new DataConnectorCloudDS();
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
						.setUrl(requestUrl)
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
