package com.wpt.hook.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by wpt on 17/6/26.
 */

public class TargetActvitity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("TargetActvitity");
        setContentView(textView);
        getApplicationContext().getPackageName();
    }
}
