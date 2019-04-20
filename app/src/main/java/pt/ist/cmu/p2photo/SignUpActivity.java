package pt.ist.cmu.p2photo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {

    EditText usernameField;
    EditText passwordField;
    Button signUp;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameField = (EditText) findViewById(R.id.signup_username);
        passwordField = (EditText) findViewById(R.id.signup_password);

        signUp = (Button) findViewById(R.id.signup_signup);
        cancel = (Button) findViewById(R.id.signup_cancel);

    }

    // Dialog_signin login button
    public void signUpOnClick(View v) {

        finish();
    }

    public void signUpCancelOnClick(View v) {

        finish();
    }


}
