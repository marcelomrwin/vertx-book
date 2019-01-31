package com.redhat.pojo;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Book {

	private static final AtomicInteger acc = new AtomicInteger(0);

	private Integer id = 0;

	private String title;

	private String url;

	private byte[] content;

	private String author;

	private String isbn;

	public Book() {
	}

	public Book(String title, String url, String author) {
		this();
		this.title = title;
		this.url = url;
		this.author = author;
	}

	public Book(int id, String title, String url, String author) {
		this(title, url, author);
		this.id = id;
	}

	public Book(JsonObject json) {
		this(json.getInteger("id", -1), json.getString("title"), json.getString("url"), json.getString("author"));
	}

	public void setIncId() {
		this.id = acc.incrementAndGet();
	}

	public static int getIncId() {
		return acc.get();
	}

	public static void setIncIdWith(int n) {
		acc.set(n);
	}

	public static Book fromString(String str) {
		return Json.decodeValue(str, Book.class);
	}

	public String getTitle() {
		return title;
	}

	public Book setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Book setUrl(String url) {
		this.url = url;
		return this;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private <T> T getOrElse(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}

	public Book merge(Book book) {
		return new Book(id, getOrElse(book.title, title), getOrElse(book.url, url), getOrElse(book.author, author));
	}

	public JsonObject toJson() {
		return JsonObject.mapFrom(this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Book [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (title != null) {
			builder.append("title=");
			builder.append(title);
			builder.append(", ");
		}
		if (url != null) {
			builder.append("url=");
			builder.append(url);
			builder.append(", ");
		}
		if (content != null) {
			builder.append("content=");
			builder.append(Arrays.toString(content));
			builder.append(", ");
		}
		if (author != null) {
			builder.append("author=");
			builder.append(author);
			builder.append(", ");
		}
		if (isbn != null) {
			builder.append("isbn=");
			builder.append(isbn);
		}
		builder.append("]");
		return builder.toString();
	}
}
