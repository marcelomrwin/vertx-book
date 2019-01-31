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


## Run

`./target/com.redhat.httpserver.vertxmain`

or

`docker run --rm -it -p 8080:8080 --net=host marcelomrwin/vertx-book:latest`

if you want limit the memory size

`docker run -it --rm -p 8080:8080 --net=host -m 256m marcelomrwin/vertx-book:latest`


## Test with wrk

`wrk -t12 -c400 -d30s http://localhost:8080/assets/index.html`

## Publish to openshift

`mvn fabric8:deploy -Popenshift`

