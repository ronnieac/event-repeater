package com.devmente.event.repeater;

import com.devmente.event.repeater.channel.ChannelService;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.ext.web.handler.sockjs.BridgeEvent;

class SubscriptionHandler implements Handler<BridgeEvent> {

    private static Logger log = LoggerFactory.getLogger(SubscriptionHandler.class);

    private final ChannelService channelService;
    private final BridgeOptions bridgeOptions;

    SubscriptionHandler(ChannelService channelService, BridgeOptions bridgeOptions) {
        this.channelService = channelService;
        this.bridgeOptions = bridgeOptions;
    }

    @Override
    public void handle(BridgeEvent event) {
        if (event.type() == BridgeEventType.REGISTER) {
            JsonObject message = event.getRawMessage();
            String channel = message.getString("address");
            channelService.subscribe(channel)
                    .doOnSuccess(success -> {
                        bridgeOptions.addOutboundPermitted(new PermittedOptions().setAddress(channel));
                        event.complete(true);
                    })
                    .doOnError(error -> {
                        log.error("An unexpected error has ocurred", error);
                        event.fail(error);
                    }).subscribe();
        } else {
            event.complete(true);
        }
    }

}
