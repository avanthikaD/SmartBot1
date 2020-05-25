package com.softwareproject.SmartBot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    EditText mTextUsername;
    TextInputEditText mTextPassword;
    Button mButtonLogin;
    CheckBox remember;
    TextView mTextViewRegister;
    DataBaseHelper db;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle("Login");
        db = new DataBaseHelper(this);
        mTextUsername = (EditText) findViewById(R.id.editText_username);
        mTextPassword = (TextInputEditText) findViewById(R.id.editText_password);
        mButtonLogin = (Button) findViewById(R.id.button_login);
        remember = findViewById(R.id.rememberMe);
        mTextViewRegister = (TextView) findViewById(R.id.textView_register);

        SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
        String checkbox = preferences.getString("remember","");
        if(checkbox.equals("true")){
            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
            startActivity(intent);
        }else if (checkbox.equals("false")){
            Toast.makeText(this,"please sign in",Toast.LENGTH_SHORT).show();
        }


        mTextViewRegister.setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick(View view){
                Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
        mButtonLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String user = mTextUsername.getText().toString().trim();
                String pwd = mTextPassword.getText().toString().trim();
                Boolean res = db.CheckUser(user, pwd);
                if(user.isEmpty())
                    Toast.makeText(getApplicationContext(),"Please enter Username",Toast.LENGTH_SHORT).show();

                else if(pwd.isEmpty())
                    Toast.makeText(getApplicationContext(),"Please enter Password",Toast.LENGTH_SHORT).show();

                else if((!user.isEmpty())&&(!pwd.isEmpty())&&((res == true))){
                    Toast.makeText(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
                    Intent HomePage = new Intent(LoginActivity.this,HomeActivity.class);
                    startActivity(HomePage);
                    mTextUsername.setText("");
                    mTextPassword.setText("");
                }

                else if((!user.isEmpty())&&(!pwd.isEmpty())&&((res == false))){
                    Toast.makeText(LoginActivity.this, "Login Error, please check your details", Toast.LENGTH_SHORT).show();
                }

            }
        });

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if(buttonView.isChecked()){

                     SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
                     SharedPreferences.Editor editor = preferences.edit();
                     editor.putString("remember","true");
                     editor.apply();
                     Toast.makeText(LoginActivity.this,"Checked",Toast.LENGTH_SHORT).show();

                 }else if(!buttonView.isChecked()){

                     SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
                     SharedPreferences.Editor editor = preferences.edit();
                     editor.putString("remember","false");
                     editor.apply();
                     Toast.makeText(LoginActivity.this,"Unchecked",Toast.LENGTH_SHORT).show();
                 }
            }
        });

    }
}
