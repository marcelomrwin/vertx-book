package com.redhat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.redhat.verticle.VertxMain;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class VertxMainTest {

	private Vertx vertx;
	// New field storing the port.
	private int port = 8081;

	@Before
	public void setUp(TestContext context) {
		vertx = Vertx.vertx();

		{
			// Pick an available and random

//			ServerSocket socket = new ServerSocket(0);
//			port = socket.getLocalPort();
//			socket.close();
//
//			DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("HTTP_PORT", port));
//			vertx.deployVerticle(MyFirstVerticle.class.getName(), options, context.asyncAssertSuccess());
		}

		// Create deployment options with the chosen port
		DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("HTTP_PORT", port));
		// Deploy the verticle with the deployment options
		vertx.deployVerticle(VertxMain.class.getName(), options, context.asyncAssertSuccess());
	}

	@After
	public void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testMyApplication(TestContext context) {
		final Async async = context.async();

		vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
			response.handler(body -> {
				context.assertTrue(body.toString().contains("Hello"));
				async.complete();
			});
		});
	}
}
