package com.guilhermefgl.peliculas.helpers;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public final class PicassoHelper {

    private PicassoHelper() { }

    public static void loadImage(Context context, String url, final ImageView imageView,
                                 final int placeholderResource, final int errorResource) {
        Picasso.with(context)
                .load(url)
                .placeholder(placeholderResource)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onError() {
                        imageView.setImageResource(errorResource);
                    }
                });
    }
}
