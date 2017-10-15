package com.devmente.event.repeater.channel.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.rabbitmq.RabbitMQClient;
import rx.Single;

class SimpleRabbitMQClient {

    private final static String EXCHANGE_DECLARATION_MSG = "Successful declaration of exchange( %s )";
    private final static String QUEUE_DECLARATION_MSG = "Successful generation of queue( %s )";
    private final static String QUEUE_BINDED_TO_EXCHANGE_MSG = "Successful bind from queue( %s ) to exchange( %s )";
    private final static String QUEUE_BINDED_TO_ADDRESS_MSG = "Successful bind from queue( %s ) to address( %s )";
    private final static String MESSAGE_PUBLISHED_MSG = "Message( %s ) published to %s";
    private final static String QUEUE_DELETION_MSG = "Successful deletion of queue( %s )";

    private final static Logger log = LoggerFactory.getLogger(SimpleRabbitMQClient.class);

    private final RabbitMQClient client;

    SimpleRabbitMQClient(RabbitMQClient rabbitMQClient) {
        this.client = rabbitMQClient;
    }

    Single<Void> start() {
        return client.rxStart();
    }

    Single<Void> declareExchange(String exchange) {
        return client.rxExchangeDeclare(exchange, "fanout", true, true).map(result -> {
            log.debug(String.format(EXCHANGE_DECLARATION_MSG, exchange));
            return null;
        });
    }

    Single<String> generateQueue() {
        return client.rxQueueDeclareAuto().map(result -> {
            String queue = result.getString("queue");
            log.debug(QUEUE_DECLARATION_MSG, queue);
            return queue;
        });
    }

    Single<Void> bindQueueToExchange(String queue, String exchange) {
        return client.rxQueueBind(queue, exchange, "").map(result -> {
            log.debug(String.format(QUEUE_BINDED_TO_EXCHANGE_MSG, queue, exchange));
            return null;
        });
    }

    Single<Void> bindQueueToEventBus(String queue, String address) {
        return client.rxBasicConsume(queue, address).map(result -> {
            log.debug(String.format(QUEUE_BINDED_TO_ADDRESS_MSG, queue, address));
            return null;
        });
    }

    Single<Void> publishToExchange(String exchange, JsonObject message) {
        return client.rxBasicPublish(exchange, "", message).map(result -> {
            log.debug(String.format(MESSAGE_PUBLISHED_MSG, message, exchange));
            return null;
        });
    }

    Single<Void> deleteQueue(String queue) {
        return client.rxQueueDelete(queue).map(result -> {
            log.debug(String.format(QUEUE_DELETION_MSG, queue));
            return null;
        });
    }
}
