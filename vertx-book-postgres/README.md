# vertx-book

This is your empty project. Ensure you have [GraalVM](https://www.graalvm.org) installed
on your path or use the provided `Dockerfile` to build your image.

## Start Postgres

create ephemeral

```
docker run --rm --name vertx-book-pg -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=vertx-book-db postgres:9.6-alpine
```

create with persistent storage

```
docker volume create pgdata
docker run --rm --name vertx-book-pg -p 5432:5432 -v pgdata:/var/lib/postgresql/data -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=vertx-book-db postgres:9.6-alpine
```

## Run

```
mvn clean compile vertx:run
```

## Test with wrk

```
wrk -t12 -c400 -d30s http://localhost:8080/index.html
wrk -t12 -c400 -d30s http://localhost:8080/api/books
```

## Publish to openshift

`mvn fabric8:deploy -Popenshift`

