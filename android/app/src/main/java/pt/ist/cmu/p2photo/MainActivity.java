package pt.ist.cmu.p2photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    protected static final int MODE_CLOUD = 1;
    protected static final int MODE_WIFI_DIRECT = 2;

    public static boolean loggedIn = false;

    private int mode;

    Button signUp;
    Button login;
    Button createAlbum;
    Button viewAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUp = findViewById(R.id.main_signup);
        login = findViewById(R.id.main_login);
        createAlbum = findViewById(R.id.main_createAlbum);
        viewAlbum = findViewById(R.id.main_viewAlbum);

        this.mode = getIntent().getIntExtra("mode", 1);

        // initialize hawk
        Hawk.init(getApplicationContext()).build();

        // check if logged in already (preferences)
        if (Hawk.contains(Constants.CURRENT_USER_KEY)) {
            loggedIn = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (loggedIn) {
            loggedInView();
        }
    }

    public void signUpOnClick(View v) {
        Intent signUpIntent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(signUpIntent);
    }

    public void logInMainOnClick(View v) {
        if (loggedIn) {

            // Get token
            User user = Hawk.get(Constants.CURRENT_USER_KEY);
            String token = "Token " + user.getToken();

            // Call logout
            ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
            Call<Void> call = service.logoutUser(token);

            call.enqueue(new Callback<Void>() {
                Toast toast;

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    switch (response.code()) {
                        case 200:
                            toast = Toast.makeText(getApplicationContext(), "Logged Out Successfully", Toast.LENGTH_SHORT);
                            break;
                        case 401:
                            toast = Toast.makeText(getApplicationContext(), "You were not logged in on the server", Toast.LENGTH_SHORT);
                            break;
                        default:
                            toast = Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT);
                            break;
                    }

                    toast.show();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT).show();
                }
            });

            // Remove user and album from shared preferences
            Hawk.deleteAll();

            loggedIn = false;
            loggedOutView();
        } else {
            Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(signInIntent);
        }

    }

    public void createAlbumOnClick(View v) {
        Intent createAlbumIntent = new Intent(MainActivity.this, CreateAlbumActivity.class);
        startActivity(createAlbumIntent);
    }


    public void viewAlbumOnClick(View v) {
        Intent viewAlbumIntent = new Intent(MainActivity.this, ViewAlbumActivity.class);
        startActivity(viewAlbumIntent);
    }

    public void loggedInView() {
        login.setText("Log Out");
        signUp.setVisibility(View.GONE);
        createAlbum.setVisibility(View.VISIBLE);
        viewAlbum.setVisibility(View.VISIBLE);
    }

    public void loggedOutView() {
        login.setText("Log In");
        signUp.setVisibility(View.VISIBLE);
        createAlbum.setVisibility(View.GONE);
        viewAlbum.setVisibility(View.GONE);
    }
}
