# vertx-book

This is your empty project. Ensure you have [GraalVM](https://www.graalvm.org) installed
on your path or use the provided `Dockerfile` to build your image.

## Build
```
export JAVA_HOME=$GRAL_VM_CE
mvn clean package -Dmaven.compiler.fork=true -Dmaven.compiler.executable=$GRAL_VM_CE/bin/javac -Dvertx.disableDnsResolver=true
```

or

`docker build -t marcelomrwin/vertx-book:latest .` 

if you already has a native-image use 

`docker build -t marcelomrwin/vertx-book:latest -f Dockerfile-binary .`

if you has compiled jar

```
docker build -t marcelomrwin/vertx-book:latest -f Dockerfile-jar .
```

## Build Native Image

```
mvn clean package -Pnative -Dmaven.compiler.fork=true -Dmaven.compiler.executable=$GRAL_VM_CE/bin/javac -DskiptTests -Dvertx.disableDnsResolver=true
```
build with native graalvm

```
./build-native-jar.sh
```

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

`./target/com.redhat.httpserver.vertxmain`

or

`docker run --rm -it -p 8080:8080 --net=host marcelomrwin/vertx-book:latest`

if you want limit the memory size

`docker run -it --rm -p 8080:8080 --net=host -m 256m marcelomrwin/vertx-book:latest`


## Test with wrk

```
wrk -t12 -c400 -d30s http://localhost:8080/index.html
wrk -t12 -c400 -d30s http://localhost:8080/api/books
```

## Publish to openshift

`mvn fabric8:deploy -Popenshift`

