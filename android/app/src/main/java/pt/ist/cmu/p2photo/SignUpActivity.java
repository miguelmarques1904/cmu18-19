package pt.ist.cmu.p2photo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private String username;
    private String password;

    EditText usernameField;
    EditText passwordField;
    TextView message;

    Button signUp;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameField = (EditText) findViewById(R.id.signup_username);
        passwordField = (EditText) findViewById(R.id.signup_password);
        message = (TextView) findViewById(R.id.signup_message);

        signUp = (Button) findViewById(R.id.signup_signup);
        cancel = (Button) findViewById(R.id.signup_cancel);
    }

    // Dialog_sign in login button
    public void signUpOnClick(View v) {

        message.setTextColor(Color.rgb(194, 38, 38));

        username = usernameField.getText().toString();
        password = passwordField.getText().toString();

        if (!username.isEmpty() && !password.isEmpty()) {

            ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
            Call<Void> call = service.registerUser(username, password);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    switch (response.code()) {
                        case 201:
                            message.setTextColor(Color.rgb(0, 133, 119));
                            message.setText("User '" + username + "' was created successfully.");
                            break;
                        case 400:
                            message.setText("Username and/or password are invalid.");
                            break;
                        case 409:
                            message.setText("Username already exists.");
                            break;
                        case 500:
                            message.setText("Something went wrong on the server side.");
                            break;
                        default:
                            message.setText("Something went wrong...");
                            break;
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    message.setText("Something went wrong... Server may be down..." + t.toString());
                }
            });

        } else {
            message.setText("Username and/or password are empty.");
        }
    }

    public void signUpCancelOnClick(View v) {
        // exit view
        finish();
    }

}
