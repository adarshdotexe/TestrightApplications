package com.amupys.testright2.fragments;

import static com.amupys.testright2.ledControl.programs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.amupys.testright2.ChartModel;
import com.amupys.testright2.ProgramModel;
import com.amupys.testright2.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class ConcFirstFragment extends Fragment implements View.OnClickListener {

    private AutoCompleteTextView ac_question;
    private TextInputLayout tl_question;
    private EditText edt_name, edt_wavelength, edt_conc, edt_if_message, edt_else_message, edt_abs;
    private CheckBox checkBox;
    TextSwitcher textSwitcher;
    ArrayList<Float> values = new ArrayList<>();
    private ConcSecondFragment concSecondFragment;
    boolean modeAdd = true;
    ArrayList<ChartModel> chartData;
    int index = 0;
    private String[] arr
            = { "<", ">",
            "=" };

    public ConcFirstFragment(ConcSecondFragment concSecondFragment) {
        // Required empty public constructor
        this.concSecondFragment = concSecondFragment;
    }

    public void setData(ArrayList<Float> value){
        ConcFirstFragment.this.values.clear();
        ConcFirstFragment.this.values.addAll(value);

        fillData();
        concSecondFragment.setData(chartData);
    }

    private void fillData() {
        if (checkEditTexts()) {
            int compare;
            TextView txt = (TextView) textSwitcher.getCurrentView();
            String caseS = txt.getText().toString();
//            Log.e("caseS", caseS);
            if(caseS.equals("<"))
                compare = 1;
            else if (caseS.equals("="))
                compare = 2;
            else
                compare = 3;

            ProgramModel program = null;

            for(ProgramModel model: programs) {
                if (model.getName() == edt_name.getText().toString())
                    program = model;
            }

            if (program == null){
                program = new ProgramModel(edt_name.getText().toString(), edt_if_message.getText().toString(),
                        edt_else_message.getText().toString(), Integer.parseInt(edt_wavelength.getText().toString()), compare,
                        checkBox.isChecked());

                programs.add(program);
            }

            chartData = program.getList();

            if(chartData == null){
                chartData = new ArrayList<>();
                chartData.add(new ChartModel(Integer.parseInt(edt_conc.getText().toString()),
                        values.get(Integer.parseInt(edt_wavelength.getText().toString())-401)));
            }else {
                ChartModel chartModel = null;
                for (ChartModel m: chartData){
                    if(m.getConcentration() == Integer.parseInt(edt_conc.getText().toString())){
                        chartModel = m;
                    }
                }
                chartModel.setAbs(values.get(Integer.parseInt(edt_wavelength.getText().toString())-401));
            }
        }
    }

    private boolean checkEditTexts() {
        boolean flag = true;
        if (edt_name.getText().toString().isEmpty()) {
            edt_name.setError(getString(R.string.required));
            flag = false;
        }
        if (edt_wavelength.getText().toString().isEmpty()) {
            edt_wavelength.setError(getString(R.string.required));
            flag = false;
        }
        if (edt_conc.getText().toString().isEmpty()) {
            edt_conc.setError(getString(R.string.required));
            flag = false;
        }
        if(checkBox.isChecked()){
            if (edt_if_message.getText().toString().isEmpty()) {
                edt_if_message.setError(getString(R.string.required));
                flag = false;
            }
            if (edt_else_message.getText().toString().isEmpty()) {
                edt_else_message.setError(getString(R.string.required));
                flag = false;
            }
        }
        return flag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conc_first, container, false);

        tl_question = view.findViewById(R.id.txt_question);
        ac_question = (AutoCompleteTextView) tl_question.getEditText();
        textSwitcher = view.findViewById(R.id.txt_relation);
        edt_name = view.findViewById(R.id.conc_edt1);
        edt_wavelength = view.findViewById(R.id.conc_edt2);
        edt_conc = view.findViewById(R.id.conc_edt3);
        edt_if_message = view.findViewById(R.id.conc_message_if);
        edt_else_message = view.findViewById(R.id.conc_message_else);
        edt_abs = view.findViewById(R.id.conc_abs_value);
        checkBox = view.findViewById(R.id.conc_checkbox);

        LinearLayout lay_print = view.findViewById(R.id.layout_print);
        ImageView btn_delete = view.findViewById(R.id.btn_conc_delete);
        RelativeLayout rel_name = view.findViewById(R.id.rel_name);

        btn_delete.setOnClickListener(view1 -> {

        });

        if(checkBox.isChecked())
            lay_print.setVisibility(View.VISIBLE);
        else
            lay_print.setVisibility(View.GONE);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    lay_print.setVisibility(View.VISIBLE);
                }else
                    lay_print.setVisibility(View.GONE);
            }
        });

        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView()
            {
                TextView textView = new TextView(getActivity());
                textView.setTextColor(getResources().getColor(R.color.white));

                textView.setTextSize(22);
                textView.setGravity(Gravity.CENTER);
                return textView;
            }
        });

        ac_question.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("getSelectedItem", adapterView.getSelectedItem().toString());
                Log.e("view", view.toString());

                if(adapterView.getSelectedItem().equals("Create New")){
                    modeAdd = true;
                    lay_print.setVisibility(View.VISIBLE);
                    btn_delete.setVisibility(View.GONE);
                    rel_name.setVisibility(View.VISIBLE);
                }else {
                    modeAdd = false;
                    lay_print.setVisibility(View.GONE);
                    btn_delete.setVisibility(View.VISIBLE);
                    rel_name.setVisibility(View.GONE);

                    edt_wavelength.setEnabled(false);
                    edt_conc.setEnabled(false);

                    String name = (String) adapterView.getSelectedItem();
                    for (ProgramModel m: programs){
                        if(m.equals(name)){
                            edt_wavelength.setText(m.getWavelength());
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        textSwitcher.setOnClickListener(this);
        textSwitcher.setText(arr[0]);

        String[] menu_questions = new String[programs.size()+1];
        menu_questions[0] = "Create New";
        for (int i=1; i<menu_questions.length; i++){
            menu_questions[i] = programs.get(i-1).getName();
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.drop_down_item, menu_questions);
        ac_question.setAdapter(adapter);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.txt_relation:
                if (index == arr.length - 1) {
                    index = 0;
                    textSwitcher.setText(arr[index]);
                }
                else {
                    textSwitcher.setText(arr[++index]);
                }
                break;
        }
    }
}