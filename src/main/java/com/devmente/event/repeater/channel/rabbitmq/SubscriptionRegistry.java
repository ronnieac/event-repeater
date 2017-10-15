package com.devmente.event.repeater.channel.rabbitmq;

import java.util.Optional;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.Shareable;
import io.vertx.rxjava.core.shareddata.LocalMap;

class SubscriptionRegistry {

    private final static String NEW_QUEUE_CLIENT_MSG = "New queue client created( %s -> %s )";
    private final static String NEW_SUBSCRIPTION_MSG = "New subscription to exchange: %s";
    private final static String LOSE_SUBSCRIPTION_MSG = "Lose subscription to exchange: %s";
    private final static String READY_FOR_DELETE_MSG = "Queue client( %s -> %s ) ready for delete";

    private final static Logger log = LoggerFactory.getLogger(SubscriptionRegistry.class);

    private final LocalMap<String, QueueRegistry> repository;

    SubscriptionRegistry(LocalMap<String, QueueRegistry> repository) {
        this.repository = repository;
    }

    Optional<String> getQueue(String exchange) {
        return Optional.ofNullable(
                repository.get(exchange))
                .map(r -> r.queue);
    }

    void register(String exchange, String queue) {
        repository.put(exchange, new QueueRegistry(queue));
        log.debug(String.format(NEW_QUEUE_CLIENT_MSG, queue, exchange));
        log.debug(String.format(NEW_SUBSCRIPTION_MSG, exchange));
    }

    void take(String exchange) {
        repository.get(exchange).quantityOfClients++;
        log.debug(String.format(NEW_SUBSCRIPTION_MSG, exchange));
    }

    boolean dispose(String exchange) {
        QueueRegistry registry = repository.get(exchange);
        registry.quantityOfClients--;

        if (registry.quantityOfClients < 1) {
            log.debug(String.format(READY_FOR_DELETE_MSG, registry.queue, exchange));
            return true;
        } else {
            log.debug(String.format(LOSE_SUBSCRIPTION_MSG, exchange));
            return false;
        }
    }

    public static class QueueRegistry implements Shareable {

        private final String queue;
        private int quantityOfClients;

        QueueRegistry(String queue) {
            this.queue = queue;
            this.quantityOfClients = 1;
        }
    }

}
