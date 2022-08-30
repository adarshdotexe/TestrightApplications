package com.amupys.testright2.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.amupys.testright2.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class ConcFragment extends Fragment {

    ConcFirstFragment concFirstFragment;
    ConcSecondFragment concSecondFragment;
    TextView btnAdd;

    public void setValuesList(ArrayList<Float> valuesList) {
        if (concFirstFragment != null) {
            concFirstFragment.setData(valuesList);
            btnAdd.setVisibility(View.VISIBLE);

            if(getActivity() != null) getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.conc_frame, concSecondFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conc, container, false);

        btnAdd = view.findViewById(R.id.conc_add_new);

        concSecondFragment = new ConcSecondFragment();
        concFirstFragment = new ConcFirstFragment(concSecondFragment);

        if(getActivity() != null) getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.conc_frame, concFirstFragment)
                .commit();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAdd.setVisibility(View.INVISIBLE);
                if(getActivity() != null) getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.conc_frame, concFirstFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}