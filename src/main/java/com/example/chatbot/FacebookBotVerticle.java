package com.example.chatbot;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class FacebookBotVerticle extends AbstractVerticle {

    private String VERIFY_TOKEN;
    private String ACCESS_TOKEN;

    private FaceDetector mFaceDetector;

    private FacebookBotVerticle() {
        try {
            mFaceDetector = new FaceDetector();
        } catch (IOException e) {
            e.printStackTrace();
            mFaceDetector = null;
        }
    }

    @Override
    public void start() {

        updateProperties();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/webhook").handler(this::verify);
        router.post("/webhook").blockingHandler(this::message, false);

        vertx.createHttpServer().requestHandler(router::accept)
                .listen(
                        Integer.getInteger("http.port", 8080), System.getProperty("http.address", "0.0.0.0"));
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new FacebookBotVerticle());
    }


    private void verify(RoutingContext routingContext) {
        String challenge = routingContext.request().getParam("hub.challenge");
        String token = routingContext.request().getParam("hub.verify_token");
        if (!VERIFY_TOKEN.equals(token)) {
            challenge = "fake";
        }

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(challenge);
    }

    private void message(RoutingContext routingContext) {

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end("done");

        final JsonObject hook = routingContext.getBodyAsJson();

        JsonArray entries = hook.getJsonArray("entry");

        entries.getList().forEach(entryObj -> {

            System.out.println(entryObj.getClass());

            Map entry = (Map) entryObj;
            ArrayList messagingList = (ArrayList) entry.get("messaging");
            System.out.println(messagingList.getClass());
            messagingList.forEach((Object m) -> {

                Map messaging = (Map) m ;
                Map sender = (Map) messaging.get("sender");
                messaging.put("recipient", sender);
                messaging.remove("sender");

                Map message = (Map) messaging.get("message");

                final ArrayList attachments = (ArrayList) message.get("attachments");
                if (attachments != null) {
                    // The request contains an attachment (most likely an image)
                    final Map attachment = (Map) attachments.get(0);
                    final Map payload = (Map) attachment.get("payload");
                    final String imageUrl = ((String) payload.get("url"))
                            .replaceAll("(\\r|\\n|\\r\\n)+", "");

                    // Detect if the image contains a face
                    try {
                        final URL url = new URL(imageUrl);
                        final File file = File.createTempFile("attachment-",
                                FilenameUtils.getName(url.getPath()));
                        FileUtils.copyURLToFile(url, file);
                        if (mFaceDetector != null) {
                            final int faces = mFaceDetector.numFacesInImage(file.getAbsolutePath());
                            final String messageText;
                            switch (faces) {
                                case 0:
                                    messageText = "I don't see a face there";
                                    break;
                                case 1:
                                    messageText = "Hey, that's a face!";
                                    break;
                                default:
                                    messageText = "I see a total of " + faces + " faces!";
                                    break;
                            }
                            message.put("text", messageText);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                message.remove("attachments");
                message.remove("mid");
                message.remove("seq");
                message.remove("sticker_id");
                messaging.put("message", message);

                System.out.println(JsonObject.mapFrom(messaging));

                WebClientOptions options = new WebClientOptions();
                options.setSsl(true).setLogActivity(true);
                WebClient client = WebClient.create(vertx, options);


                client
                        .post(443, "graph.facebook.com", "/v2.6/me/messages/")
                        .addQueryParam("access_token", ACCESS_TOKEN)
                        .sendJsonObject(JsonObject.mapFrom(messaging), ar -> {
                            if (ar.succeeded()) {
                                // Obtain response
                                HttpResponse<Buffer> res = ar.result();

                                System.out.println("Received response with status code" + res.bodyAsString());
                            } else {
                                System.out.println("Something went wrong " + ar.cause().getMessage());
                            }
                        });
            });
        });
    }

    private void updateProperties() {
        
            VERIFY_TOKEN = System.getProperty("facebook.verify.token", "verify-token-default");
            ACCESS_TOKEN = System.getProperty("facebook.access.token", "access-token-default");
        
    }
}

