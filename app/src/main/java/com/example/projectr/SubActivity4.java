package com.example.projectr;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class SubActivity4 extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.winb);
    }
}