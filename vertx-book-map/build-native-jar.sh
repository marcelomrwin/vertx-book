#!/bin/bash

ln -s target/classes classes

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
