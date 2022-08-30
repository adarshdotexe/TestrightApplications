package in.testright.watertest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import in.testright.watertest.models.Program;

public class ProgramAdapter extends ArrayAdapter<Program> {

    public static final int TYPE_SETTING = 1;
    public static final int TYPE_OPTION = 2;
    Context context;
    Activity activity;
    ArrayList<Program> programs;
    Program selectedProgram;
    int type;

    public ProgramAdapter(@NonNull Context context, Activity HomeActivity, ArrayList<Program> programs, int type) {
        super(context, R.layout.activity_home, programs);

        this.programs = programs;
        this.context = context;
        this.type = type;
        this.activity = HomeActivity;


//        if (type == TYPE_OPTION) {
//        } else if (type == TYPE_TEST) {
//
//        } else if (type == TYPE_SETTING) {
//
//        }
    }

    public View getView(int position, View view, ViewGroup parent) {
        Holder holder = null;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (type) {
                case TYPE_OPTION: {
                    view = inflater.inflate(R.layout.program_button, parent, false);
                    holder = new Holder();
                    holder.option = view.findViewById(R.id.materialButtonProgram);
                    view.setTag(holder);
                }
                case TYPE_SETTING: {
                    view = inflater.inflate(R.layout.program_setting, parent, false);
                    holder = new Holder();
                    holder.title = view.findViewById(R.id.programName);
                    holder.a = view.findViewById(R.id.editTextA);
                    holder.b = view.findViewById(R.id.editTextB);
                    holder.c = view.findViewById(R.id.editTextC);
                    holder.x = view.findViewById(R.id.editTextX);

                    holder.option = view.findViewById(R.id.materialButtonProgram);
                    view.setTag(holder);
                }


            }
        } else {
            holder = (Holder) view.getTag();
        }

        switch (type) {
            case TYPE_OPTION: {
                holder.option.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(activity instanceof HomeActivity) {

                        }
                    }
                });

            }
            case TYPE_SETTING: {
                holder.title.setText(programs.get(position).getProgramName());
                holder.a.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Integer.parseInt()
                    }
                });
            }

        }
        return view;
    }

    private class Holder {
        MaterialButton option;
        TextView title;
        EditText a;
        EditText b;
        EditText c;
        EditText x;

    }
}