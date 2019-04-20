package pt.ist.cmu.p2photo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignInActivity extends AppCompatActivity {

    EditText usernameField;
    EditText passwordField;
    Button loginBtn;
    Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        usernameField = (EditText) findViewById(R.id.signin_username);
        passwordField = (EditText) findViewById(R.id.signin_password);


        loginBtn = (Button) findViewById(R.id.login_login);
        cancelBtn = (Button) findViewById(R.id.login_cancel);

    }

    // Dialog_signin login button
    public void logInOnClick(View v) {

        // TODO contact server to check values
        // static values for now
        if(usernameField.getText().toString().equals("username") && passwordField.getText().toString().equals("password")) {
            MainActivity.loggedIn = true;
            finish();
        }
    }

    public void logInCancelOnClick(View v) {

        finish();
    }


}
