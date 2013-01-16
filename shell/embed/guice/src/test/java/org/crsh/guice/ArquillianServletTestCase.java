package org.crsh.guice;

import java.io.IOException;
import java.net.URL;

import org.apache.http.client.fluent.Request;
import org.hamcrest.core.IsNull;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ArquillianServletTestCase {

	@Test
	@RunAsClient
	public void testSimpleGet(@ArquillianResource URL baseURL) throws IOException {
		String content = Request.Get(baseURL.toExternalForm() + "?howHigh=5").execute().returnContent().asString();
		Assert.assertThat(content, IsNull.notNullValue());
	}

	@Test
	@RunAsClient
	public void testRetrieveTelnetPort(@ArquillianResource URL baseURL) throws IOException {
		String telnetPort = Request.Options(baseURL.toExternalForm()).execute().returnContent().asString();
		Assert.assertThat(telnetPort, IsNull.notNullValue());
	}

	
	@Deployment
	public static WebArchive createDeployment() {
		return GuiceTestWebAppArchive.buildInstance();
	}
}
