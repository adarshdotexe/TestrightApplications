package in.testright.watertest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import in.testright.watertest.models.Program;

public class TestActivity extends AppCompatActivity {

    private static final int HARDCODED_INT = 401;
    Activity activity = TestActivity.this;
    ArrayList<Float> resultData = new ArrayList<>();
    ListView listView;
    Program selectedProgram = null;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ArrayList<Program> programs = (ArrayList<Program>) getIntent().getSerializableExtra("programs");
        listView = findViewById(R.id.programList);
        ProgramAdapter adapter = new ProgramAdapter(getApplicationContext(), programs, ProgramAdapter.TYPE_OPTION);
        listView.setAdapter(adapter);

    }



}
