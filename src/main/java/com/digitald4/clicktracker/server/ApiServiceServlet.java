package com.digitald4.clicktracker.server;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.server.service.JSONServiceImpl;
import com.digitald4.common.server.service.SingleProtoService;
import com.digitald4.common.storage.GenericStore;

public class ApiServiceServlet extends com.digitald4.common.server.ApiServiceServlet {
	public ApiServiceServlet() {
		addService("click",
				new JSONServiceImpl<>(new SingleProtoService<>(new GenericStore<>(Click.class, daoProvider)),  false));
	}
}
