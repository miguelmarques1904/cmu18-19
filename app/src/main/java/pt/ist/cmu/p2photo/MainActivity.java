package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //TODO change to false. it's true for testing purposes.
    public static boolean loggedIn = true;

    Button signUp;
    Button login;
    Button createAlbum;
    Button viewAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUp = (Button) findViewById(R.id.main_signup);
        login = (Button) findViewById(R.id.main_login);
        createAlbum = (Button) findViewById(R.id.main_createAlbum);
        viewAlbum = (Button) findViewById(R.id.main_viewAlbum);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if(loggedIn) {
            loggedInView();
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
