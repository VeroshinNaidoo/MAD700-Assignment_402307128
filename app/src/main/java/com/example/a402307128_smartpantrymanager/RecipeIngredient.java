package com.example.a402307128_smartpantrymanager;

/**
 * One line of a recipe's ingredient list, e.g. "2 pcs egg".
 * Maps to one row of the "recipe_ingredients" table.
 */
public class RecipeIngredient {

    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
