# Smart Pantry Manager

**Module:** Mobile App Development 700 (Richfield), Practical Assignment
**Author:** Veroshin Naidoo, 402307128

Smart Pantry Manager is a Java Android app built to cut down on food waste. Users log
the ingredients they already have at home, and the app shows only the recipes they can
make straight away, with no extra shopping needed.

## Features

- **Pantry management:** Add, edit and delete ingredients (name, quantity, unit and an optional expiry date), with input validation.
- **Pantry list:** A RecyclerView with a custom adapter, backed by the database.
- **18 built-in recipes:** Stored in the database and loaded the first time the app runs.
- **Suggested Recipes:** Shows only recipes whose ingredients are **all** available in the pantry (strict matching).
- **Recipe detail:** Displays the full ingredient list and method for a recipe.
- **Settings:** A switch that highlights ingredients expiring within 3 days, saved using SharedPreferences.
- **Empty states:** Clear messages when the pantry is empty or no recipes match.
- **Navigation:** A bottom navigation bar links the Pantry, Suggestions and Settings screens.

The app does not use Google Maps, any mapping SDK, or location/GPS features.

## Database choice: SQLite

I used **SQLite** (via `SQLiteOpenHelper`) for these reasons:

1. The data is personal and small, so a local on-device database is sufficient.
2. It works **offline**, which matters for an app used in the kitchen where signal may be poor.
3. It requires no server, account or API key, so the project runs on any machine with Android Studio.
4. It supports full CRUD and real persistence, so data survives closing and reopening the app.
5. It matches the approach taught in the module's persistent-data chapter.

The database has three tables: `pantry`, `recipes`, and `recipe_ingredients` (which has a foreign key to `recipes`).

## The strict-matching rule

A recipe is suggested **only when every ingredient it requires is in the pantry in at
least the needed quantity**. If a single ingredient is missing or there isn't enough of
it, the recipe is left out entirely. This logic is in `RecipeMatcher.java` and is
tested by `RecipeMatcherTest.java`.

To handle messy real-world input, the matcher:

- ignores letter case, extra spaces and punctuation
- treats simple plurals as the same item (`tomato` = `tomatoes`, `egg` = `eggs`)
- converts between compatible units (`1 kg` satisfies `500 g`; `1 tbsp` satisfies `2 tsp`)
- adds together multiple pantry entries for the same ingredient (`100 g + 150 g = 250 g`)

Assumptions: grams and millilitres are not converted to each other because that would
require a density value, so `500 g flour` does not satisfy `1 cup flour`. The units
`pcs`, `clove` and `slice` are treated as one "count" group, and a pinch is taken as
roughly 0.3 ml. "Oil" and "Olive oil" are considered separate ingredients.

## Setup and running

1. Install the current **Android Studio**.
2. Clone the repository: `git clone <repository-url>`
3. Open the project folder in Android Studio and wait for **Gradle sync** to complete.
4. Create an emulator (Device Manager > Create device, API 24 or higher), or connect a phone with USB debugging enabled.
5. Click **Run** (the green triangle).
6. To run the unit tests, right-click `RecipeMatcherTest` and choose *Run 'RecipeMatcherTest'*.

## Project structure

| File | Purpose |
|---|---|
| `MainActivity` | Holds the bottom navigation and the three main fragments |
| `PantryFragment` | Displays the pantry list, handles edit/delete, and opens the Add/Edit screen |
| `AddEditIngredientActivity` | Adds or edits a single ingredient, with validation |
| `SuggestedRecipesFragment` | Runs the strict matcher and shows the matching recipes |
| `RecipeDetailActivity` | Shows a recipe's ingredients and method |
| `SettingsFragment` | Contains the expiring-soon alerts switch |
| `DatabaseHelper` | Defines the SQLite schema, CRUD operations and recipe seed data |
| `RecipeMatcher` | Implements the strict-matching algorithm |
| `PantryAdapter`, `RecipeAdapter` | RecyclerView adapters |
| `Ingredient`, `Recipe`, `RecipeIngredient` | Model classes |
| `Utils` | Quantity formatting, expiry-date calculations and settings access |