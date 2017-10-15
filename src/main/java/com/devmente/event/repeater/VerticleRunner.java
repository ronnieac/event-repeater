package com.devmente.event.repeater;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;

public class VerticleRunner {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        vertx.fileSystem().rxReadFile("config.json").subscribe(buffer -> {
            JsonObject config = new JsonObject(buffer.getDelegate());

            DeploymentOptions options = new DeploymentOptions()
                    .setConfig(config)
                    .setInstances(1);

            vertx.deployVerticle(EventRepeaterVerticle.class.getName(), options);
        });
    }

}
