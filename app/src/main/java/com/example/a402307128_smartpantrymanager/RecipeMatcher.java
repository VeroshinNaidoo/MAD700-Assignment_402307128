package com.example.a402307128_smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The strict-matching rule (core business logic of the app).
 *
 * A recipe is "suggested" ONLY IF every ingredient it requires is present in
 * the pantry in at least the required quantity. One missing ingredient, or
 * one ingredient with too little quantity, excludes the whole recipe.
 *
 * To cope with real-world messiness the matcher:
 *  - ignores case, extra spaces and punctuation ("  EGG " == "egg")
 *  - treats simple plurals as the same word ("tomatoes" == "tomato")
 *  - converts compatible units ("1 kg" satisfies "500 g", "1 tbsp" satisfies "2 tsp")
 *  - adds up several pantry rows of the same ingredient (100 g + 150 g = 250 g)
 *
 * This class has no Android dependencies, so it can be unit tested on the JVM.
 */
public final class RecipeMatcher {

    private static final double EPSILON = 1e-6;   // tolerance for floating point maths

    private static final Map<String, String> UNIT_ALIASES = new HashMap<>();
    private static final Map<String, String> UNIT_FAMILY = new HashMap<>();
    private static final Map<String, Double> UNIT_FACTOR = new HashMap<>();
    private static final Map<String, String> IRREGULAR_PLURALS = new HashMap<>();

    static {
        // unit, family, factor to the family's base unit (g / ml / count), aliases...
        defineUnit("g", "mass", 1, "gram", "grams", "gm");
        defineUnit("kg", "mass", 1000, "kilogram", "kilograms", "kilo", "kilos");
        defineUnit("ml", "volume", 1, "millilitre", "millilitres", "milliliter", "milliliters");
        defineUnit("l", "volume", 1000, "litre", "litres", "liter", "liters");
        defineUnit("tsp", "volume", 5, "teaspoon", "teaspoons");
        defineUnit("tbsp", "volume", 15, "tablespoon", "tablespoons");
        defineUnit("cup", "volume", 240, "cups");
        defineUnit("pinch", "volume", 0.3, "pinches");          // roughly 1/16 teaspoon
        defineUnit("pcs", "count", 1, "pc", "piece", "pieces");
        defineUnit("clove", "count", 1, "cloves");
        defineUnit("slice", "count", 1, "slices");

        IRREGULAR_PLURALS.put("loaves", "loaf");
        IRREGULAR_PLURALS.put("leaves", "leaf");
        IRREGULAR_PLURALS.put("halves", "half");
        IRREGULAR_PLURALS.put("knives", "knife");
    }

    private RecipeMatcher() { }   // utility class, never instantiated

    private static void defineUnit(String unit, String family, double factor, String... aliases) {
        UNIT_FAMILY.put(unit, family);
        UNIT_FACTOR.put(unit, factor);
        UNIT_ALIASES.put(unit, unit);
        for (String alias : aliases) {
            UNIT_ALIASES.put(alias, unit);
        }
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /** Returns only the recipes that can be made right now from the given pantry. */
    public static List<Recipe> findSuggestions(List<Recipe> allRecipes, List<Ingredient> pantry) {
        List<Recipe> suggestions = new ArrayList<>();
        if (allRecipes == null || pantry == null || pantry.isEmpty()) {
            return suggestions;                       // nothing in the pantry = nothing to suggest
        }
        Map<String, Double> totals = buildPantryTotals(pantry);
        for (Recipe recipe : allRecipes) {
            if (canMakeFromTotals(recipe, totals)) {
                suggestions.add(recipe);
            }
        }
        return suggestions;
    }

    /** True only when EVERY ingredient of the recipe is covered by the pantry. */
    public static boolean canMake(Recipe recipe, List<Ingredient> pantry) {
        return canMakeFromTotals(recipe, buildPantryTotals(pantry));
    }

    /** Lower-cases, strips punctuation and singularises the last word of a name. */
    public static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\s]", " ")      // keep letters and spaces only
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        int lastSpace = cleaned.lastIndexOf(' ');
        String head = cleaned.substring(0, lastSpace + 1);   // "" when only one word
        String last = cleaned.substring(lastSpace + 1);
        return head + singularize(last);
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    /** Adds up the pantry per (ingredient name + unit family) in base units. */
    private static Map<String, Double> buildPantryTotals(List<Ingredient> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (Ingredient item : pantry) {
            String name = normalizeName(item.getName());
            if (name.isEmpty()) {
                continue;
            }
            String unit = canonicalUnit(item.getUnit());
            String key = name + "|" + familyOf(unit);
            double baseQuantity = item.getQuantity() * factorOf(unit);
            Double current = totals.get(key);
            totals.put(key, (current == null ? 0.0 : current) + baseQuantity);
        }
        return totals;
    }

    private static boolean canMakeFromTotals(Recipe recipe, Map<String, Double> totals) {
        if (recipe.getIngredients().isEmpty()) {
            return false;                              // a recipe with no ingredients is never suggested
        }
        for (RecipeIngredient needed : recipe.getIngredients()) {
            String unit = canonicalUnit(needed.getUnit());
            String key = normalizeName(needed.getName()) + "|" + familyOf(unit);
            Double have = totals.get(key);             // null = ingredient missing (or incompatible unit)
            double required = needed.getQuantity() * factorOf(unit);
            if (have == null || have + EPSILON < required) {
                return false;                          // one failure rejects the whole recipe
            }
        }
        return true;
    }

    private static String singularize(String word) {
        String irregular = IRREGULAR_PLURALS.get(word);
        if (irregular != null) {
            return irregular;
        }
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("ies") && word.length() > 4) {          // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes") || word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) { // tomatoes -> tomato
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss")
                && !word.endsWith("us") && !word.endsWith("is")) {  // eggs -> egg
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private static String canonicalUnit(String raw) {
        if (raw == null) {
            return "";
        }
        String unit = raw.toLowerCase(Locale.ROOT).trim();
        if (unit.endsWith(".")) {
            unit = unit.substring(0, unit.length() - 1);
        }
        String canonical = UNIT_ALIASES.get(unit);
        return canonical != null ? canonical : unit;
    }

    /** Units from different families (e.g. grams vs pieces) can never be compared. */
    private static String familyOf(String canonicalUnit) {
        String family = UNIT_FAMILY.get(canonicalUnit);
        return family != null ? family : "other:" + canonicalUnit;
    }

    private static double factorOf(String canonicalUnit) {
        Double factor = UNIT_FACTOR.get(canonicalUnit);
        return factor != null ? factor : 1.0;
    }
}

