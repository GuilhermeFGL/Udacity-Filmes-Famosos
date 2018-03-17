package com.guilhermefgl.peliculas.views.splash;


import android.os.Bundle;

import com.guilhermefgl.peliculas.views.main.MainActivity;
import com.guilhermefgl.peliculas.views.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.startActivity(this);
    }

}
