package pt.ist.cmu.p2photo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SignUpActivity extends AppCompatActivity {

    Button signUp;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signUp = (Button) findViewById(R.id.signup_signup);
        cancel = (Button) findViewById(R.id.signup_cancel);

    }

    // Dialog_signin login button
    public void signUpOnClick(View v) {

        finish();
    }

    public void logInCancelOnClick(View v) {

        finish();
    }


}
