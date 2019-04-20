package pt.ist.cmu.p2photo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateAlbumActivity extends AppCompatActivity {

    EditText albumNameField;
    Button createBtn;
    Button cancelBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createalbum);

        albumNameField = (EditText) findViewById(R.id.createalbum_albumName);

        createBtn = (Button) findViewById(R.id.createalbum_create);
        cancelBtn = (Button) findViewById(R.id.createalbum_cancel);
    }

    public void CreateOnClick(View v) {

        // TODO contact server to create Album and check if already exists with that name
        // static values for now
        if(!albumNameField.getText().toString().isEmpty() && !albumNameField.getText().toString().equals("album")) {
            finish();
        }
    }

    public void CreateAlbumCancelOnClick(View v) {

        finish();
    }

}
