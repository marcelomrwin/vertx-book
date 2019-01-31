package com.redhat.repository;

import java.util.List;

import com.redhat.common.Constants;
import com.redhat.pojo.Book;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.redis.RedisClient;

public class VertxBookRepository {

	private final RedisClient redis;
	private static final Logger logger = LoggerFactory.getLogger(VertxBookRepository.class);

	public VertxBookRepository(Vertx vertx) {
		redis = RedisClient.create(vertx);
	}

	public Future<Void> initData() {
		Future<Void> future = Future.future();
		Book bookTest = new Book(99, "Fallacies of distributed computing",
				"https://en.wikipedia.org/wiki/Fallacies_of_distributed_computing", "marcelo sales");
		redis.hset(Constants.REDIS_BOOK_KEY, "99", Json.encodePrettily( // test connection
				bookTest), res -> {
					if (res.failed()) {
						logger.error("Redis service is not running!");
						res.cause().printStackTrace();
						future.fail(res.cause());
					} else if (res.succeeded()) {
						logger.info("Redis service is running well!");
						future.complete();
					}
				});

		redis.hgetall("book-list", res -> {
			logger.info(res);
		});
		return future;
	}

	public Future<Book> insert(Book book) {
		Future<Book> future = Future.future();
		final String encoded = Json.encodePrettily(book);
		redis.hset(Constants.REDIS_BOOK_KEY, String.valueOf(book.getId()), encoded, res -> {
			if (res.succeeded())
				future.complete(book);
			else
				future.fail(res.cause());
		});

		return future;
	}

	public Future<JsonArray> getAll() {
		Future<JsonArray> future = Future.future();

		redis.hvals(Constants.REDIS_BOOK_KEY, res -> {
			if (res.succeeded()) {
				future.complete(res.result());
			} else
				future.fail(res.cause());
		});

		return future;
	}

	public Future<Book> getOne(String id) {
		Future<Book> future = Future.future();
		redis.hget(Constants.REDIS_BOOK_KEY, id, res -> {
			if (res.succeeded())
				future.complete(Book.fromString(res.result()));
			else
				future.fail(res.cause());
		});
		return future;
	}

	public Future<Book> addOne(Book pBook) {
		Future<Book> future = Future.future();
		Book book = wrapObject(pBook);
		final String encoded = Json.encodePrettily(book);

		redis.hset(Constants.REDIS_BOOK_KEY, String.valueOf(book.getId()), encoded, res -> {
			if (res.succeeded())
				future.complete(book);
			else
				future.fail(res.cause());
		});
		return future;
	}

	public Future<Void> updateOne(Book pBook, String id) {
		Future<Void> future = Future.future();

		final String encoded = Json.encodePrettily(pBook);

		redis.hset(Constants.REDIS_BOOK_KEY, String.valueOf(pBook.getId()), encoded, res -> {
			if (res.succeeded())
				future.complete();
			else
				future.fail(res.cause());
		});

		return future;
	}

	public Future<Void> deleteOne(String id) {
		Future<Void> future = Future.future();
		redis.hdel(Constants.REDIS_BOOK_KEY, id, res -> {
			if (res.succeeded())
				future.complete();
			else
				future.fail(res.cause());
		});

		return future;
	}

	public Future<Void> deleteAll() {
		Future<Void> future = Future.future();
		redis.del(Constants.REDIS_BOOK_KEY, res -> {
			if (res.succeeded())
				future.complete();
			else
				future.fail(res.cause());
		});
		return future;
	}

	private Book wrapObject(Book book) {		
		Integer id = book.getId();
		if (id > Book.getIncId()) {
			Book.setIncIdWith(id);
		} else if (id == 0)
			book.setIncId();
		
		return book;
	}

}
