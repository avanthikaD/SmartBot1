package com.softwareproject.SmartBot;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    EditText mTextUsername;
    TextInputEditText mTextPassword;
    TextInputEditText mTextCnfPassword;
    Button mButtonRegister;
    TextView mTextViewLogin;
    DataBaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.setTitle("Register");
        db = new DataBaseHelper(this);
        mTextUsername = (EditText) findViewById(R.id.editText_username);
        mTextCnfPassword =(TextInputEditText) findViewById(R.id.editText_cnf_password);
        mTextPassword = (TextInputEditText) findViewById(R.id.editText_password);
        mButtonRegister = (Button) findViewById(R.id.button_register);
        mTextViewLogin = (TextView) findViewById(R.id.textView_login );
        mTextViewLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent LoginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(LoginIntent);
            }
        });
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = mTextUsername.getText().toString().trim();
                String pwd =mTextPassword.getText().toString().trim();
                String cnf_pwd = mTextCnfPassword.getText().toString().trim();
                Boolean res = db.CheckUser(user, pwd);
                long val = db.addUser(user, pwd);

                if(user.isEmpty())
                    Toast.makeText(getApplicationContext(),"Please enter Username",Toast.LENGTH_SHORT).show();

                else if(pwd.isEmpty())
                    Toast.makeText(getApplicationContext(),"Please enter Password",Toast.LENGTH_SHORT).show();

                else if(pwd.length()<6)
                    Toast.makeText(getApplicationContext(),"Password must contain 6  characters",Toast.LENGTH_SHORT).show();

                else if(cnf_pwd.isEmpty())
                    Toast.makeText(getApplicationContext(),"Please confirm Password",Toast.LENGTH_SHORT).show();

                if((!user.isEmpty())&&(!pwd.isEmpty())&&(!cnf_pwd.isEmpty())&&(pwd.length()>=6)&&(pwd.equals(cnf_pwd))){

                    if (res){
                        Toast.makeText(getApplicationContext(),"Already a registered user\nplease login",Toast.LENGTH_SHORT).show();
                    }
                    else {

                        if (val > 0) {
                            Toast.makeText(RegisterActivity.this, "You have registered successfully please login", Toast.LENGTH_SHORT).show();
                            Intent moveToLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(moveToLogin);
                        }
                    }

                }
                else if((!user.isEmpty())&&(!pwd.isEmpty())&&(!cnf_pwd.isEmpty())&&(pwd.length()>=6)&&(!pwd.equals(cnf_pwd))){
                    Toast.makeText(RegisterActivity.this, "Passwords not matched", Toast.LENGTH_SHORT).show();
                }
            }
            });
    }
}
