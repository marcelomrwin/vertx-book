package com.redhat.httpserver;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class VertxMain extends AbstractVerticle {

	public VertxMain() {
		super();
		File logbackFile = new File("logback.xml");
		System.setProperty("logback.configurationFile", logbackFile.getAbsolutePath());
		System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
		logger = LoggerFactory.getLogger(VertxMain.class);

		logger.info("Logger configured...");
	}

	private Map<Integer, Book> readingList = new LinkedHashMap<>();
	protected Logger logger = null;

	public static void main(String[] args) {
		Vertx.vertx().deployVerticle(new VertxMain());
	}

	@Override
	public void start(Future<Void> fut) {

		createSomeData();
		Router router = Router.router(vertx);

		router.route("/").handler(rc -> {
			HttpServerResponse response = rc.response();
			response.putHeader("content-type", "text/html").end("<pre><h1>Hello from my first Vert.x 3 app</h1></pre>");
		});

		router.route("/eventbus/*").handler(eventBusHandler());

		// Serve static resources from the /assets directory
		router.route("/assets/*").handler(StaticHandler.create("webroot"));
		router.route().handler(StaticHandler.create());		

		// Rest API
		router.get("/api/books").handler(this::getAll);
		router.get("/api/books/:id").handler(this::getOne);
		router.route("/api/books*").handler(BodyHandler.create());
		router.post("/api/books").handler(this::addOne);
		router.delete("/api/books/:id").handler(this::deleteOne);
		router.put("/api/books/:id").handler(this::updateOne);

		router.get("/api/event").handler(this::publish);

		ConfigRetriever retriever = ConfigRetriever.create(vertx);
		retriever.getConfig(config -> {
			if (config.failed()) {
				fut.fail(config.cause());
			} else {
				Integer port = config().getInteger("HTTP_PORT", 8080);
				// Create the HTTP server and pass the "accept" method to the request handler.
				vertx.createHttpServer(new HttpServerOptions().setSsl(false)).requestHandler(router::accept).listen(
						// Retrieve the port from the config, default to 8080.
						port, result -> {
							if (result.succeeded()) {
								logger.info("Server listening at: http://localhost:" + port);
								fut.complete();
							} else {
								logger.error("Server start fail");
								fut.fail(result.cause());
							}
						});
			}
		});
	}

	private void createSomeData() {		
		Book book1 = new Book("Fallacies of distributed computing",
				"https://en.wikipedia.org/wiki/Fallacies_of_distributed_computing", "marcelo sales");
		readingList.put(book1.getId(), book1);
		Book book2 = new Book("Reactive Manifesto", "https://www.reactivemanifesto.org/", "marcelo sales");
		readingList.put(book2.getId(), book2);
	}

	private void getAll(RoutingContext rc) {		
		rc.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(readingList.values()));
	}

	private void addOne(RoutingContext rc) {
		Book book = rc.getBodyAsJson().mapTo(Book.class);
		readingList.put(book.getId(), book);
		rc.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(book));
	}

	private void deleteOne(RoutingContext rc) {
		String id = rc.request().getParam("id");
		try {
			Integer idAsInteger = Integer.valueOf(id);
			readingList.remove(idAsInteger);
			rc.response().setStatusCode(204).end();
		} catch (NumberFormatException e) {
			rc.response().setStatusCode(400).end();
		}
	}

	private void getOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		try {
			Integer idAsInteger = Integer.valueOf(id);
			Book book = readingList.get(idAsInteger);
			if (book == null) {
				// Not found
				routingContext.response().setStatusCode(404).end();
			} else {
				routingContext.response().setStatusCode(200)
						.putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(book));
			}
		} catch (NumberFormatException e) {
			routingContext.response().setStatusCode(400).end();
		}
	}

	private void updateOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		try {
			Integer idAsInteger = Integer.valueOf(id);
			Book book = readingList.get(idAsInteger);
			if (book == null) {
				// Not found
				routingContext.response().setStatusCode(404).end();
			} else {
				JsonObject body = routingContext.getBodyAsJson();
				book.setTitle(body.getString("title")).setUrl(body.getString("url"))
						.setAuthor(body.getString("author"));
				;
				readingList.put(idAsInteger, book);
				routingContext.response().setStatusCode(200)
						.putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(book));
			}
		} catch (NumberFormatException e) {
			routingContext.response().setStatusCode(400).end();
		}

	}

	private SockJSHandler eventBusHandler() {
		BridgeOptions options = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddressRegex("out"))
				.addInboundPermitted(new PermittedOptions().setAddressRegex("in"));

		SharedData data = vertx.sharedData();
		EventBus eventBus = vertx.eventBus();

		SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
		return sockJSHandler.bridge(options, new VertxWebSocketHandler(eventBus));
	}

	private void publish(RoutingContext rc) {

		rc.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily("ok"));
		Book book = new Book("titulo", "url", "author");
		JsonObject bookJson = new JsonObject(Json.encode(book));
		vertx.eventBus().publish("out", bookJson);
	}

}
