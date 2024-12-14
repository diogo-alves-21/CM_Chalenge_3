package com.example.appweek8_2.Helpers;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {
    private static final String TAG = "MqttHelper";
    private static final String MQTT_BROKER = "tcp://broker.emqx.io:1883";
    private static final String CLIENT_ID = "AndroidClient_" + System.currentTimeMillis();

    private MqttAsyncClient mqttClient;
    private Context context;
    private MqttConnectionListener mqttConnectionListener; // Listener para a conexão
    private MessageCallback messageCallback; // Callback para mensagens

    public MqttHelper(Context context) {
        this.context = context;
        connectToBroker();
    }

    private void connectToBroker() {
        try {
            mqttClient = new MqttAsyncClient(MQTT_BROKER, CLIENT_ID, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Connection lost: " + cause.getMessage());
                    if (mqttConnectionListener != null) {
                        mqttConnectionListener.onConnectionFailed(cause);
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d(TAG, "Message received on topic " + topic + ": " + new String(message.getPayload()));
                    if (messageCallback != null) {
                        messageCallback.onMessageReceived(topic, message);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Message delivery complete");
                }
            });

            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connected to MQTT broker");
                    if (mqttConnectionListener != null) {
                        mqttConnectionListener.onConnected();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to connect to broker: " + exception.getMessage());
                    if (mqttConnectionListener != null) {
                        mqttConnectionListener.onConnectionFailed(exception);
                    }
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Error connecting to MQTT broker: " + e.getMessage());
        }
    }

    public void subscribeToTopic(String topic) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(topic, 1, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Subscribed to topic: " + topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Failed to subscribe to topic: " + topic + ", error: " + exception.getMessage());
                    }
                });
            } else {
                Log.e(TAG, "Cannot subscribe, MQTT client is not connected.");
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error subscribing to topic: " + topic + ", error: " + e.getMessage());
        }
    }

    public void publishMessage(String topic, String message) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage();
                mqttMessage.setPayload(message.getBytes());
                mqttMessage.setQos(1);

                mqttClient.publish(topic, mqttMessage, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Message published to topic: " + topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Failed to publish message to topic: " + topic + ", error: " + exception.getMessage());
                    }
                });
            } else {
                Log.e(TAG, "Cannot publish, MQTT client is not connected.");
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error publishing message to topic: " + topic + ", error: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect(null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Disconnected from MQTT broker");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Failed to disconnect from broker: " + exception.getMessage());
                    }
                });
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error disconnecting from MQTT broker: " + e.getMessage());
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
        void onMessageReceived(String topic, MqttMessage message);
    }

    public interface MqttConnectionListener {
        void onConnected();
        void onConnectionFailed(Throwable exception);
    }
}