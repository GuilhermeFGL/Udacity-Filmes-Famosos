package com.guilhermefgl.peliculas.helpers;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.guilhermefgl.peliculas.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class PicassoHelper {

    private PicassoHelper() { }

    public static void loadImage(Context context, String url, final ImageView imageView) {
        Picasso.with(context)
                .load(url)
                .placeholder(R.mipmap.movie_background)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onError() {
                        imageView.setImageResource(R.mipmap.error_background);
                    }
                });
    }
}
