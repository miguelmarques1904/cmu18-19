package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AddUserActivity extends AppCompatActivity {

    TextView title;


    LinearLayout userAddListLayout;
    LinearLayout userOwnListLayout;


    Button addBtn;
    Button cancelBtn;

    //Temporary Userlist
    List<String> userList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adduser);

        String albumName = getIntent().getStringExtra("albumName");
        title = (TextView) findViewById(R.id.adduser_title);
        title.setText("Managing Users of \"" + albumName + "\" album.");

        userAddListLayout = (LinearLayout) findViewById(R.id.adduser_canaddll);
        userOwnListLayout = (LinearLayout) findViewById(R.id.adduser_cantaddll);


        addBtn = (Button) findViewById(R.id.adduser_add);
        cancelBtn = (Button) findViewById(R.id.adduser_cancel);

        // TODO request userlist
        // temporary implementation
        for(int i = 1; i<=20; i++) {
            userList.add("username" + i);
        }

        for(int i = 0; i < userList.size(); i++) {
            // TODO if user already in ownership. if it is add to useradd_cantaddll as a textview
            if(i%2==0) { // replace by ownership function call
                TextView tv = new TextView(getApplicationContext());
                tv.setText(userList.get(i));
                userOwnListLayout.addView(tv);
            }
            else {
                CheckBox cb = new CheckBox(getApplicationContext());
                cb.setText(userList.get(i));
                userAddListLayout.addView(cb);
            }
        }

    }

    public void addUserClick(View v) {
        finish();
    }

    public void backOnClick(View v) {
        finish();
    }


}
