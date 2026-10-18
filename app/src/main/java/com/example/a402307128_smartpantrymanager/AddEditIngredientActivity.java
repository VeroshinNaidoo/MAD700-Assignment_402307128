package com.example.a402307128_smartpantrymanager;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add / Edit Ingredient screen (one Activity, two modes).
 *  - Opened without extras        -> ADD mode (creates a new pantry row)
 *  - Opened with EXTRA_INGREDIENT_ID -> EDIT mode (loads and updates that row)
 * All input is validated before anything is written to the database.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra holding the id of the ingredient being edited. */
    public static final String EXTRA_INGREDIENT_ID = "ingredient_id";

    private static final long NO_ID = -1;
    private static final double MAX_QUANTITY = 100000;

    private TextInputLayout tilName;
    private TextInputLayout tilQuantity;
    private TextInputEditText etName;
    private TextInputEditText etQuantity;
    private TextInputEditText etExpiry;
    private Spinner spinnerUnit;

    private DatabaseHelper db;
    private long editingId = NO_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = DatabaseHelper.getInstance(this);

        tilName = findViewById(R.id.til_name);
        tilQuantity = findViewById(R.id.til_quantity);
        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etExpiry = findViewById(R.id.et_expiry);
        spinnerUnit = findViewById(R.id.spinner_unit);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);

        // Fill the unit drop-down from R.array.units_array
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units_array, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        etExpiry.setOnClickListener(v -> showDatePicker());

        // Decide between ADD and EDIT mode from the Intent
        editingId = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, NO_ID);
        if (editingId != NO_ID) {
            setTitle(R.string.edit_ingredient);
            loadIngredient();
        } else {
            setTitle(R.string.add_ingredient);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // back arrow in the toolbar
        }

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveIngredient());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** EDIT mode: copy the stored values into the form fields. */
    private void loadIngredient() {
        Ingredient ingredient = db.getIngredient(editingId);
        if (ingredient == null) {              // row was deleted in the meantime
            finish();
            return;
        }
        etName.setText(ingredient.getName());
        etQuantity.setText(Utils.formatQuantity(ingredient.getQuantity()));
        if (ingredient.getExpiry() != null) {
            etExpiry.setText(ingredient.getExpiry());
        }

        String[] units = getResources().getStringArray(R.array.units_array);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(ingredient.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
    }

    /** Date picker for the optional expiry date; the Clear button removes the date again. */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String current = textOf(etExpiry);
        if (current.matches("\\d{4}-\\d{2}-\\d{2}")) {       // start on the date already chosen
            String[] parts = current.split("-");
            calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[2]));
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> etExpiry.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Clear",
                (d, which) -> etExpiry.setText(""));
        dialog.show();
    }

    /** Validates the form; shows an error under each invalid field. */
    private boolean validateInputs() {
        boolean valid = true;
        tilName.setError(null);
        tilQuantity.setError(null);

        if (textOf(etName).isEmpty()) {
            tilName.setError(getString(R.string.error_empty_name));
            valid = false;
        }

        Double quantity = parseQuantity(textOf(etQuantity));
        if (quantity == null || quantity <= 0 || quantity > MAX_QUANTITY) {
            tilQuantity.setError(getString(R.string.error_invalid_quantity));
            valid = false;
        }

        if (spinnerUnit.getSelectedItem() == null) {
            Toast.makeText(this, R.string.error_empty_unit, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    private void saveIngredient() {
        if (!validateInputs()) {
            return;                               // stay on the screen and show the errors
        }

        String name = textOf(etName);
        double quantity = parseQuantity(textOf(etQuantity));
        String unit = spinnerUnit.getSelectedItem().toString();
        String expiry = textOf(etExpiry);
        if (expiry.isEmpty()) {
            expiry = null;                        // the expiry date is optional
        }

        if (editingId == NO_ID) {
            db.addIngredient(new Ingredient(name, quantity, unit, expiry));           // CREATE
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateIngredient(new Ingredient(editingId, name, quantity, unit, expiry)); // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();                                 // back to the pantry list
    }

    /** Parses a number, accepting both "2.5" and "2,5" (some phone locales use a comma). Null if invalid. */
    private Double parseQuantity(String raw) {
        try {
            return Double.valueOf(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}

