package com.example.a402307128_smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for a recipe: a name, preparation steps and the list of
 * ingredients it requires. Recipes are seeded into the database on first run.
 */
public class Recipe {

    private final long id;
    private final String name;
    private final String method;                       // numbered steps separated by newlines
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getMethod() { return method; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
