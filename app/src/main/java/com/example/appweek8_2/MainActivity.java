package com.example.appweek8_2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MQTT";
    private static final String MQTT_BROKER = "tcp://broker.hivemq.com:1883";
    private static final String CLIENT_ID = "AndroidClient_" + System.currentTimeMillis();
    private static final String TOPIC_SUBSCRIBE = "dei/test";
    private static final String TOPIC_PUBLISH = "dei/test";

    private MqttClient mqttClient;
    private TextView tvMessages;
    private EditText etMessage;
    private Button btnPublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnPublish = findViewById(R.id.btnPublish);

        setupMQTTClient();

        btnPublish.setOnClickListener(view -> publishMessage());
    }

    private void setupMQTTClient() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID, null);

            // Configura opciones de conexión
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Conexión perdida: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.i(TAG, "Mensaje recibido: " + new String(message.getPayload()));
                    runOnUiThread(() -> tvMessages.append("\n" + topic + ": " + new String(message.getPayload())));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i(TAG, "Mensaje entregado");
                }
            });

            mqttClient.connect(options);
            mqttClient.subscribe(TOPIC_SUBSCRIBE);
            Log.i(TAG, "Conectado al broker y suscrito al tópico: " + TOPIC_SUBSCRIBE);

        } catch (MqttException e) {
            Log.e(TAG, "Error de conexión: " + e.getMessage());
        }
    }

    private void publishMessage() {
        try {
            String message = etMessage.getText().toString();
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttClient.publish(TOPIC_PUBLISH, mqttMessage);
            Log.i(TAG, "Mensaje publicado: " + message);
        } catch (MqttException e) {
            Log.e(TAG, "Error al publicar: " + e.getMessage());
        }
    }
}
