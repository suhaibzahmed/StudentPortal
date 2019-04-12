package com.example.android.studentportal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail,UserPassword,UserConfirmPassword;
    private Button CreateAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    Context context = RegisterActivity.this;
    CharSequence text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        UserConfirmPassword= (EditText) findViewById(R.id.register_confirmpassword);
        CreateAccount = (Button) findViewById(R.id.register_create);

        CreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
        sendUserToMainActivity();
    }}

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(context,text = "Enter Your Email ",Toast.LENGTH_SHORT).show();
        }
       else if(TextUtils.isEmpty(password)){
            Toast.makeText(context,text = "Please Enter Your Password",Toast.LENGTH_SHORT).show();
        }
       else if(TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(context,text = "Please Confirm Your Password",Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirmPassword))
        {
            Toast.makeText(this, "your password does not match with your confirm password...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Please wait, Your account is being created");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        sendUserToSetupActivity();
                        Toast.makeText(context,text = "Account Created Successfully",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(context,text= "Error Occured" +message,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
