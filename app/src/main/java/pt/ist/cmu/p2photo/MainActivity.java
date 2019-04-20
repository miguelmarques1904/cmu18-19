package pt.ist.cmu.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void signUpOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }

    public void logInOnClick(View v) {
        Button button=(Button) v;
        if(loggedIn) {
            button.setText("Log In");
        }
        else {
            button.setText("Log Out");
            setContentView(R.layout.dialog_signin);
        }

        //loggedIn = !loggedIn; do this in the dialog layout button click if checks credentials

        setContentView(R.layout.dialog_signin);
    }

    public void createAlbumOnClick(View v) {
        if(loggedIn) {

        }
    }

    public void findUserOnClick(View v) {
        if(loggedIn) {

        }
    }

    public void addPhotoOnClick(View v) {
        if(loggedIn) {

        }
    }

    public void addUserOnClick(View v) {
        if(loggedIn) {

        }
    }


    public void listUserOnClick(View v) {
        if(loggedIn) {

        }
    }

    public void viewAlbumOnClick(View v) {
        if(loggedIn) {

        }
    }

}
