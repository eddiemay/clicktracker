package com.digitald4.clicktracker.server;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.exception.DD4StorageException;
import com.digitald4.common.storage.Store;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.mockito.Mock;

public class ClickTrackerTest {

	@Mock private HttpServletRequest request = mock(HttpServletRequest.class);
	@Mock private HttpServletResponse response = mock(HttpServletResponse.class);
	@Mock private Store<Click> clickStore = mock(Store.class);
	private ClickTracker clickTracker = new ClickTracker(null, clickStore);

	@Test
	public void testBasicRequest() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getParameter("url")).thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/");
	}

	@Test
	public void testEnAcceptLanguageRequest() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getHeader("Accept-Language")).thenReturn("en-US,en;q=0.8");
		when(request.getParameter("url")).thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/");
	}

	@Test
	public void testEsAcceptLanguageRequest() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getHeader("Accept-Language")).thenReturn("es-419,es;q=0.8");
		when(request.getParameter("url")).thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/?wtlocale=es-419");
	}

	@Test
	public void testJTLangRequestOverridesAcceptLanguage() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getHeader("Accept-Language")).thenReturn("es-419,es;q=0.8");
		when(request.getParameter("url"))
				.thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/?wtlocale=S");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/?wtlocale=S");
	}

	@Test
	public void testLangRequestOverridesAcceptLanguage() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getHeader("Accept-Language")).thenReturn("es-419,es;q=0.8");
		when(request.getParameter("url"))
				.thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/?lang=ru");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/?wtlocale=ru");
	}

	@Test
	public void testContinuesOnDBError() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getParameter("url")).thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/");
		when(clickStore.create(any(Click.class))).thenThrow(new DD4StorageException("Database not available"));

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/");
	}
}
