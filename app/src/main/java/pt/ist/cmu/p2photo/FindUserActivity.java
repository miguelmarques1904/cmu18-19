package pt.ist.cmu.p2photo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class FindUserActivity extends AppCompatActivity {

    LinearLayout userListLayout;

    Button findBtn;
    Button cancelBtn;

    //Temporary Userlist
    List<String> userList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finduser);

        userListLayout = (LinearLayout) findViewById(R.id.finduser_vertical_layout);


        findBtn = (Button) findViewById(R.id.adduser_add);
        cancelBtn = (Button) findViewById(R.id.adduser_cancel);

        // TODO request userlist
        // temporary implementation
        for(int i = 1; i<=20; i++) {
            userList.add("username" + i);
        }

        for(int i = 0; i < userList.size(); i++) {
            CheckBox cb = new CheckBox(getApplicationContext());
            cb.setText(userList.get(i));
            userListLayout.addView(cb);
        }


    }


    public void selectOnClick(View v) {

        // TODO contact server to check username
        // static values for now

        finish();
    }


    public void cancelOnClick(View v) {

        finish();
    }

}
