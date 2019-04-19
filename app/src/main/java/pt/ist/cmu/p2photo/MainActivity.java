package pt.ist.cmu.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void signUpButtonOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }

    public void logInButtonOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }

    public void createAlbumButtonOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }

    public void findUserButtonOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }

    public void addPhotoButtonOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }

    public void addUserButtonOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }


    public void listUserButtonOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }

    public void viewAlbumButtonOnClick(View v) {
        Button button=(Button) v;
        button.setText("top");
    }

}
