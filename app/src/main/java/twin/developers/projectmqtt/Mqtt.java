package twin.developers.projectmqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqtt{
    // Constantes para la configuración de MQTT
    private static final String TAG = "MQTT";
    private static final String MQTT_SERVER = "ws://broker.emqx.io:8084/mqtt";
    // Otra opción de servidor MQTT comentada
    //private static final String MQTT_SERVER = "mqtt://[user]:[password]@[address]";

    private static final String CLIENT_ID = "giovannib";
    private static final String TOPIC = "EvaluacionNacional/giovannib";
    private static String MESSAGE = "";
    private static final int QOS = 2; // Calidad de servicio

    private MqttAndroidClient mqttClient;

    // Constructor de la clase Mqtt
    public Mqtt(Context context) {
        // Genera un ID de cliente único
        String clientId = CLIENT_ID + System.currentTimeMillis();
        String serverUri = MQTT_SERVER;

        // Crea el cliente MQTT y establece sus callbacks
        mqttClient = new MqttAndroidClient(context.getApplicationContext(), serverUri, clientId, new MemoryPersistence());
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // Manejo de la reconexión y conexión completa
                if (reconnect) {
                    Log.d(TAG, "Reconnected to: " + serverURI);
                } else {
                    Log.d(TAG, "Connected to: " + serverURI);
                }
                // Suscribirse al tema cuando la conexión está completa
                subscribeToTopic();
            }

            @Override
            public void connectionLost(Throwable cause) {
                // Manejo de la pérdida de conexión
                Log.d(TAG, "Connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Manejo de mensajes entrantes
                Log.d(TAG, "Message received: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Confirmación de entrega de mensaje
                Log.d(TAG, "Message delivered");
            }
        });
    }

    // Método para conectarse al broker MQTT
    public void connectToMqttBroker() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        try {
            // Intenta conectar al cliente con el broker MQTT
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Manejo exitoso de la conexión
                    Log.d(TAG, "Connected to MQTT broker");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Manejo de fallos en la conexión
                    Log.e(TAG, "Failed to connect to MQTT broker: " + exception.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Método para publicar mensajes en el tema MQTT
    public void publishMessage(String mensaje) {
        try {
            MqttMessage message = new MqttMessage(mensaje.getBytes());
            message.setQos(QOS);
            mqttClient.publish(TOPIC, message);
        } catch (MqttException e) {
            // Manejo de errores al publicar
            Log.e(TAG, "Error al publicar el mensaje: " + e.getMessage());
        }
    }

    // Método para suscribirse a un tema MQTT
    public void subscribeToTopic() {
        try {
            // Intenta suscribirse al tema
            mqttClient.subscribe(TOPIC, QOS, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Manejo exitoso de la suscripción
                    Log.d(TAG, "Subscribed to topic: " + TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Manejo de fallos en la suscripción
                    Log.e(TAG, "Failed to subscribe to topic: " + TOPIC);
                }
            });
        } catch (MqttException e) {
            // Manejo de errores al suscribirse
            Log.e(TAG, "Error al suscribirse al tema: " + e.getMessage());
        }
    }
}