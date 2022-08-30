package in.testright.watertest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Login Activity";
    private final AppCompatActivity activity = LoginActivity.this;
    private FirebaseAuth mAuth;
    private InputValidation inputValidation;
    private MaterialButton appCompatButtonLogin;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUi();

    }

    private void initUi() {
        mAuth = FirebaseAuth.getInstance();
        appCompatButtonLogin = findViewById(R.id.materialButtonLogin);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        inputValidation = new InputValidation(activity);
        appCompatButtonLogin.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        HomeActivity(currentUser);
    }

    @Override
    public void onClick(View v) {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, "Enter Valid Email")) {
            return;
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, "Enter Valid Email")) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, "Enter Password")) {
            return;
        }
        mAuth.signInWithEmailAndPassword(textInputEditTextEmail.getText().toString().trim()
                        , textInputEditTextPassword.getText().toString().trim())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        HomeActivity(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed. Make sure you are connected to the internet and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void HomeActivity(FirebaseUser user) {
        if (user != null) {
            Intent homeIntent = new Intent(activity, HomeActivity.class);
            homeIntent.putExtra("UserID", user.getUid());
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            startActivity(homeIntent);
        }

    }

}
