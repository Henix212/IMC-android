package com.example.imc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class connectionPage extends AppCompatActivity {

    private boolean isLoginMode = true;
    private EditText etNom, etPassword;
    private Button btnAction;
    private TextView tvToggle;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connection);

        sharedPreferences = getSharedPreferences("IMC_Prefs", MODE_PRIVATE);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        etNom = findViewById(R.id.nom);
        etPassword = findViewById(R.id.password);
        btnAction = findViewById(R.id.button);
        tvToggle = findViewById(R.id.tv_not_registered);

        tvToggle.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            clearFields();
            if (isLoginMode) {
                btnAction.setText(R.string.label_login_btn);
                tvToggle.setText(R.string.not_registered);
            } else {
                btnAction.setText(R.string.label_register_btn);
                tvToggle.setText(R.string.registered);
            }
        });

        btnAction.setOnClickListener(v -> {
            String username = etNom.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                performLogin(username, password);
            } else {
                performRegister(username, password);
            }
        });
    }

    private void clearFields() {
        if (etNom != null) etNom.setText("");
        if (etPassword != null) etPassword.setText("");
    }

    private void performLogin(String username, String password) {
        String savedPassword = sharedPreferences.getString(username, null);
        
        if (savedPassword != null && savedPassword.equals(password)) {
            sharedPreferences.edit().putString("CURRENT_USER", username).apply();
            
            Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(this, ComputeActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
        }
    }

    private void performRegister(String username, String password) {
        if (sharedPreferences.contains(username)) {
            Toast.makeText(this, "Cet utilisateur existe déjà", Toast.LENGTH_SHORT).show();
        } else {
            sharedPreferences.edit().putString(username, password).apply();
            Toast.makeText(this, "Compte créé ! Connectez-vous.", Toast.LENGTH_SHORT).show();
            tvToggle.performClick();
        }
    }
}
