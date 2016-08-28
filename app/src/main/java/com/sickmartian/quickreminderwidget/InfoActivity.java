package com.sickmartian.quickreminderwidget;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by ***REMOVED*** on 8/15/16.
 */
public class InfoActivity extends AppCompatActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_activity);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set toolbar and navigation
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.info_activity_label);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView feedback = (TextView) findViewById(R.id.information_feedback_line);
        feedback.setMovementMethod(LinkMovementMethod.getInstance());
        TextView privacy = (TextView) findViewById(R.id.information_privacy_line);
        privacy.setMovementMethod(LinkMovementMethod.getInstance());
        TextView icons = (TextView) findViewById(R.id.information_icons_line);
        icons.setMovementMethod(LinkMovementMethod.getInstance());
        TextView icons2 = (TextView) findViewById(R.id.information_icons_line2);
        icons2.setMovementMethod(LinkMovementMethod.getInstance());
        TextView icons3 = (TextView) findViewById(R.id.information_icons_line3);
        icons3.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static Intent getIntentForShow() {
        Intent startActivity = new Intent(App.getAppContext(), InfoActivity.class);
        return startActivity;
    }

}
