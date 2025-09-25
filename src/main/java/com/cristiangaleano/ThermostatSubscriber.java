package com.cristiangaleano;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient.Mqtt5Publishes;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

public class ThermostatSubscriber {
    private static final int IDEAL_TEMPERATURE = 22;
    private static final String SUB_TOPIC = "home/thermostat"; 
    private static final String PUB_TOPIC = "home/thermostat/command";

    public static void main(String[] args) {
        Mqtt5BlockingClient client = MqttClient.builder()
                .useMqttVersion5()
                .identifier("thermostat-subscriber")
                .serverHost("localhost")
                .serverPort(1883)
                .buildBlocking();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.disconnect();
            } catch (Exception ignored) {}
        }));

        try (Mqtt5Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL)) {
iuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuñppppppppppppppppl
            boolean connected = false;
            int attempts = 0;
            while (!connected && attempts < 5) {
                try {
                    client.connect();
                    connected = true;
                    System.out.println("Connected to MQTT broker.");
                } catch (final Exception e) {
                    attempts++;
                    System.err.println("Failed to connect (attempt " + attempts + "): " + e.getMessage());
                    try {
                        TimeUnit.SECONDS.sleep(2L * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            if (!connected) {
                System.err.println("Could not connect to MQTT broker after retries. Exiting.");
                return;
            }

            try {
                client.subscribeWith()
                        .topicFilter(SUB_TOPIC)
                        .send();
                System.out.println("Subscribed to topic: " + SUB_TOPIC);
            } catch (Exception e) {
                System.err.println("Failed to subscribe: " + e.getMessage());
                return;
            }

            while (true) {
                try {
                    Mqtt5Publish publish = publishes.receive();
                    String topic = publish.getTopic().toString();

                    byte[] payloadBytes = publish.getPayloadAsBytes();
                    String payload = payloadBytes != null ? new String(payloadBytes, StandardCharsets.UTF_8) : "";

                    System.out.println("Received message: " + payload + " from topic: " + topic);
        

                    JSONObject payloadJson;
                    try {
                        payloadJson = new JSONObject(payload);
                    } catch (Exception jsonEx) {
                        System.err.println("Invalid JSON received, skipping. Error: " + jsonEx.getMessage());
                        continue;
                    }

                    if (!payloadJson.has("temperature")) {
                        System.err.println("Missing 'temperature' key in JSON, skipping.");
                        continue;
                    }

                    int temperature;
                    try {
                        temperature = payloadJson.getInt("temperature");
                    } catch (Exception typeEx) {
                        System.err.println("'temperature' is not an int: " + typeEx.getMessage());
                        continue;
                    }

                    JSONObject payloadResponse = new JSONObject();
                    payloadResponse.put("currentTemp", temperature);
                    payloadResponse.put("idealTemp", IDEAL_TEMPERATURE);

                    if (temperature > IDEAL_TEMPERATURE) {
                        payloadResponse.put("state", "cooling");
                    } else if (temperature < IDEAL_TEMPERATURE) {
                        payloadResponse.put("state", "heating");
                    } else {
                        payloadResponse.put("state", "idle");
                    }

                    try {
                        client.publishWith()
                                .topic(PUB_TOPIC)
                                .payload(payloadResponse.toString().getBytes(StandardCharsets.UTF_8))
                                .send();

                        System.out.println("Published command: " + payloadResponse + " to topic: " + PUB_TOPIC);
                    } catch (Exception pubEx) {
                        System.err.println("Failed to publish command: " + pubEx.getMessage());
                    }

                } catch (Exception loopEx) {
                    System.err.println("Error in receive loop: " + loopEx.getMessage());
                }
            }
        }
    }
}
