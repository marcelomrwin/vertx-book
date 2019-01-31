package com.redhat.common;

public final class Constants {
	private Constants() {
	}

	/** API Route */
	public static final String API_GET = "/api/books/:id";
	public static final String API_LIST_ALL = "/api/books";
	public static final String API_CREATE = "/api/books";
	public static final String API_UPDATE = "/api/books/:id";
	public static final String API_DELETE = "/api/books/:id";
	public static final String API_DELETE_ALL = "/api/books";

	/** Persistence key */
	public static final String REDIS_BOOK_KEY = "BOOK_LIST";
}
