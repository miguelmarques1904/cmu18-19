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
    Button findUser;
    Button addPhoto;
    Button addUser;
    Button listUser;
    Button viewAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUp = (Button) findViewById(R.id.signup);
        login = (Button) findViewById(R.id.login);
        createAlbum = (Button) findViewById(R.id.createAlbum);
        findUser = (Button) findViewById(R.id.findUser);
        addPhoto = (Button) findViewById(R.id.addPhoto);
        addUser = (Button) findViewById(R.id.addUser);
        listUser = (Button) findViewById(R.id.listUser);
        viewAlbum = (Button) findViewById(R.id.viewAlbum);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if(loggedIn) {
            login.setText("Log Out");
            loggedInView();
        }
    }

    public void signUpOnClick(View v) {
        if(loggedIn) {
            loggedIn = false;
            login.setText("Log In");

        }
        else {
            Intent signUpIntent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(signUpIntent);
        }
    }

    public void logInMainOnClick(View v) {

        if(loggedIn) {
            loggedIn = false;
            login.setText("Log In");
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

    public void findUserOnClick(View v) {
        Intent findUserIntent = new Intent(MainActivity.this, FindUserActivity.class);
        startActivity(findUserIntent);
    }

    public void addPhotoOnClick(View v) {
        Intent addPhotoIntent = new Intent(MainActivity.this, AddPhotoActivity.class);
        startActivity(addPhotoIntent);
    }

    public void addUserOnClick(View v) {
        Intent addUserIntent = new Intent(MainActivity.this, AddUserActivity.class);
        startActivity(addUserIntent);
    }


    public void listUserOnClick(View v) {
        Intent listUserIntent = new Intent(MainActivity.this, ListUserActivity.class);
        startActivity(listUserIntent);
    }

    public void viewAlbumOnClick(View v) {
        Intent viewAlbumIntent = new Intent(MainActivity.this, ViewAlbumActivity.class);
        startActivity(viewAlbumIntent);
    }

    public void loggedInView() {
        signUp.setVisibility(View.GONE);
        createAlbum.setVisibility(View.VISIBLE);
        findUser.setVisibility(View.VISIBLE);
        addPhoto.setVisibility(View.VISIBLE);
        addUser.setVisibility(View.VISIBLE);
        listUser.setVisibility(View.VISIBLE);
        viewAlbum.setVisibility(View.VISIBLE);
    }

    public void loggedOutView() {
        signUp.setVisibility(View.VISIBLE);
        createAlbum.setVisibility(View.GONE);
        findUser.setVisibility(View.GONE);
        addPhoto.setVisibility(View.GONE);
        addUser.setVisibility(View.GONE);
        listUser.setVisibility(View.GONE);
        viewAlbum.setVisibility(View.GONE);
    }

}
