package com.redhat.httpserver;

import java.util.concurrent.atomic.AtomicInteger;

public class Book {
	private static final AtomicInteger COUNTER = new AtomicInteger();

	private final int id;

	private String title;

	private String url;
	
	private byte[] content;
	
	private String author;
			
	public Book(String title, String url, String author) {
		this();
		this.title = title;
		this.url = url;
		this.author = author;
	}

	public Book() {
		this.id = COUNTER.getAndIncrement();
	}

	public int getId() {
		return id;
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
}
