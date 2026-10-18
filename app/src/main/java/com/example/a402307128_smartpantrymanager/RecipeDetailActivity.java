package com.example.a402307128_smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Recipe Detail screen: shows the full ingredient list and method of the
 * recipe whose id was passed in the Intent (EXTRA_RECIPE_ID).
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra holding the id of the recipe to display. */
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        setTitle(R.string.recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // back arrow in the toolbar
        }

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = DatabaseHelper.getInstance(this).getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvIngredients = findViewById(R.id.tv_detail_ingredients);
        TextView tvMethod = findViewById(R.id.tv_detail_method);

        tvName.setText(recipe.getName());

        // Build the bullet list: "• 2 pcs egg"
        StringBuilder builder = new StringBuilder();
        for (RecipeIngredient item : recipe.getIngredients()) {
            builder.append("\u2022 ")
                    .append(Utils.formatQuantity(item.getQuantity()))
                    .append(" ")
                    .append(item.getUnit())
                    .append(" ")
                    .append(item.getName())
                    .append("\n");
        }
        tvIngredients.setText(builder.toString().trim());
        tvMethod.setText(recipe.getMethod());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

