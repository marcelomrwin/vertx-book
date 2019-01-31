package com.redhat.pojo;

import io.vertx.core.json.JsonObject;

public class Book {

	private long id;

	private String title;

	private String url;

	private byte[] content;

	private String author;

	private String isbn;

	public Book() {
	}

	public Book(String title, String url, String author) {
		super();
		this.title = title;
		this.url = url;
		this.author = author;
	}

	public Book(long id, String title, String url, String author) {
		this();
		this.id = id;
		this.title = title;
		this.url = url;
		this.author = author;
	}

	public Book(JsonObject json) {
		this(json.getInteger("id", -1), json.getString("title"), json.getString("url"), json.getString("author"));
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
