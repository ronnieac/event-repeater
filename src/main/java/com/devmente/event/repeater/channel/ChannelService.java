package com.devmente.event.repeater.channel;

import io.vertx.core.json.JsonObject;
import rx.Single;

public interface ChannelService {

    Single<Void> start();

    Single<Void> subscribe(String channel);

    Single<Void> unsubscribe(String channel);

    Single<Void> publish(String channel, JsonObject message);

}
