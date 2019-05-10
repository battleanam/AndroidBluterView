package com.hanayue.ayuemobieview.notice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hanayue.ayuemobieview.R;

public class NoticeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        Intent intent = new Intent();
        intent.putExtra("res", R.id.nav_notice);
        setResult(2, intent);
    }
}
