package com.example.imc;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class ComputeActivity extends AppCompatActivity {

    private ImageButton btnBack, btnEmail, btnCalendar;
    private Spinner spinnerGenre;
    private EditText etBirthDate, etPoids, etTaille;
    private RadioGroup rgUnit;
    private RadioButton rbMetre, rbCm;
    private CheckBox cbAffichage;
    private TextView tvResultText;
    private Button btnCalculer, btnRAZ;

    private double currentIMC = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compute);

        // Initialisation des vues
        btnBack = findViewById(R.id.btnBack);
        btnEmail = findViewById(R.id.btnEmail);
        btnCalendar = findViewById(R.id.btnCalendar);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        etBirthDate = findViewById(R.id.etBirthDate);
        etPoids = findViewById(R.id.etPoids);
        etTaille = findViewById(R.id.etTaille);
        rgUnit = findViewById(R.id.rgUnit);
        rbMetre = findViewById(R.id.rbMetre);
        rbCm = findViewById(R.id.rbCm);
        cbAffichage = findViewById(R.id.cbAffichage);
        tvResultText = findViewById(R.id.tvResultText);
        btnCalculer = findViewById(R.id.btnCalculer);
        btnRAZ = findViewById(R.id.btnRAZ);

        // Configuration du Spinner Genre
        String[] genres = {"Homme", "Femme"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(adapter);

        // Bouton Retour avec envoi de l'IMC
        btnBack.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            if (currentIMC != -1) {
                resultIntent.putExtra("IMC_RESULT", String.format(Locale.FRANCE, "%.2f", currentIMC));
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Bouton Email (Intent Implicite)
        btnEmail.setOnClickListener(v -> {
            if (currentIMC == -1) {
                Toast.makeText(this, "Calculez d'abord votre IMC", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "IMC");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "mon IMC est de " + String.format(Locale.FRANCE, "%.2f", currentIMC));
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            }
        });

        // Bouton Calendrier
        btnCalendar.setOnClickListener(v -> showDatePicker(null));
        etBirthDate.setOnClickListener(v -> showDatePicker(etBirthDate));

        // TextWatcher pour réinitialiser le texte du résultat si les champs changent
        TextWatcher fieldsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetResultText();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        etPoids.addTextChangedListener(fieldsWatcher);
        etTaille.addTextChangedListener(fieldsWatcher);
        etBirthDate.addTextChangedListener(fieldsWatcher);

        // Bouton Calculer
        btnCalculer.setOnClickListener(v -> calculateIMC());

        // Bouton RAZ
        btnRAZ.setOnClickListener(v -> showRAZDialog());
    }

    private void showDatePicker(EditText target) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String date = String.format(Locale.FRANCE, "%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);
            if (target != null) target.setText(date);
            else etBirthDate.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void resetResultText() {
        tvResultText.setText(R.string.result_initial_text);
        currentIMC = -1;
    }

    private void calculateIMC() {
        try {
            String poidsStr = etPoids.getText().toString();
            String tailleStr = etTaille.getText().toString();

            if (poidsStr.isEmpty() || tailleStr.isEmpty()) {
                throw new Exception("Champs vides");
            }

            double poids = Double.parseDouble(poidsStr);
            double taille = Double.parseDouble(tailleStr);

            if (poids <= 0 || taille <= 0) {
                throw new Exception("Valeurs nulles ou négatives");
            }

            // Conversion en mètres si nécessaire
            if (rbCm.isChecked()) {
                taille = taille / 100;
            }

            currentIMC = poids / (taille * taille);
            displayResult(currentIMC);

        } catch (Exception e) {
            Toast.makeText(this, "Erreur de saisie : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            resetResultText();
        }
    }

    private void displayResult(double imc) {
        String imcStr = String.format(Locale.FRANCE, "%.2f", imc);
        if (!cbAffichage.isChecked()) {
            tvResultText.setText("Votre IMC est de " + imcStr);
        } else {
            String genre = spinnerGenre.getSelectedItem().toString();
            String prefix = genre.equals("Homme") ? "Monsieur" : "Madame";
            
            // Calcul simplifié de l'âge (juste pour l'exemple d'affichage demandé)
            int age = 25; // TODO: Calculer le vrai âge à partir de etBirthDate
            String category = getIMCCategory(imc);

            tvResultText.setText(String.format("%s, votre IMC est de %s.\nPour votre catégorie d'âge, vous êtes dans la catégorie %s", 
                prefix, imcStr, category));
        }
    }

    private String getIMCCategory(double imc) {
        if (imc < 18.5) return "Maigreur";
        if (imc < 25) return "Normale";
        if (imc < 30) return "Surpoids";
        return "Obésité";
    }

    private void showRAZDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Remise à zéro")
                .setMessage("Voulez-vous vraiment effacer tous les champs ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    etPoids.setText("");
                    etTaille.setText("");
                    etBirthDate.setText("");
                    spinnerGenre.setSelection(0);
                    rbCm.setChecked(true);
                    cbAffichage.setChecked(false);
                    resetResultText();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}
