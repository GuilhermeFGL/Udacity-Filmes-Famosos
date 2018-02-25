package com.guilhermefgl.peliculas.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.guilhermefgl.peliculas.R;

import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

}
