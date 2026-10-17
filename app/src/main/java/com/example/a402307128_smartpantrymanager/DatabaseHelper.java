package com.example.a402307128_smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // pantry table
    private static final String T_PANTRY = "pantry";
    private static final String P_ID = "id";
    private static final String P_NAME = "name";
    private static final String P_QUANTITY = "quantity";
    private static final String P_UNIT = "unit";
    private static final String P_EXPIRY = "expiry";

    // recipes table
    private static final String T_RECIPES = "recipes";
    private static final String R_ID = "id";
    private static final String R_NAME = "name";
    private static final String R_METHOD = "method";

    // recipe_ingredients table
    private static final String T_RECIPE_ING = "recipe_ingredients";
    private static final String RI_ID = "id";
    private static final String RI_RECIPE_ID = "recipe_id";
    private static final String RI_NAME = "name";
    private static final String RI_QUANTITY = "quantity";
    private static final String RI_UNIT = "unit";

    private static DatabaseHelper instance;

    /** Singleton so every screen shares one database connection. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + P_NAME + " TEXT NOT NULL, "
                + P_QUANTITY + " REAL NOT NULL, "
                + P_UNIT + " TEXT NOT NULL, "
                + P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + R_NAME + " TEXT NOT NULL, "
                + R_METHOD + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_RECIPE_ING + " ("
                + RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RI_RECIPE_ID + " INTEGER NOT NULL, "
                + RI_NAME + " TEXT NOT NULL, "
                + RI_QUANTITY + " REAL NOT NULL, "
                + RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + RI_RECIPE_ID + ") REFERENCES " + T_RECIPES + "(" + R_ID + ") ON DELETE CASCADE)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy for this assignment: rebuild everything.
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // ==================================================================
    // Pantry CRUD
    // ==================================================================

    /** CREATE: inserts a new ingredient and returns its new row id (-1 on failure). */
    public long addIngredient(Ingredient ingredient) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(ingredient));
    }

    /** READ: all ingredients, alphabetical. */
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null,
                null, null, P_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(cursorToIngredient(c));
            }
        }
        return list;
    }

    /** READ: one ingredient by id, or null when it does not exist. */
    public Ingredient getIngredient(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, P_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? cursorToIngredient(c) : null;
        }
    }

    /** UPDATE: overwrites the row that has the same id. Returns rows changed. */
    public int updateIngredient(Ingredient ingredient) {
        return getWritableDatabase().update(T_PANTRY, toValues(ingredient), P_ID + "=?",
                new String[]{String.valueOf(ingredient.getId())});
    }

    /** DELETE: removes one ingredient. Returns rows deleted. */
    public int deleteIngredient(long id) {
        return getWritableDatabase().delete(T_PANTRY, P_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(Ingredient ingredient) {
        ContentValues values = new ContentValues();
        values.put(P_NAME, ingredient.getName());
        values.put(P_QUANTITY, ingredient.getQuantity());
        values.put(P_UNIT, ingredient.getUnit());
        values.put(P_EXPIRY, ingredient.getExpiry());   // null is stored as SQL NULL
        return values;
    }

    private Ingredient cursorToIngredient(Cursor c) {
        int expiryIndex = c.getColumnIndexOrThrow(P_EXPIRY);
        return new Ingredient(
                c.getLong(c.getColumnIndexOrThrow(P_ID)),
                c.getString(c.getColumnIndexOrThrow(P_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(P_QUANTITY)),
                c.getString(c.getColumnIndexOrThrow(P_UNIT)),
                c.isNull(expiryIndex) ? null : c.getString(expiryIndex));
    }

    // ==================================================================
    // Recipes (read only)
    // ==================================================================

    /** All recipes, each with its ingredient list filled in. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null,
                R_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe recipe = cursorToRecipe(c);
                loadRecipeIngredients(db, recipe);
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    /** One recipe (with ingredients) by id, or null when it does not exist. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, R_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }
            Recipe recipe = cursorToRecipe(c);
            loadRecipeIngredients(db, recipe);
            return recipe;
        }
    }

    private Recipe cursorToRecipe(Cursor c) {
        return new Recipe(
                c.getLong(c.getColumnIndexOrThrow(R_ID)),
                c.getString(c.getColumnIndexOrThrow(R_NAME)),
                c.getString(c.getColumnIndexOrThrow(R_METHOD)));
    }

    private void loadRecipeIngredients(SQLiteDatabase db, Recipe recipe) {
        try (Cursor c = db.query(T_RECIPE_ING, null, RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipe.getId())}, null, null, RI_ID + " ASC")) {
            while (c.moveToNext()) {
                recipe.addIngredient(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow(RI_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(RI_QUANTITY)),
                        c.getString(c.getColumnIndexOrThrow(RI_UNIT))));
            }
        }
    }

    // ==================================================================
    // Seed data (18 recipes) - ingredient format is "name:quantity:unit"
    // ==================================================================

    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Scrambled Eggs",
                steps("Crack the eggs into a bowl, add the salt and whisk.",
                        "Melt the butter in a pan over medium heat.",
                        "Pour in the eggs and stir gently until just set.",
                        "Serve immediately."),
                "egg:2:pcs", "butter:1:tbsp", "salt:1:pinch");

        insertRecipe(db, "Tomato Omelette",
                steps("Dice the tomato.",
                        "Whisk the eggs with the salt.",
                        "Heat the oil and cook the tomato for 2 minutes.",
                        "Pour in the eggs, cook until set, fold and serve."),
                "egg:3:pcs", "tomato:1:pcs", "oil:1:tbsp", "salt:1:pinch");

        insertRecipe(db, "Cheese Toast",
                steps("Butter one side of each bread slice.",
                        "Place the cheese between the slices, butter side out.",
                        "Toast in a pan for 2-3 minutes per side until golden."),
                "bread:2:slice", "cheese:2:slice", "butter:1:tbsp");

        insertRecipe(db, "Garlic Butter Pasta",
                steps("Boil the pasta in salted water until al dente, then drain.",
                        "Melt the butter and fry the crushed garlic for 1 minute.",
                        "Toss the pasta in the garlic butter and season with the salt."),
                "pasta:200:g", "garlic:3:clove", "butter:2:tbsp", "salt:1:pinch");

        insertRecipe(db, "Simple Tomato Pasta",
                steps("Boil the pasta until al dente and drain.",
                        "Heat the olive oil and fry the chopped garlic for 1 minute.",
                        "Add the chopped tomatoes and simmer for 10 minutes.",
                        "Season with the salt and stir through the pasta."),
                "pasta:200:g", "tomato:3:pcs", "garlic:2:clove", "olive oil:2:tbsp", "salt:1:pinch");

        insertRecipe(db, "Egg Fried Rice",
                steps("Cook the rice and let it cool.",
                        "Fry the chopped onion in the oil until soft.",
                        "Push the onion aside, scramble the eggs in the pan.",
                        "Add the rice and soy sauce and stir-fry for 3 minutes."),
                "rice:1:cup", "egg:2:pcs", "onion:1:pcs", "soy sauce:2:tbsp", "oil:1:tbsp");

        insertRecipe(db, "Pancakes",
                steps("Whisk the flour, sugar, egg and milk into a smooth batter.",
                        "Melt a little butter in a pan over medium heat.",
                        "Pour in a ladle of batter and cook until bubbles form.",
                        "Flip and cook for another minute."),
                "flour:150:g", "milk:200:ml", "egg:1:pcs", "sugar:1:tbsp", "butter:1:tbsp");

        insertRecipe(db, "French Toast",
                steps("Whisk the eggs, milk and sugar in a shallow dish.",
                        "Dip each bread slice so both sides are coated.",
                        "Fry in the butter until golden on both sides."),
                "bread:4:slice", "egg:2:pcs", "milk:100:ml", "sugar:1:tsp", "butter:1:tbsp");

        insertRecipe(db, "Banana Smoothie",
                steps("Peel and slice the bananas.",
                        "Blend the bananas, milk and sugar until smooth.",
                        "Pour into a glass and serve cold."),
                "banana:2:pcs", "milk:250:ml", "sugar:1:tsp");

        insertRecipe(db, "Mashed Potatoes",
                steps("Peel and chop the potatoes, then boil until soft (about 15 minutes).",
                        "Drain and mash with the butter and warm milk.",
                        "Season with the salt."),
                "potato:4:pcs", "butter:2:tbsp", "milk:100:ml", "salt:1:pinch");

        insertRecipe(db, "Tomato Soup",
                steps("Chop the onion, garlic and tomatoes.",
                        "Fry the onion and garlic in the oil until soft.",
                        "Add the tomatoes and salt, simmer for 20 minutes.",
                        "Blend until smooth and serve hot."),
                "tomato:5:pcs", "onion:1:pcs", "garlic:2:clove", "oil:1:tbsp", "salt:1:tsp");

        insertRecipe(db, "Egg Noodle Stir Fry",
                steps("Boil the noodles and drain.",
                        "Fry the sliced onion in the oil for 2 minutes.",
                        "Add the beaten eggs and scramble.",
                        "Add the noodles and soy sauce and toss for 2 minutes."),
                "noodles:200:g", "egg:2:pcs", "soy sauce:2:tbsp", "oil:1:tbsp", "onion:1:pcs");

        insertRecipe(db, "Chicken Stir Fry",
                steps("Slice the chicken, onion and garlic.",
                        "Heat the oil and fry the chicken until cooked through.",
                        "Add the onion and garlic and fry for 3 minutes.",
                        "Stir in the soy sauce and serve."),
                "chicken:300:g", "onion:1:pcs", "garlic:2:clove", "soy sauce:2:tbsp", "oil:1:tbsp");

        insertRecipe(db, "Garlic Rice",
                steps("Cook the rice.",
                        "Fry the minced garlic in the butter until fragrant.",
                        "Stir the garlic butter and salt through the rice."),
                "rice:1:cup", "garlic:4:clove", "butter:1:tbsp", "salt:1:pinch");

        insertRecipe(db, "Cheese Omelette",
                steps("Whisk the eggs with the salt.",
                        "Melt the butter in a pan and pour in the eggs.",
                        "When almost set, sprinkle over the cheese and fold."),
                "egg:3:pcs", "cheese:50:g", "butter:1:tbsp", "salt:1:pinch");

        insertRecipe(db, "Banana Oatmeal",
                steps("Heat the milk and oats in a pot, stirring for 5 minutes.",
                        "Slice the banana and stir in the sugar.",
                        "Top the oatmeal with the banana and serve."),
                "oats:1:cup", "milk:250:ml", "banana:1:pcs", "sugar:1:tsp");

        insertRecipe(db, "Garlic Potato Wedges",
                steps("Heat the oven to 200 C.",
                        "Cut the potatoes into wedges and toss with the olive oil, garlic and salt.",
                        "Bake for 35-40 minutes, turning once."),
                "potato:3:pcs", "olive oil:2:tbsp", "garlic:2:clove", "salt:1:tsp");

        insertRecipe(db, "Peanut Butter Sandwich",
                steps("Spread the peanut butter over one slice of bread.",
                        "Top with the second slice and cut in half."),
                "bread:2:slice", "peanut butter:2:tbsp");
    }

    /** Inserts one recipe and its ingredient lines, e.g. "egg:2:pcs". */
    private void insertRecipe(SQLiteDatabase db, String name, String method, String... ingredientSpecs) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(R_NAME, name);
        recipeValues.put(R_METHOD, method);
        long recipeId = db.insert(T_RECIPES, null, recipeValues);

        for (String spec : ingredientSpecs) {
            String[] parts = spec.split(":");           // [name, quantity, unit]
            ContentValues values = new ContentValues();
            values.put(RI_RECIPE_ID, recipeId);
            values.put(RI_NAME, parts[0].trim());
            values.put(RI_QUANTITY, Double.parseDouble(parts[1]));
            values.put(RI_UNIT, parts[2].trim());
            db.insert(T_RECIPE_ING, null, values);
        }
    }

    /** Numbers the steps: "1. First\n2. Second". */
    private static String steps(String... steps) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < steps.length; i++) {
            builder.append(i + 1).append(". ").append(steps[i]);
            if (i < steps.length - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}

