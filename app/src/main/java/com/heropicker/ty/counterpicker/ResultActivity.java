package com.heropicker.ty.counterpicker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    static LinkedList<List> fullRatings = new LinkedList<>();
    LinkedList<List> ratings = new LinkedList<>();
    static ArrayList<ImageButton> images = null;
    LinkedList<List> newRatings = new LinkedList<>(); //Copy of ratings so that methods are constructive
    List hero;

    RelativeLayout layoutView;
    LayoutInflater inflater;
    RelativeLayout.LayoutParams params;
    RelativeLayout overlayChild;
    RelativeLayout heroChild;
    TranslateAnimation translate;     //For translating the overlay into the screen
    TranslateAnimation nextTranslate; //For translating the overlay out of screen
    int padding;
    String backgroundColor = "grey";
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutView = (RelativeLayout) inflater.inflate(R.layout.activity_result, null); //Inflate activity_end.xml and uses it as parent
        setContentView(layoutView); //Sets the inflated activity_end as the view the user sees
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        padding = getResources().getDimensionPixelOffset(R.dimen.activity_result_padding); //Used to set padding for layoutView

        ArrayList<String> tags = Utilities.extractTags(images);
        for(int i = 0; i < 12; i ++) {
            ratings.add(fullRatings.get(i));
        }
        for(List lst : ratings) {
            lst.set(0,Utilities.unFormatHero((String)lst.get(0), tags));
        }
        addOverlay();
    }



    public void addOverlay(){
        overlayChild = (RelativeLayout)inflater.inflate(R.layout.result_overlay, layoutView, false); //Element to be added onto parent
        overlayChild.setVisibility(View.GONE);
        overlayChild.setId(View.generateViewId());
        layoutView.addView(overlayChild, params); //set layout params

        translate = new TranslateAnimation(0,0,-200,0);
        translate.setDuration(800); translate.setFillAfter(true);
        translate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                overlayChild.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                overlayChild.startAnimation(nextTranslate);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        }); //Sets visible on start and also triggers the method to add the selected hero views
        nextTranslate = new TranslateAnimation(0,0,0,-200); //Translate for when the overlay exits
        nextTranslate.setDuration(800);
        nextTranslate.setFillAfter(true); nextTranslate.setStartOffset(500);
        nextTranslate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutView.setPadding(padding,0,padding,0);
                addCounterHeroes();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        overlayChild.startAnimation(translate);
    }

    //Organizes the presentation of best heroes for the user to pick
    public void addCounterHeroes() {
        overlayChild = (RelativeLayout)inflater.inflate(R.layout.result_overlay_next, layoutView, false); //Element to be added onto parent
        overlayChild.setVisibility(View.GONE); overlayChild.setId(View.generateViewId());
        layoutView.addView(overlayChild, params); //set layout params
        AlphaAnimation alpha = new AlphaAnimation(0f, 1.0f);  alpha.setDuration(700); alpha.setFillAfter(true);
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                overlayChild.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                addCounterHeroesB(ratings, overlayChild); //OverlayChild is set as parent because this is the parent for the first hero

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        overlayChild.startAnimation(alpha);
        //After overlayChild, the parents will be the previously added heroes
    }
    //Tail-recursive auxilary function
    public void addCounterHeroesB(LinkedList<List> heroes, RelativeLayout heroParent) { //heroParent represents the individual hero's parent
        newRatings = heroes;
        if(counter == 11) { //BASE CASE FOR EXIT SCENARIO
            return;
        }
        hero = heroes.get(0);

        heroChild = (RelativeLayout)inflater.inflate(R.layout.result_item_hero, layoutView, false); //Inflating view based off of end_item_hero layout, which represents the previously selected heroes
        RelativeLayout.LayoutParams heroParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); //Initializing parameters for selected heroes
        heroChild.setLayoutParams(heroParams);
        heroChild.setId(View.generateViewId()); //Generating id so it will work with relative layout parameters
        heroParams.addRule(RelativeLayout.BELOW, heroParent.getId()); heroParams.addRule(RelativeLayout.CENTER_HORIZONTAL); heroParams.setMargins(0, 18, 0, 0); //Adding parameters to the layour
        heroChild.setVisibility(View.INVISIBLE);
        if(backgroundColor.equals("grey")) {
            heroChild.setBackgroundColor(getResources().getColor(R.color.lightColorBackground));
            backgroundColor = "other";
        } else {
            heroChild.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            backgroundColor = "grey";
        }

        //------------------- Setting properties for the specific end_item_hero.xml for this hero--------------------//
        System.out.println("CURRENT HERO TAG " + ((String)hero.get(0)));
        ImageButton heroButton = matchTags(Utilities.unModifyHeroName((String)hero.get(0))); //Finds the image button for the current hero
        ImageView image = (ImageView)heroChild.findViewById(R.id.heroImage);
        Drawable imageDrawable = heroButton.getDrawable(); imageDrawable.clearColorFilter();
        image.setImageDrawable(imageDrawable); //Setting image for hero

        TextView text = (TextView) heroChild.findViewById(R.id.heroText); //Setting text for hero
        String formatText = Utilities.formatHero(heroButton.getTag().toString()); //Removes underscore and capitalizes first letters
        text.setText(formatText);

        TextView percentage = (TextView) heroChild.findViewById(R.id.heroPercentage); // Setting percentage text
        Double removeNegative = 0.0 - (Double)hero.get(1);
        String formatPercentage = new DecimalFormat("#.##").format(removeNegative);
        percentage.setText(formatPercentage);
        layoutView.addView(heroChild);
        //-----------------------------------------------------------------------------------------------------------//

        AlphaAnimation alpha = new AlphaAnimation(0f, 1.0f);  alpha.setDuration(300); alpha.setFillAfter(true);
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                newRatings.remove(hero);
                counter++;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                addCounterHeroesB(newRatings, heroChild);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        }); //RECURSIVE CALL INSIDE LISTENER
        heroChild.startAnimation(alpha);


    }

    public ImageButton matchTags(String hero) { //Used to find the the associated image button so that the appropriate hero image is used when showing the user what heroes were selected
        for (ImageButton button : images) { //Selected heroes is a list of currently pressed heroes
            if (hero.equals((String) button.getTag())) { //If the pressed button is already faded out,
                return button;
            }
        }
        return null;
    }
}
