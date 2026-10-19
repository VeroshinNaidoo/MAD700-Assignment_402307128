package com.example.a402307128_smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Suggested Recipes screen: runs the strict-matching rule (RecipeMatcher)
 * against the current pantry and lists ONLY the recipes that can be made now.
 * When nothing matches, a friendly message is shown instead of a blank list.
 */
public class SuggestedRecipesFragment extends Fragment implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_suggested_recipes, container, false);

        db = DatabaseHelper.getInstance(requireContext());
        tvEmpty = view.findViewById(R.id.tv_empty_suggestions);
        recyclerView = view.findViewById(R.id.rv_suggestions);

        adapter = new RecipeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        return view;
    }

    /** Re-run the matching each time the tab is shown, so pantry changes are reflected. */
    @Override
    public void onResume() {
        super.onResume();
        List<Recipe> suggestions = RecipeMatcher.findSuggestions(db.getAllRecipes(), db.getAllIngredients());
        adapter.setRecipes(suggestions);

        boolean empty = suggestions.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        // Intent carrying the recipe id to the detail screen
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
