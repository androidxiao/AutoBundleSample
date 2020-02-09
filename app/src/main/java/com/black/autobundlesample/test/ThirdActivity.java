package com.black.autobundlesample.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.black.autobundlesample.R;
import com.black.lib_annotation.AutoBundle;

import androidx.appcompat.app.AppCompatActivity;


public class ThirdActivity extends AppCompatActivity {


    @AutoBundle
    public int no;
    @AutoBundle
    public String address;
    @AutoBundle
    public boolean isChoose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView tvMsg1 = findViewById(R.id.tv_test1);

        tvMsg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                setResult(RESULT_OK, intent1);
                finish();
            }
        });
    }
}
