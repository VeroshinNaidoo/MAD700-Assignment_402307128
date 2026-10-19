package com.example.a402307128_smartpantrymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Settings screen: a single switch that turns "expiring soon" highlighting
 * on the pantry list on or off. The value is saved with SharedPreferences.
 */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);

        SwitchMaterial switchExpiring = view.findViewById(R.id.switch_expiring);
        switchExpiring.setChecked(Utils.isExpiringAlertsEnabled(requireContext()));
        switchExpiring.setOnCheckedChangeListener((button, isChecked) ->
                Utils.setExpiringAlertsEnabled(requireContext(), isChecked));

        return view;
    }
}

