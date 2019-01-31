package com.redhat.httpserver;

import static com.redhat.common.ActionHelper.badRequest;
import static com.redhat.common.ActionHelper.created;
import static com.redhat.common.ActionHelper.noContent;
import static com.redhat.common.ActionHelper.notFound;
import static com.redhat.common.ActionHelper.ok;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.redhat.pojo.Book;
import com.redhat.repository.VertxBookRepository;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
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
import io.vertx.ext.web.handler.CorsHandler;
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

	protected Logger logger = null;

	private VertxBookRepository repository;

	public static void main(String[] args) {
		Vertx.vertx().deployVerticle(new VertxMain());
	}

	@Override
	public void start(Future<Void> fut) {
		repository = new VertxBookRepository(vertx);
		Router router = Router.router(vertx);

		router.route("/").handler(rc -> {
			HttpServerResponse response = rc.response();
			response.putHeader("content-type", "text/html").end("<pre><h1>Hello from my first Vert.x 3 app</h1></pre>");
		});

		router.route("/eventbus/*").handler(eventBusHandler());

		// Serve static resources from the /assets directory
		router.route("/assets/*").handler(StaticHandler.create("webroot"));
		router.route().handler(StaticHandler.create());

		// enable cors
		enableCorsSupport(router);

		// Rest API
		router.get("/api/books").handler(this::getAll);
		router.get("/api/books/:id").handler(this::getOne);
		router.route("/api/books*").handler(BodyHandler.create());
		router.post("/api/books").handler(this::addOne);
		router.delete("/api/books/:id").handler(this::deleteOne);
		router.delete("/api/books").handler(this::deleteAll);
		router.put("/api/books/:id").handler(this::updateOne);

		router.get("/api/event").handler(this::publish);

		ConfigRetriever retriever = ConfigRetriever.create(vertx);
		ConfigRetriever.getConfigAsFuture(retriever).compose(config -> {
			return repository.initData().compose(x -> {
				return createHttpServer(config, router);
			});
		}).setHandler(fut);
	}

	private Future<Void> createHttpServer(JsonObject config, Router router) {
		Future<Void> future = Future.future();
		Integer port = config.getInteger("HTTP_PORT", 8080);
		vertx.createHttpServer(new HttpServerOptions().setSsl(false)).requestHandler(router::accept).listen(port,
				res -> {
					if (res.succeeded()) {
						logger.info("Server listening at: http://localhost:" + port);
						future.handle(res.mapEmpty());
					} else {
						logger.error("Server start fail");
						future.fail(res.cause());
					}
				}

		);
		return future;
	}

	protected void enableCorsSupport(Router router) {
		Set<String> allowHeaders = new HashSet<>();
		allowHeaders.add("x-requested-with");
		allowHeaders.add("Access-Control-Allow-Origin");
		allowHeaders.add("origin");
		allowHeaders.add("Content-Type");
		allowHeaders.add("accept");
		// CORS support
		router.route()
				.handler(CorsHandler.create("*").allowedHeaders(allowHeaders).allowedMethod(HttpMethod.GET)
						.allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.DELETE).allowedMethod(HttpMethod.PATCH)
						.allowedMethod(HttpMethod.PUT));
	}

	// ---- HTTP Actions ----

	private void getAll(RoutingContext rc) {

		repository.getAll().compose(array -> {
			Future<java.util.List<Book>> future = Future.future();
			if (array.isEmpty())
				noContent(rc);
			else {
				future.complete(array.stream().map(x -> Book.fromString((String) x))
						.sorted((o1, o2) -> o1.getId().compareTo(o2.getId())).collect(Collectors.toList()));
			}
			return future;
		}).setHandler(ok(rc));

	}

	private void getOne(RoutingContext rc) {
		String id = rc.pathParam("id");
		if (id == null || id.isEmpty())
			badRequest(rc);
		else {
			repository.getOne(id).compose(book -> {
				Future<Book> future = Future.future();
				if (book == null)
					notFound(rc);
				else {
					future.complete(book);
				}
				return future;
			}).setHandler(ok(rc));
		}
	}

	private void addOne(RoutingContext rc) {
		Book book = rc.getBodyAsJson().mapTo(Book.class);
					
		repository.addOne(book).compose(bk -> {
			Future<Book> future = Future.future();
			if (book == null || book.getId() < 1)
				badRequest(rc);
			else {
				future.complete(bk);
			}
			return future;
		}).setHandler(created(rc));
	}

	private void deleteOne(RoutingContext rc) {
		String id = rc.pathParam("id");
		repository.deleteOne(id).setHandler(noContent(rc));
	}
	
	private void deleteAll(RoutingContext rc) {
		repository.deleteAll().setHandler(noContent(rc));
	}

	private void updateOne(RoutingContext rc) {
		String id = rc.request().getParam("id");
		Book newBook = rc.getBodyAsJson().mapTo(Book.class);

		if (id == null || newBook == null) {
			badRequest(rc);
		} else {
			repository.getOne(id).compose(res -> {
				Future<Book> future = Future.future();
				if (res == null)
					badRequest(rc);
				else {
					Book book = res.merge(newBook);
					repository.updateOne(book, id).setHandler(noContent(rc));
				}
				return future;
			});
		}

	}

	// websocket
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
