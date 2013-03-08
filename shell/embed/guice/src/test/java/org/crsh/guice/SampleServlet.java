package org.crsh.guice;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SampleServlet extends HttpServlet {

	@Inject SampleService service;
	@Inject @Named(value="telnet.port") Integer telnetPort;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String howHigh = req.getParameter("howHigh");
		if (howHigh != null) {
			service.jump(howHigh);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("text/plain");
			resp.getWriter().format("Jumped up to %s m", howHigh);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
		}
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		resp.getWriter().print(telnetPort);
		resp.flushBuffer();
	}
	
}
