package com.devmente.event.repeater.channel.rabbitmq;

import com.devmente.event.repeater.channel.ChannelService;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.EventBus;
import rx.Single;

class ChannelServiceImpl implements ChannelService {

    private final EventBus eventBus;
    private final SimpleRabbitMQClient client;
    private final SubscriptionRegistry registry;

    ChannelServiceImpl(
            EventBus eventBus,
            SimpleRabbitMQClient client,
            SubscriptionRegistry registry) {

        this.eventBus = eventBus;
        this.client = client;
        this.registry = registry;
    }

    @Override
    public Single<Void> start() {
        return client.start();
    }

    @Override
    public Single<Void> subscribe(String channel) {
        return registry.getQueue(channel)
                .map(q -> {
                    registry.take(channel);
                    return Single.<Void>just(null);
                }).orElse(configureExchange(channel));
    }

    private Single<Void> configureExchange(String exchange) {
        return client
                .declareExchange(exchange)
                .flatMap(result -> bindExchangeToEventBus(exchange));
    }

    private Single<Void> bindExchangeToEventBus(String exchange) {
        return generateExchangeClient(exchange)
                .flatMap(queue -> exposeExchangeClient(exchange, queue));
    }

    private Single<String> generateExchangeClient(String exchange) {
        return client
                .generateQueue()
                .flatMap(queue -> {
                    return client
                            .bindQueueToExchange(queue, exchange)
                            .map(result -> queue);
                });
    }

    private Single<Void> exposeExchangeClient(String exchange, String queue) {
        return client.bindQueueToEventBus(queue, queue)
                .map(result -> {
                    eventBus.consumer(queue, payload -> {
                        JsonObject wrapper = JsonObject.mapFrom(payload.body());
                        JsonObject message = new JsonObject(wrapper.getString("body"));

                        eventBus.publish(exchange, message);
                    });

                    registry.register(exchange, queue);
                    return null;
                });
    }

    @Override
    public Single<Void> unsubscribe(String channel) {
        return registry.getQueue(channel)
                .filter(q -> registry.dispose(channel))
                .map(client::deleteQueue)
                .orElse(Single.just(null));
    }

    @Override
    public Single<Void> publish(String channel, JsonObject message) {
        JsonObject wrapper = new JsonObject();
        wrapper.put("body", message.toString());

        return client.publishToExchange(channel, wrapper);
    }
}
