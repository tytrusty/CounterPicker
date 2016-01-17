package com.heropicker.ty.counterpicker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;

/**
 * Menu screen activity. Pretty ugly, so I may ATTEMPT to fix it.
 *
 * @author Ty Trusty
 * @version 1/16/16
 */
public class StarterActivity extends AppCompatActivity {
    Button myButton;
    AlphaAnimation fastFadeOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        fastFadeOut = new AlphaAnimation(1.0f, 0.5f); fastFadeOut.setDuration(200); fastFadeOut.setFillAfter(false);
        fastFadeOut.setRepeatMode(Animation.REVERSE); fastFadeOut.setRepeatCount(1);
        fastFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        myButton = (Button) findViewById(R.id.starter);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myButton.startAnimation(fastFadeOut);
                
            }
        });
    }
}
