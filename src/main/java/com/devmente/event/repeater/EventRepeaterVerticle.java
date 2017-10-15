package com.devmente.event.repeater;

import com.devmente.event.repeater.channel.ChannelService;
import com.devmente.event.repeater.channel.rabbitmq.ChannelServiceFactory;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;

public class EventRepeaterVerticle extends AbstractVerticle {

    private static Logger log = LoggerFactory.getLogger(EventRepeaterVerticle.class);

    @Override
    public void start() throws Exception {

        Router router = Router.router(vertx);
        ChannelService channelService = ChannelServiceFactory.create(vertx, config());

        configureEventBus(channelService, router);
        router.route().handler(StaticHandler.create());

        channelService.start().subscribe();

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("port", 8080));

        log.info("Start sequence completed.");
    }

    private void configureEventBus(ChannelService channelService, Router router) {
        BridgeOptions bridgeOptions = new BridgeOptions();

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx)
                .bridge(bridgeOptions, new SubscriptionHandler(channelService, bridgeOptions));

        bridgeOptions.addInboundPermitted(new PermittedOptions().setAddress("publication-channel"));
        vertx.eventBus().consumer("publication-channel").handler(new PublicationHandler(channelService));

        router.route("/eventbus/*").handler(sockJSHandler);

        log.info("Event bus configured.");
    }

}
