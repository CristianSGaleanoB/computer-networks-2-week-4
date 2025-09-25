package com.cristiangaleano;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.json.JSONObject;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

public class ThermostatPublisherInClass
{
    private static final String BROKER = "tcp://localhost:1883";
    private static final String TOPIC = "home/thermostat";
    public static void main( String[] args )
    {
        Mqtt5BlockingClient clientPub = MqttClient.builder()
                .useMqttVersion5()
                .identifier("thermostat-publisher")
                .serverHost("ae7eed9aad04409ca3733870dcfcd866.s1.eu.hivemq.cloud")
                .serverPort(8883)
                .sslWithDefaultConfig()
                .simpleAuth()
                    .username("hivemq.webclient.1758826468479")
                    .password("aEyi%S1DU.5Yl!te73&P".getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth()
                .buildBlocking();

        try{
            clientPub.connect();
            System.out.println("Connected to MQTT broker at" );
        }catch(Exception e){
            System.out.println("Failed to connect to MQTT broker at" );

        }

        while (true) {
            int randomTemperature = RandomTemperature.getRandomTemperature();
            JSONObject payload = new JSONObject();
            payload.put("dispositive", "thermostat_01");
            payload.put("temperature", randomTemperature);
            payload.put("time", java.time.LocalTime.now().toString());
            
            Mqtt5Publish publicMessage = Mqtt5Publish.builder()
                    .topic(TOPIC)
                    .payload(payload.toString().getBytes(StandardCharsets.UTF_8))
                    .build();

            clientPub.publish(publicMessage);
            System.out.println("Publish message " + payload + " to topic: " + TOPIC);


            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        clientPub.disconnect();
        System.out.println("Disconnected from MQTT broker at " + BROKER);

    }

    private static class RandomTemperature{
        public static int getRandomTemperature(){
            Random random = new Random();
            int min = -10;
            int max = 55;
            return random.nextInt(max - min + 1) + min;
        }
    }
}
