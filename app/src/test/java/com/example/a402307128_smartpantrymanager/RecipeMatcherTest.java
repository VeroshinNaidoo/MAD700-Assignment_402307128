package com.example.a402307128_smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Local unit tests for the strict-matching rule (section 2.3 of the brief).
 * Run with: right-click this file in Android Studio > Run 'RecipeMatcherTest'.
 */
public class RecipeMatcherTest {

    private static Recipe recipe(String name, RecipeIngredient... items) {
        Recipe r = new Recipe(1, name, "1. Cook.");
        for (RecipeIngredient item : items) {
            r.addIngredient(item);
        }
        return r;
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static Ingredient have(String name, double qty, String unit) {
        return new Ingredient(name, qty, unit, null);
    }

    @Test
    public void allIngredientsPresent_recipeIsSuggested() {
        Recipe r = recipe("Scrambled Eggs", need("egg", 2, "pcs"), need("butter", 1, "tbsp"));
        List<Ingredient> pantry = Arrays.asList(have("Egg", 6, "pcs"), have("Butter", 2, "tbsp"));
        assertTrue(RecipeMatcher.canMake(r, pantry));
    }

    @Test
    public void fourOfFiveIngredients_recipeIsNotSuggested() {
        Recipe r = recipe("Fried Rice", need("rice", 1, "cup"), need("egg", 2, "pcs"),
                need("onion", 1, "pcs"), need("soy sauce", 2, "tbsp"), need("oil", 1, "tbsp"));
        List<Ingredient> pantry = Arrays.asList(have("Rice", 2, "cup"), have("Egg", 4, "pcs"),
                have("Onion", 1, "pcs"), have("Soy Sauce", 5, "tbsp"));   // no oil
        assertFalse(RecipeMatcher.canMake(r, pantry));
    }

    @Test
    public void notEnoughQuantity_recipeIsNotSuggested() {
        Recipe r = recipe("Omelette", need("egg", 3, "pcs"));
        assertFalse(RecipeMatcher.canMake(r, Arrays.asList(have("Egg", 2, "pcs"))));
    }

    @Test
    public void singularAndPluralNames_match() {
        Recipe r = recipe("Tomato Soup", need("tomato", 2, "pcs"));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(have("Tomatoes", 3, "pcs"))));
        Recipe plural = recipe("Egg Dish", need("eggs", 2, "pcs"));
        assertTrue(RecipeMatcher.canMake(plural, Arrays.asList(have("Egg", 2, "pcs"))));
    }

    @Test
    public void caseAndWhitespace_areIgnored() {
        Recipe r = recipe("Toast", need("bread", 2, "slice"));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(have("  BREAD ", 2, "slice"))));
    }

    @Test
    public void compatibleUnits_areConverted() {
        Recipe r = recipe("Pancakes", need("flour", 150, "g"));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(have("Flour", 1, "kg"))));
        Recipe spoons = recipe("Sweet", need("sugar", 2, "tsp"));
        assertTrue(RecipeMatcher.canMake(spoons, Arrays.asList(have("Sugar", 1, "tbsp"))));
    }

    @Test
    public void incompatibleUnits_doNotMatch() {
        Recipe r = recipe("Pancakes", need("flour", 1, "cup"));
        assertFalse(RecipeMatcher.canMake(r, Arrays.asList(have("Flour", 500, "g"))));
    }

    @Test
    public void duplicatePantryRows_areAddedTogether() {
        Recipe r = recipe("Bread", need("flour", 200, "g"));
        List<Ingredient> pantry = Arrays.asList(have("Flour", 100, "g"), have("Flour", 150, "g"));
        assertTrue(RecipeMatcher.canMake(r, pantry));
    }

    @Test
    public void emptyPantry_returnsNoSuggestions() {
        List<Recipe> all = new ArrayList<>();
        all.add(recipe("Toast", need("bread", 1, "slice")));
        assertEquals(0, RecipeMatcher.findSuggestions(all, new ArrayList<Ingredient>()).size());
    }

    @Test
    public void findSuggestions_returnsOnlyMakeableRecipes() {
        List<Recipe> all = new ArrayList<>();
        all.add(recipe("Toast", need("bread", 1, "slice")));
        all.add(recipe("Cheese Toast", need("bread", 1, "slice"), need("cheese", 1, "slice")));
        List<Ingredient> pantry = Arrays.asList(have("Bread", 4, "slice"));
        List<Recipe> result = RecipeMatcher.findSuggestions(all, pantry);
        assertEquals(1, result.size());
        assertEquals("Toast", result.get(0).getName());
    }
}
