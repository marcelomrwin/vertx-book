package com.redhat.data;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.redhat.pojo.Book;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

public class VertxBookRepository {

	private final Vertx vertx;

	public VertxBookRepository(Vertx vertx) {
		this.vertx = vertx;
	}

	public Future<SQLConnection> createTableIfNeeded(SQLConnection connection) {
		Future<SQLConnection> future = Future.future();
		vertx.fileSystem().readFile("tables.sql", ar -> {
			if (ar.failed()) {
				future.fail(ar.cause());
			} else {
				connection.execute(ar.result().toString(), ar2 -> future.handle(ar2.map(connection)));
			}
		});
		return future;
	}

	public Future<SQLConnection> createSomeDataIfNone(SQLConnection connection) {
		Future<SQLConnection> future = Future.future();
		connection.query("SELECT * FROM Books", select -> {
			if (select.failed()) {
				future.fail(select.cause());
			} else {
				if (select.result().getResults().isEmpty()) {
					Book book1 = new Book("Fallacies of distributed computing",
							"https://en.wikipedia.org/wiki/Fallacies_of_distributed_computing", "marcelo sales");
					Book book2 = new Book("Reactive Manifesto", "https://www.reactivemanifesto.org/", "marcelo sales");
					Future<Book> insertion1 = insert(connection, book1, false);
					Future<Book> insertion2 = insert(connection, book2, false);
					CompositeFuture.all(insertion1, insertion2).setHandler(r -> future.handle(r.map(connection)));
				} else {
					future.complete(connection);
				}
			}
		});
		return future;
	}

	public Future<List<Book>> query(SQLConnection connection) {
		Future<List<Book>> future = Future.future();
		connection.query("SELECT * FROM books order by id", result -> {
			connection.close();
			future.handle(result.map(rs -> rs.getRows().stream().map(Book::new).collect(Collectors.toList())));
		});
		return future;
	}

	public Future<Book> queryOne(SQLConnection connection, String id) {
		Future<Book> future = Future.future();
		String sql = "SELECT * FROM books WHERE id = ?";
		connection.queryWithParams(sql, new JsonArray().add(Integer.valueOf(id)), result -> {
			connection.close();
			future.handle(result.map(rs -> {
				List<JsonObject> rows = rs.getRows();
				if (rows.size() == 0) {
					throw new NoSuchElementException("No book with id " + id);
				} else {
					JsonObject row = rows.get(0);
					return new Book(row);
				}
			}));
		});
		return future;
	}

	public Future<Book> insert(SQLConnection connection, Book book, boolean closeConnection) {
		Future<Book> future = Future.future();
		String sql = "INSERT INTO Books (title, url,author) VALUES (?, ?, ?)";
		connection.updateWithParams(sql, new JsonArray().add(book.getTitle()).add(book.getUrl()).add(book.getAuthor()),
				ar -> {
					if (closeConnection) {
						connection.close();
					}
					future.handle(ar.map(res -> new Book(res.getKeys().getLong(0), book.getTitle(), book.getUrl(),
							book.getAuthor())));
				});
		return future;
	}

	public Future<Void> delete(SQLConnection connection, String id) {
		Future<Void> future = Future.future();
		String sql = "DELETE FROM Books WHERE id = ?";
		connection.updateWithParams(sql, new JsonArray().add(Integer.valueOf(id)), ar -> {
			connection.close();
			if (ar.failed()) {
				future.fail(ar.cause());
			} else {
				if (ar.result().getUpdated() == 0) {
					future.fail(new NoSuchElementException("Unknown book " + id));
				} else {
					future.complete();
				}
			}
		});
		return future;
	}

	public Future<Void> update(SQLConnection connection, String id, Book book) {
		Future<Void> future = Future.future();
		String sql = "UPDATE Books SET title = ?, url = ?, author = ? WHERE id = ?";
		connection.updateWithParams(sql,
				new JsonArray().add(book.getTitle()).add(book.getUrl()).add(book.getAuthor()).add(Integer.valueOf(id)),
				ar -> {
					connection.close();
					if (ar.failed()) {
						future.fail(ar.cause());
					} else {
						UpdateResult ur = ar.result();
						if (ur.getUpdated() == 0) {
							future.fail(new NoSuchElementException("No book with id " + id));
						} else {
							future.complete();
						}
					}
				});
		return future;
	}
}
