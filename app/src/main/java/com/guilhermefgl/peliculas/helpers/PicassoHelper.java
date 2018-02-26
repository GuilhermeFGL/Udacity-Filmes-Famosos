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

    public static void loadImage(Context context, String url, final ImageView image) {
        loadImage(context, url, image, null);
    }

    public static void loadImage(Context context, String url,
                                 final ImageView imageView, final ProgressBar progressBar) {
        Picasso.with(context)
                .load(url)
                .placeholder(R.mipmap.movie_background)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError() {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        imageView.setImageResource(R.mipmap.error_background);
                    }
                });
    }
}
