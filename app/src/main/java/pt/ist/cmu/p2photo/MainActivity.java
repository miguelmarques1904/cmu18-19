package pt.ist.cmu.p2photo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //TODO change to false. it's true for testing purposes.
    public static boolean loggedIn = false;
    static int MODE_SELECTION = 1;
    static int MODE_CLOUD = 1;
    static int MODE_WIFI_DIRECT = 2;

    int mode;


    Button signUp;
    Button login;
    Button createAlbum;
    Button viewAlbum;

    Intent modeSelectionIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUp = (Button) findViewById(R.id.main_signup);
        login = (Button) findViewById(R.id.main_login);
        createAlbum = (Button) findViewById(R.id.main_createAlbum);
        viewAlbum = (Button) findViewById(R.id.main_viewAlbum);


        modeSelectionIntent = new Intent(MainActivity.this, ModeSelectionActivity.class);
        startActivityForResult(modeSelectionIntent, MODE_SELECTION);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if(loggedIn) {
            loggedInView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if(requestCode == MODE_SELECTION) {
            if(resultCode == Activity.RESULT_OK) {
                int modeRes = data.getIntExtra("mode",-1);

                if(modeRes == MODE_CLOUD || modeRes == MODE_WIFI_DIRECT)
                    mode = modeRes;
            }
            else {
                startActivityForResult(modeSelectionIntent, MODE_SELECTION);
            }
        }
    }


    public void signUpOnClick(View v) {
        Intent signUpIntent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(signUpIntent);
    }

    public void logInMainOnClick(View v) {

        if(loggedIn) {
            loggedIn = false;
            loggedOutView();
        }
        else {
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
