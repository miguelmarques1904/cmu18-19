package pt.ist.cmu.p2photo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SignInActivity extends AppCompatActivity {

    Button loginBtn;
    Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        loginBtn = (Button) findViewById(R.id.login_login);
        cancelBtn = (Button) findViewById(R.id.login_cancel);

    }

    // Dialog_signin login button
    public void logInOnClick(View v) {

        //TODO check credentials
        MainActivity.loggedIn = true;

        finish();
    }

    public void logInCancelOnClick(View v) {

        finish();
    }


}
