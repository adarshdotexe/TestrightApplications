package in.testright.diamsure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class CameraFragment extends Fragment implements View.OnClickListener {
    int count = 0;
    private Button btn_Second_image;
    private Button btn_first_gal;
    private Button btn_first_image;
    private Button btn_second_gal;
    private Button btn_third_gal;
    private Button btn_third_image;
    private FragmentCallback fragmentCallback;
    private TextView proceed;

    public void setFragmentCallback(FragmentCallback fragmentCallback2) {
        this.fragmentCallback = fragmentCallback2;
    }

    public void setProceed(int id) {
        if (id == 0) {
            this.btn_first_image.setText(R.string.done);
            this.btn_first_image.setEnabled(false);
            this.btn_first_gal.setEnabled(false);
            this.btn_Second_image.setEnabled(true);
            this.btn_second_gal.setEnabled(true);
            this.count = id;
        } else if (id == 1) {
            this.btn_Second_image.setText(R.string.done);
            this.btn_Second_image.setEnabled(false);
            this.btn_second_gal.setEnabled(false);
            this.btn_third_image.setEnabled(true);
            this.btn_third_gal.setEnabled(true);
            this.count = id;
        } else if (id == 2) {
            this.btn_third_image.setText(R.string.done);
            this.btn_third_image.setEnabled(false);
            this.btn_third_gal.setEnabled(false);
            this.proceed.setBackgroundColor(ContextCompat.getColor(requireActivity().getApplicationContext(), R.color.teal_700));
            this.count = id;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        this.btn_first_image = view.findViewById(R.id.btn1_cam_frag);
        this.btn_Second_image = view.findViewById(R.id.btn2_cam_frag);
        this.btn_third_image = view.findViewById(R.id.btn3_cam_frag);
        this.btn_first_gal = view.findViewById(R.id.btn1_gallery_frag);
        this.btn_second_gal = view.findViewById(R.id.btn2_gallery_frag);
        this.btn_third_gal = view.findViewById(R.id.btn3_gallery_frag);
        this.proceed = view.findViewById(R.id.frag_proceed);
        this.btn_first_image.setOnClickListener(this);
        this.btn_Second_image.setOnClickListener(this);
        this.btn_third_image.setOnClickListener(this);
        this.proceed.setOnClickListener(this);
        this.btn_first_gal.setOnClickListener(this);
        this.btn_second_gal.setOnClickListener(this);
        this.btn_third_gal.setOnClickListener(this);
        return view;
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn1_cam_frag || id == R.id.btn2_cam_frag || id == R.id.btn3_cam_frag) {
            this.fragmentCallback.onCamButtonClick();
        } else if (id == R.id.btn1_gallery_frag || id == R.id.btn2_gallery_frag || id == R.id.btn3_gallery_frag) {
            this.fragmentCallback.onGalleryButtonClick();
        } else if (id == R.id.frag_proceed && this.count == 2) {
            this.fragmentCallback.onProceedClick();
        }
    }
}
