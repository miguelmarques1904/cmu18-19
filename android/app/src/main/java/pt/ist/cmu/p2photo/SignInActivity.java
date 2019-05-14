package pt.ist.cmu.p2photo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dropbox.core.android.Auth;
import com.orhanobut.hawk.Hawk;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.cloud.DropboxActivity;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends DropboxActivity {

    private String username;
    private String password;

    EditText usernameField;
    EditText passwordField;
    TextView message;

    Button loginBtn;
    Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        usernameField = findViewById(R.id.signin_username);
        passwordField = findViewById(R.id.signin_password);
        message = findViewById(R.id.signin_message);

        loginBtn = findViewById(R.id.login_login);
        cancelBtn = findViewById(R.id.login_cancel);
    }

    /*
     *  POST users/login
     *
     *  Login action button
     */

    public void logInOnClick(View v) {

        message.setTextColor(Color.rgb(194, 38, 38));

        username = usernameField.getText().toString();
        password = passwordField.getText().toString();

        if (!username.isEmpty() && !password.isEmpty() && !MainActivity.loggedIn) {

            ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
            Call<User> call = service.loginUser(username, password);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    switch (response.code()) {
                        case 200:
                            message.setTextColor(Color.rgb(0, 133, 119));
                            MainActivity.loggedIn = true;

                            // get user
                            User user = response.body();
                            user.setUsername(username);

                            // save user on shared preferences (hawk)
                            Hawk.put(Constants.CURRENT_USER_KEY, user);

                            if (!hasToken()) {
                                message.setText("You are logged in. You will be redirected to Dropbox to login.");
                                new java.util.Timer().schedule( new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            Auth.startOAuth2Authentication(SignInActivity.this, Constants.DROPBOX_APP_KEY);
                                        }
                                    }, 2000
                                );
                            } else {
                                message.setText("You are logged in. You already signed in on Dropbox.");
                                loginBtn.setEnabled(false);
                            }

                            break;
                        case 400:
                            message.setText("Username and/or password are invalid.");
                            break;
                        case 401:
                            message.setText("Credentials are invalid.");
                            break;
                        default:
                            message.setText("Something went wrong...");
                            break;
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    message.setText("Something went wrong... Network may be down...");
                }
            });

        } else if (MainActivity.loggedIn) {
            message.setText("You are already logged in.");
        } else {
            message.setText("Username and/or password are empty.");
        }
    }

    /*
     *  Function called onResume()
     *  Check out DropboxActivity
     */

    @Override
    protected void loadData() {
        if (MainActivity.loggedIn) {
            message.setText("Login to Dropbox was successful.");
            loginBtn.setEnabled(false);
        }
    }

    /*
     *  Activity change function
     */

    public void logInCancelOnClick(View v) {
        finish();
    }
}