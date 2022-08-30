package com.amupys.testright2.fragments;

import static com.amupys.testright2.ledControl.wavelengths;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amupys.testright2.FragmentCallback;
import com.amupys.testright2.R;
import com.amupys.testright2.ledControl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DetailsFragment extends Fragment implements TextWatcher {

    RotateAnimation anim;
    int[] edt_ids = {R.id.edt1, R.id.edt2, R.id.edt3, R.id.edt4, R.id.edt5,
            R.id.edt6},
            textViews_ids = {R.id.txt1, R.id.txt2, R.id.txt3, R.id.txt4, R.id.txt5, R.id.txt6};

    EditText[] editTexts = new EditText[6];
    TextView[] textViews = new TextView[6];
    ArrayList<Float> floatArrayList;

    public DetailsFragment() { }

    public void setData(ArrayList<Float> list){
//        Log.e("list size", String.valueOf(list.size()));
        floatArrayList = list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(700);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s == editTexts[0].getEditableText()){
            setError(editTexts[0]);
        }else if (s == editTexts[1].getEditableText()){
            setError(editTexts[1]);
        }else if (s == editTexts[2].getEditableText()){
            setError(editTexts[2]);
        }else if (s == editTexts[3].getEditableText()){
            setError(editTexts[3]);
        }else if (s == editTexts[4].getEditableText()){
            setError(editTexts[4]);
        }else if (s == editTexts[5].getEditableText()){
            setError(editTexts[5]);
        }
    }

    private void setError(EditText edt){
        String text = edt.getText().toString();
        if (!text.isEmpty()) {
            try {
                int val = Integer.parseInt(text);
                if (val >= 401 && val <= 700) { } else {
                    edt.setError("Value range between 401–700");
                }
            } catch (Exception e) {
                e.printStackTrace();
                edt.setError("Value range between 401–700");
            }
        } else {
            edt.setError("Required");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        for (int i = 0; i < 6; i++) {
            editTexts[i] = v.findViewById(edt_ids[i]);
            textViews[i] = v.findViewById(textViews_ids[i]);
            editTexts[i].addTextChangedListener(this);
        }

        for(int i=0;i<6;i++){
            editTexts[i].setText(String.valueOf(wavelengths[i]));
        }



        return v;
    }

    public void onButtonClick() {
        getActivity().runOnUiThread(() -> {
            if (ledControl.isBtConnected) {
                for(int i=0; i<6; i++){
                    String value = editTexts[i].getText().toString();
                    if (!value.isEmpty() ) {
                        wavelengths[i] = Integer.parseInt(value);
                        int x = wavelengths[i] - 401;
                        if(floatArrayList.size() > x && x >= 0){
                            textViews[i].setText(String.valueOf(floatArrayList.get(x)));
                        }else
                            textViews[i].setText("NA");
                    } else {
                        Toast.makeText(getContext(), "Empty wavelength!", Toast.LENGTH_SHORT).show();
                    }
                }

            } else {
                Toast.makeText(getContext(), "No device connected", Toast.LENGTH_SHORT).show();
            }
        });
    }
}