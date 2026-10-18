package com.example.a402307128_smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry List screen: shows every ingredient from the database in a
 * RecyclerView. The "+" button opens the Add screen, the pencil opens the
 * Edit screen (via Intents) and the bin deletes after a confirmation dialog.
 */
public class PantryFragment extends Fragment implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_pantry, container, false);

        db = DatabaseHelper.getInstance(requireContext());
        tvEmpty = view.findViewById(R.id.tv_empty_pantry);
        recyclerView = view.findViewById(R.id.rv_pantry);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);

        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Explicit Intent: open the Add/Edit screen in "add" mode (no id extra)
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditIngredientActivity.class)));

        return view;
    }

    /** Reload every time the screen becomes visible, so edits made elsewhere show up. */
    @Override
    public void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<Ingredient> items = db.getAllIngredients();
        adapter.setHighlightExpiring(Utils.isExpiringAlertsEnabled(requireContext()));
        adapter.setItems(items);

        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEditClicked(Ingredient ingredient) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(Ingredient ingredient) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    db.deleteIngredient(ingredient.getId());
                    loadPantry();
                    Toast.makeText(requireContext(), "Ingredient deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
