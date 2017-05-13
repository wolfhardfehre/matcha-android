package com.nicefontaine.matcha.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.provider.Settings;

import com.nicefontaine.matcha.R;

import javax.inject.Singleton;


@Singleton
public final class DialogManager {

    public static void showEnableGPSDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.dialog_no_location_message).setCancelable(true)
                .setPositiveButton(R.string.dialog_ok, (dialog, id) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    dialog.dismiss();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
