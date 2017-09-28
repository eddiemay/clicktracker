package com.digitald4.clicktracker.server;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.exception.DD4StorageException;
import com.digitald4.common.storage.DAO;
import com.digitald4.common.storage.GenericStore;
import com.digitald4.common.storage.Store;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.mockito.Mock;

public class ClickTrackerTest {

	@Mock private HttpServletRequest request = mock(HttpServletRequest.class);
	@Mock private HttpServletResponse response = mock(HttpServletResponse.class);
	@Mock private DAO dao = mock(DAO.class);
	@Mock private Clock clock = mock(Clock.class);
	private Store<Click> clickStore = new GenericStore<>(Click.class, () -> dao);
	private ClickTracker clickTracker = new ClickTracker(clickStore, clock);

	@Test
	public void testBasicRequest() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getParameter("url")).thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/");
	}

	@Test
	public void testReConstructsRequest() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		Map<String, String[]> parameterMap = new HashMap<>();
		String url = "https://www.jw.org/finder?docid=1011214";
		parameterMap.put("url", new String[]{url});
		parameterMap.put("item", new String[]{"docid-502017855_1_VIDEO"});
		parameterMap.put("prefer", new String[]{"content"});
		parameterMap.put("srcid", new String[]{"share"});
		when(request.getParameterMap()).thenReturn(parameterMap);
		when(request.getParameter("url")).thenReturn(url);

		clickTracker.doGet(request, response);

		verify(response).sendRedirect(
				"https://www.jw.org/finder?docid=1011214&item=docid-502017855_1_VIDEO&prefer=content&srcid=share");
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

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/?locale=es");
	}

	@Test
	public void testIpadEsAcceptLanguageRequest() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getHeader("Accept-Language")).thenReturn("es-xl,es;q=0.8");
		when(request.getParameter("url")).thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/?locale=es");
	}

	@Test
	public void testLanguageWithCountryFallback() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getHeader("Accept-Language")).thenReturn("es-US,es;q=0.8");
		when(request.getParameter("url")).thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/?locale=es");
	}

	@Test
	public void testWTLocaleRequestOverridesAcceptLanguage() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getHeader("Accept-Language")).thenReturn("es-419,es;q=0.8");
		when(request.getParameter("url"))
				.thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/?locale=es");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/?locale=es");
	}

	@Test
	public void testLocaleRequestOverridesAcceptLanguage() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getHeader("Accept-Language")).thenReturn("es-419,es;q=0.8");
		when(request.getParameter("url"))
				.thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/?locale=en");

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/?locale=en");
	}

	@Test
	public void testContinuesOnDBError() throws Exception {
		when(request.getRemoteAddr()).thenReturn("4.2.2.1");
		when(request.getParameter("url")).thenReturn("https://www.jw.org/en/publications/books/good-news-from-god/");
		when(dao.create(any(Click.class))).thenThrow(new DD4StorageException("Database not available"));

		clickTracker.doGet(request, response);

		verify(response).sendRedirect("https://www.jw.org/en/publications/books/good-news-from-god/");
	}
}
