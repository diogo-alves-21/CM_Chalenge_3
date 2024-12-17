package com.example.appweek8_2.Helpers;

import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {
    private static final String TAG = "MqttHelper";
    private static final String MQTT_BROKER = "tcp://broker.emqx.io:1883";
    private static final String CLIENT_ID = "AndroidClient_" + System.currentTimeMillis();

    private MqttClient mqttClient;
    private MqttConnectionListener mqttConnectionListener; // Listener para a conexão
    private MessageCallback messageCallback; // Callback para mensagens

    public MqttHelper(String topic) {
        connectToBroker(topic);
    }

    private void connectToBroker(String topic) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID, null);

                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(false);
                    //options.setAutomaticReconnect(true);

                    mqttClient.connect(options);
                    mqttClient.subscribe(topic);

                    mqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            Log.d(TAG, "Connection lost: " + cause.getMessage());
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) {
                            Log.d(TAG, "Message received on topic " + topic + ": " + new String(message.getPayload()));
                            messageCallback.onMessageReceived(message);
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            Log.d(TAG, "Message delivery complete");
                        }
                    });

                } catch (MqttException e) {
                    Log.d(TAG, "Error connecting to MQTT broker: " + e.getMessage());
                }
            }
        }).start();
    }

    public void publishMessage(String topic, String message) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage();
                mqttMessage.setPayload(message.getBytes());
                mqttMessage.setQos(1);

                mqttClient.publish(topic, mqttMessage);
            } else {
                Log.d(TAG, "Cannot publish, MQTT client is not connected.");
            }
        } catch (MqttException e) {
            Log.d(TAG, "Error publishing message to topic: " + topic + ", error: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            Log.d("mqtt staff", "Error disconnecting from MQTT broker: " + e.getMessage());
        }
    }

    public void setMqttConnectionListener(MqttConnectionListener listener) {
        this.mqttConnectionListener = listener;
    }

    public void setMessageCallback(MessageCallback callback) {
        this.messageCallback = callback;
    }

    // Callback interface for message reception
    public interface MessageCallback {
        void onMessageReceived(MqttMessage message);
    }

    public interface MqttConnectionListener {
        void onConnected();
        void onConnectionFailed(Throwable exception);
    }
}