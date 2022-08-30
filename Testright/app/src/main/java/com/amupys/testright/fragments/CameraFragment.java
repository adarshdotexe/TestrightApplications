package com.amupys.testright.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amupys.testright.FragmentCallback;
import com.amupys.testright.R;


public class CameraFragment extends Fragment implements View.OnClickListener {

    private TextView proceed;
    private FragmentCallback fragmentCallback;
    private Button btn_first_image, btn_Second_image,
    btn_first_gal, btn_second_gal;
    int count = 0;


    public CameraFragment() { }

    public void setFragmentCallback(FragmentCallback fragmentCallback) {
        this.fragmentCallback = fragmentCallback;
    }

    public void setProceed(int id){
        if(id == 0){
            btn_first_image.setText(R.string.done);
            btn_first_image.setEnabled(false);
            btn_first_gal.setEnabled(false);
            btn_Second_image.setEnabled(true);
            btn_second_gal.setEnabled(true);
        }else {
            btn_Second_image.setText(R.string.done);
            btn_Second_image.setEnabled(false);
            btn_second_gal.setEnabled(false);
            proceed.setBackgroundColor(getResources().getColor(R.color.teal_200));
            count = id;
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
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        btn_first_image = view.findViewById(R.id.btn1_cam_frag);
        btn_Second_image = view.findViewById(R.id.btn2_cam_frag);
        btn_first_gal = view.findViewById(R.id.btn1_gallery_frag);
        btn_second_gal = view.findViewById(R.id.btn2_gallery_frag);
        proceed = view.findViewById(R.id.frag_proceed);

        btn_first_image.setOnClickListener(this);
        btn_Second_image.setOnClickListener(this);
        proceed.setOnClickListener(this);
        btn_first_gal.setOnClickListener(this);
        btn_second_gal.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn1_cam_frag:
            case R.id.btn2_cam_frag:
                fragmentCallback.onCamButtonClick();
                break;
            case R.id.btn1_gallery_frag:
            case R.id.btn2_gallery_frag:
                fragmentCallback.onGalleryButtonClick();
                break;
            case R.id.frag_proceed:
                if(count == 1) fragmentCallback.onProceedClick();
                break;
        }
    }
}