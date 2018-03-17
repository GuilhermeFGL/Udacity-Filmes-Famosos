package com.guilhermefgl.peliculas.helpers;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.guilhermefgl.peliculas.R;

public final class SnackBarHelper {

    private SnackBarHelper() {}

    public static Snackbar make(Context context, View parentView, int messageResource, int durationLength) {
        Snackbar snackbar = Snackbar.make(parentView, messageResource, durationLength);
        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text))
                .setTextColor(context.getResources().getColor(R.color.primary_light));
        return snackbar;
    }
}
