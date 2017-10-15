package com.devmente.event.repeater.channel.rabbitmq;

import com.devmente.event.repeater.channel.rabbitmq.SubscriptionRegistry.QueueRegistry;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.shareddata.LocalMap;
import io.vertx.rxjava.rabbitmq.RabbitMQClient;

public class ChannelServiceFactory {

    private ChannelServiceFactory() {}

    public static ChannelServiceImpl create(Vertx vertx, JsonObject config) {

        EventBus eventBus = vertx.eventBus();

        JsonObject rabbitMQConfig = config.getJsonObject("rabbitMQ");
        RabbitMQClient rabbitMQClient = RabbitMQClient.create(vertx, rabbitMQConfig);
        SimpleRabbitMQClient client = new SimpleRabbitMQClient(rabbitMQClient);

        LocalMap<String, QueueRegistry> repository = vertx.sharedData().getLocalMap("subscriptionRepository");
        SubscriptionRegistry registry = new SubscriptionRegistry(repository);

        return new ChannelServiceImpl(eventBus, client, registry);
    }

}
