package com.devmente.event.repeater;

import com.devmente.event.repeater.channel.ChannelService;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.eventbus.Message;

class PublicationHandler implements Handler<Message<Object>> {

    private static Logger log = LoggerFactory.getLogger(PublicationHandler.class);

    private final ChannelService channelService;

    PublicationHandler(ChannelService channelService) {
        this.channelService = channelService;
    }


    @Override
    public void handle(Message<Object> event) {
        JsonObject json = JsonObject.mapFrom(event.body());
        String channel = json.getString("channel");
        JsonObject payload = json.getJsonObject("payload");
        channelService.publish(channel, payload).doOnError(error -> {
            log.error("An unexpected error has ocurred", error);
        }).subscribe();
    }

}
