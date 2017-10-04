package com.digitald4.clicktracker.server;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.server.SingleProtoService;
import com.digitald4.common.storage.GenericStore;
import javax.servlet.ServletException;

public class ApiServiceServlet extends com.digitald4.common.server.ApiServiceServlet {
	public ApiServiceServlet() throws ServletException {
		addService("click", new SingleProtoService<>(new GenericStore<>(Click.class, dataAccessObjectProvider)));
	}
}
