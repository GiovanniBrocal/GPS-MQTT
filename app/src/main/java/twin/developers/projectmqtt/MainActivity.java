package twin.developers.projectmqtt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private EditText etLatitude;
    private EditText etLongitude;
    private Mqtt mqttManager;
    private DatabaseReference firebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // MQTT Setup
        mqttManager = new Mqtt(getApplicationContext());
        mqttManager.connectToMqttBroker();


        // Firebase Setup
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("ubicaciones");

        // UI Setup
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        Button btnGetLocation = findViewById(R.id.btnGetLocation);
        btnGetLocation.setOnClickListener(this::getLocation);
    }

    public void getLocation(View view) {
        if (checkLocationPermission()) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    double latitude = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();

                    etLatitude.setText("Latitud: " + latitude);
                    etLongitude.setText("Longitud: " + longitude);

                    String locationMessage = "Latitud: " + latitude + ", Longitud: " + longitude;
                    enviarMensajeFirebase(locationMessage);

                    Toast.makeText(this, "Ubicación guardada y enviada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "El GPS no está habilitado", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation(findViewById(R.id.btnGetLocation));
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enviarMensajeFirebase(String mensaje) {
        firebaseDatabaseReference.push().setValue(mensaje);
    }
}
