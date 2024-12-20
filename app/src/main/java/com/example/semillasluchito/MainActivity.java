package com.example.semillasluchito;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String THINGSPEAK_URL_OPEN = "https://api.thingspeak.com/update?api_key=U5I3VHW3OVU46IDA&field1=0"; // URL para el botón "Abrir"
    private static final String THINGSPEAK_URL_CLOSE = "https://api.thingspeak.com/update?api_key=U5I3VHW3OVU46IDA&field1=180"; // URL para el botón "Cerrar"

    private Button btnOpen;
    private Button btnClose;
    private boolean isActionInProgress = false; // Para evitar que se presionen los botones múltiples veces

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializando los botones de la interfaz
        btnOpen = findViewById(R.id.Abrir);
        btnClose = findViewById(R.id.Cerrar);

        // Cuando el botón "Abrir" es presionado, se envía la URL de abrir (valor 0)
        btnOpen.setOnClickListener(view -> {
            if (!isActionInProgress) {
                sendCommand(THINGSPEAK_URL_OPEN, true); // Ejecutar el comando de abrir
                toggleButtons(false); // Desactivar ambos botones
            }
        });

        // Cuando el botón "Cerrar" es presionado, se envía la URL de cerrar (valor 180)
        btnClose.setOnClickListener(view -> {
            if (!isActionInProgress) {
                sendCommand(THINGSPEAK_URL_CLOSE, false); // Ejecutar el comando de cerrar
                toggleButtons(false); // Desactivar ambos botones
            }
        });
    }

    private void sendCommand(String urlString, boolean isOpenCommand) {
        isActionInProgress = true; // Marca la acción como en progreso

        // Inicia un hilo para realizar la solicitud HTTP
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // Crear la URL de la solicitud
                URL url = new URL(urlString);
                Log.d(TAG, "URL generada: " + url.toString()); // Log de la URL generada

                // Abrir una nueva conexión HTTP
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // Tiempo de espera de conexión (20 segundos)
                connection.setReadTimeout(5000); // Tiempo de espera de lectura (20 segundos)

                // Verificar la respuesta del servidor
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Si la respuesta es exitosa, mostrar un mensaje en pantalla
                    runOnUiThread(() -> Toast.makeText(this, "Comando enviado: " + urlString, Toast.LENGTH_SHORT).show());
                } else {
                    // Si la respuesta es un error, mostrar el código de error
                    runOnUiThread(() -> Toast.makeText(this, "Error: Código " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al enviar comando: " + e.getMessage(), e);
                // Si ocurre un error, mostrar el mensaje en pantalla
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                // Asegurarse de desconectar la conexión HTTP
                if (connection != null) {
                    connection.disconnect();
                }
            }

            // Cambiar los botones y esperar 5 segundos
            runOnUiThread(() -> {
                // Esperar 5 segundos antes de reactivar los botones
                new Handler().postDelayed(() -> {
                    toggleButtons(true); // Reactivar los botones
                    isActionInProgress = false; // Termina la acción, permite presionar los botones
                }, 5000); // 5000 milisegundos = 5 segundos
            });
        }).start();
    }

    // Método para habilitar o deshabilitar los botones
    private void toggleButtons(boolean enable) {
        runOnUiThread(() -> {
            btnOpen.setEnabled(enable); // Habilitar o deshabilitar el botón "Abrir"
            btnClose.setEnabled(enable); // Habilitar o deshabilitar el botón "Cerrar"
        });
    }

    // Método para abrir la pantalla de información (opcional, no relacionado con ThingSpeak)
    public void Info(View view) {
        Intent intent = new Intent(this, Informacion.class);
        startActivity(intent);
    }
}
