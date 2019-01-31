#!/bin/bash

mvn clean package -Dmaven.compiler.fork=true -Dmaven.compiler.executable=$GRAL_VM_CE/bin/javac -Dvertx.disableDnsResolver=true -DskipTests

ln -s target/classes classes
cp classes/META-INF/native-image/com.redhat/vertx-book/reflection.json .

$GRAL_VM_CE/bin/native-image --enable-all-security-services \
-H:+ReportUnsupportedElementsAtRuntime \
--allow-incomplete-classpath \
--rerun-class-initialization-at-runtime=io.netty.handler.codec.http2.Http2CodecUtil \
--delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,io.netty.handler.codec.http2.DefaultHttp2FrameWriter,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder,io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator,io.netty.handler.ssl.ReferenceCountedOpenSslEngine \
-H:IncludeResources='(META-INF/vertx|META-INF/services|static|webroot|template)/.*' \
-H:ReflectionConfigurationFiles=${PWD}/reflection.json \
-cp target/vertx-book.jar -H:Class=com.redhat.httpserver.VertxMain

mv com.redhat.httpserver.vertxmain vertx-book

unlink classes
rm -rf classes
rm reflection.json