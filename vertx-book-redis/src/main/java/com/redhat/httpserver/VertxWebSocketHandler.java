package com.redhat.httpserver;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;

public class VertxWebSocketHandler implements Handler<BridgeEvent> {

	 public VertxWebSocketHandler(EventBus eventBus) {
		super();
		this.eventBus = eventBus;
	}

	private final EventBus eventBus;
	
	@Override
	public void handle(BridgeEvent event) {
		if (event.type() == BridgeEventType.SOCKET_CREATED)
            System.out.println("A socket was created");

        if (event.type() == BridgeEventType.SEND)
            clientToServer();

        event.complete(true);
		
	}

	private void clientToServer() {
        String value = "message";        
        eventBus.publish("out", value);                
    }
	
}
