package com.heropicker.ty.counterpicker;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * This activity follows MainActivity and sends the user to ResultActivity, so it is the 3rd activity
 * Class that handles data extraction and processing in the background. The user is shown which heroes were selected.
 *
 * @author Ty Trusty
 * @version 1/16/16
 */
public class MiddleActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    public static ArrayList<ImageButton> myButtonImages = null;
    ArrayList<String> selectedHeroes = new ArrayList<>();
    LinkedList<List> ratings = new LinkedList<>();
    LayoutInflater inflater;
    RelativeLayout layoutView;
    RelativeLayout overlayChild;
    RelativeLayout.LayoutParams params;
    IntListener intState; boolean doneProcessing = false;
    boolean isDataOld = false; //If data is old, then obtain new from online. false is default state ... method will change to true if it is older than a week
    //USED IN addSelectedHeroes ANIMATIONS
    ArrayList<String> newHeroes;
    RelativeLayout heroChild;
    String hero;
    //Used when reading and writing files
    String MY_FILE_NAME = "hero_data.txt";
    File file;
    FileOutputStream fileOut;
    FileInputStream fileIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedHeroes = getIntent().getStringArrayListExtra("heroes"); //Extracting intent data
        selectedHeroes.remove(0); //Removing 0 because 0 is a sentinel value

        //Shared preferences is used to determine whether to access the internet or locally stored data
        //Initializing file input and output stream for data access
        try {
            initializeFiles();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("FILE " + MY_FILE_NAME + " NOT FOUND ");
        }
        sharedPreferences();

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutView = (RelativeLayout) inflater.inflate(R.layout.activity_end,null); //Inflate activity_end.xml and uses it as parent
        setContentView(layoutView); //Sets the inflated activity_end as the view the user sees
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        getSupportActionBar().hide();
        //-----------------METHODS THAT DO THE MAIN WORK-------WORKING SIMULTANEOUSLY ------------------------------//
        runAsyncTask(); //Works in the background calculating the counters                                          //
        addOverlay(selectedHeroes);  //Adds the overlay then calls the method to add the selected hero views        //
        //----------------------------------------------------------------------------------------------------------//

        //Listener so that the user can skip the animations by clicking
        layoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(doneProcessing)
                    intState.set(2);
            }
        });

        //--------------THIS IS THE TRIGGER TO MOVE TO RESULT ACTIVITY-------*TRIGGERED WHEN ABOVE PROCESSES FINISH*----------------------------//
        intState = new IntListener();                                                                                                           //
        intState.setOnChangeListener(new LoadListener() {                                                                                       //
            @Override                                                                                                                           //
            public void onChange(int load) {                                                                                                //
                if(intState.get() == 2) { //IF IT IS 2 THEN BOTH the calculation and animations are done                                        //
                    Intent i = new Intent(getApplicationContext(),ResultActivity.class);                                                        //
                    ResultActivity.fullRatings = ratings;                                                                                       //
                    ResultActivity.images = myButtonImages;                                                                                     //
                    startActivity(i, ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.nothing).toBundle());  //
                }                                                                                                                               //
            }                                                                                                                                   //
        });                                                                                                                                 //
        //------------------------------------------------------------------------------------------------------------------------------------- //
    }

    private void runAsyncTask() {
        new AsyncWebpage(selectedHeroes).execute();
    }

    public void sort(LinkedList<List> unsortedRatings) {
        LinkedList<List> condensedRatings; //Used when combining lists that have an equal name value
        Collections.sort(unsortedRatings, new nameComparator()); //SORTING BY HERO_NAME
        condensedRatings = Utilities.memberReduce(unsortedRatings, new LinkedList<List>()); //Removes duplicates and combines percentage rates for each hero
        Collections.sort(condensedRatings, new percentComparator()); //SORTING BY WIN PERCENTAGE

        ArrayList<String> tags = Utilities.extractTags(myButtonImages); //Remove selected heroes
        for(int i = 0; i < condensedRatings.size(); i++) {
            //System.out.println("CURRENT UNFORMATED " + condensedRatings.get(i).get(0));
            //unFormatHero is used to ensure that each hero name taken from the website matches the name in the image tags
            String currHero = Utilities.unFormatHero((String)condensedRatings.get(i).get(0),tags);
            //System.out.println("CURRENT FORMATTED " + currHero);
            if(Utilities.isSelected(currHero,selectedHeroes))
                condensedRatings.remove(condensedRatings.get(i));
        }

        /*for(int k = 0; k < 6; k++) {
            System.out.println(condensedRatings.get(k).get(0));
        }*/
        ratings = condensedRatings;
        intState.set(intState.get() + 1);
        doneProcessing = true;
    }

    public class percentComparator implements Comparator<List> { //Custom comparator for my LinkedList<List> where List = (String "hero_name", Double winPercent)
        @Override
        public int compare(List lhs, List rhs) {
            if((Double)lhs.get(1) < (Double)rhs.get(1))
                return -1;
            else if((Double)lhs.get(1) > (Double)rhs.get(1))
                return 1;
            else
                return 0; //Equals
        }
    }
    public class nameComparator implements Comparator<List> { //Custom comparator for my LinkedList<List> where List = (String "hero_name", Double winPercent)
        @Override
        public int compare(List lhs, List rhs) {
            //Declaring as a string so I have access to compareTo
            String lhsStr = (String)lhs.get(0); //Hero name
            return lhsStr.compareTo((String)rhs.get(0));
        }
    }

    private class AsyncWebpage extends AsyncTask<Void, Void, Void> { //Allows the program to do work in the background
        ArrayList<String> myHeroes;
        LinkedList<List> newRatings = new LinkedList<>();

        public AsyncWebpage(ArrayList<String> selectedHeroes) {
            myHeroes = selectedHeroes;
        }


        @Override
        protected Void doInBackground(Void... params) {
                //TODO if fail to connect to internet, call useData() and use assets data
                //TODO When extracting data from internet, inflate a view with a loading circle and a textView that changes to show progress
            isDataOld = false; //THIS MEANS IT IS NEVER ACCESSING THE INTERNET ... USING DATA FROM ASSETS FOLDER
            if(isDataOld) {
                writeData(); //Collects fresh data and places into text file
                //myHeroes represents selected heroes
                //newRatings initialized on call
                newRatings = useData(myHeroes);
            }
            else if(!isDataOld) {
                newRatings = useData(myHeroes);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) { //When it's done pulling the info from the webpage
            sort(newRatings);

        }
    }

    public void addOverlay(ArrayList<String> heroes){ //Needs heroes parameter to avoid conflict with AsyncTask since both use selectedHeroes
        final ArrayList<String> heroesList = heroes;
        overlayChild = (RelativeLayout)inflater.inflate(R.layout.end_overlay, layoutView, false); //Element to be added onto parent
        overlayChild.setVisibility(View.GONE);
        overlayChild.setId(View.generateViewId());
        layoutView.addView(overlayChild, params); //set layout params

        TranslateAnimation translate = new TranslateAnimation(0,0,-200,0);
        translate.setDuration(800); translate.setFillAfter(true);
        translate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                overlayChild.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                addSelectedHeroes(heroesList);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        }); //Sets visible on start and also triggers the method to add the selected hero views
        overlayChild.startAnimation(translate);
    }

    public void addSelectedHeroes(ArrayList<String> heroes) { //Needs to be constructive to avoid conflict with AsyncTask since both use selectedHeroes
        addSelectedHeroesB(heroes, overlayChild); //OverlayChild is set as parent because this is the parent for the first hero
                                                         //After overlayChild, the parents will be the previously added heroes
    }
    //Tail-recursive auxilary function
    public void addSelectedHeroesB(ArrayList<String> heroes, RelativeLayout heroParent) { //heroParent represents the individual hero's parent
        newHeroes = heroes;
        if(newHeroes.size() == 0) { //BASE CASE FOR EXIT SCENARIO
            intState.set(intState.get() + 1); //adds one to the intState listener
            return;
        }
        hero = heroes.get(0);

        int viewHeight = getWindowManager().getDefaultDisplay().getHeight();
        viewHeight = viewHeight / 5;
        int viewWidth = (int)(viewHeight * ((double)71/127)); //Fraction representing the width * length ratio
        //System.out.println("viewHeight" + viewHeight + " viewWidth " + viewWidth);

        heroChild = (RelativeLayout)inflater.inflate(R.layout.end_item_hero, layoutView, false); //Inflating view based off of end_item_hero layout, which represents the previously selected heroes
        RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(viewHeight, viewWidth); //Initializing parameters for selected heroes
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); //Initializing parameters for selected heroes
        heroChild.setLayoutParams(childParams);
        heroChild.setId(View.generateViewId()); //Generating id so it will work with relative layout parameters
        childParams.addRule(RelativeLayout.BELOW, heroParent.getId()); childParams.addRule(RelativeLayout.CENTER_HORIZONTAL); childParams.setMargins(0, 25, 0, 0); //Adding parameters to the layout
        heroChild.setVisibility(View.INVISIBLE);

        //------------------- Setting properties for the specific end_item_hero.xml for this hero--------------------//
        ImageButton heroButton = matchTags(hero); //Finds the image button for the current hero
        ImageView image = (ImageView)heroChild.findViewById(R.id.heroImage);
        image.setId(View.generateViewId());
        imageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        image.setLayoutParams(imageParams); //This is a real train wreck of a function.
        Drawable imageDrawable = heroButton.getDrawable(); imageDrawable.clearColorFilter();
        image.setImageDrawable(imageDrawable); //Setting image for hero

        TextView text = (TextView) heroChild.findViewById(R.id.heroText); //Setting text for hero
        String formatText = Utilities.formatHero(heroButton.getTag().toString()); //Removes underscore and capitalizes first letters
        text.setText(formatText);
        text.setLayoutParams(textParams);
        textParams.addRule(RelativeLayout.BELOW, image.getId());textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutView.addView(heroChild);
        //-----------------------------------------------------------------------------------------------------------//
        TranslateAnimation heroTranslate = new TranslateAnimation(0,0,800,0); heroTranslate.setDuration(1000);
        AlphaAnimation alpha = new AlphaAnimation(0f, 1.0f);  alpha.setDuration(700);
        //Using animation set so I can fire two animations at once.
        AnimationSet set = new AnimationSet(false); set.addAnimation(alpha); set.addAnimation(heroTranslate); set.setFillAfter(true);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                newHeroes.remove(hero);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                addSelectedHeroesB(newHeroes, heroChild);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        }); //RECURSIVE CALL INSIDE LISTENER
        heroChild.startAnimation(set);


    }

    public ImageButton matchTags(String hero) { //Used to find the the associated image button so that the appropriate hero image is used when showing the user what heroes were selected
        for (ImageButton button : myButtonImages) { //Selected heroes is a list of currently pressed heroes
            if (hero.equals((String) button.getTag())) { //If the pressed button is already faded out,
                return button;
            }
        }
        return null;
    }

    public void sharedPreferences() {
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        long currTime = System.currentTimeMillis();
        Long pastTime = pref.getLong("key_date", 0);
        long difference;
        long diffInWeeks;

        if(isDataOld) { //ONLY TRUE IF IT HAS ALREADY BEEN SET. This is being called in doInBackground
            editor.putLong("key_date",currTime);
            editor.commit();
            System.out.println("RESETTING TIME");
            isDataOld = false;
        }

        if(pastTime == 0) {
            editor.putLong("key_date",currTime);
            editor.commit();
            System.out.println("FIRST TIME START UP ... Putting in current time \n Enterring default data");
            writeOldData();
        } else {
            difference = currTime - pastTime;
            diffInWeeks = difference / DateUtils.WEEK_IN_MILLIS;
            System.out.println("DIFFERENCE IN Weeks " + diffInWeeks);
            System.out.println("DIFFERENCE IN SECONDS " + difference / DateUtils.SECOND_IN_MILLIS);
            if(diffInWeeks >= 4) {
                file.delete();
                isDataOld = true;
            }
        }
    }

    public void initializeFiles() throws FileNotFoundException {
        file = new File(getApplicationContext().getFilesDir(),MY_FILE_NAME);
        //TODO right now it just deletes everytime to avoid data errors
        //The data error is a result of writeOldData failing overwrite the data in the internal storage after app updates.
        file.delete();
        try {
            if(!file.exists())
                file.createNewFile();

        } catch (IOException e) {}
        fileOut = new FileOutputStream(file,true);
        fileIn = new FileInputStream(file);
        writeOldData(); //TODO shouldn't writeOldData everytime.
        /*I am writing old data because writeData() is error prone and can take minutes to extract all the data
        easier for me to call the writeData function then manually add the data to hero_data.txt where it will
        be accessed from the writeOldData function*/

    }

    //Connects to the internet and extract data
    public void writeData() {
        try { //ensures that the file is empty
            file.delete();
            file.createNewFile();
        } catch (IOException e) {}

        ArrayList<String> tags = Utilities.extractTags(myButtonImages);
        OutputStreamWriter writer = new OutputStreamWriter(fileOut); //Used to write to textFile
        String dataLine = "x";

        for (int i = 0; i < tags.size(); i++) {
            //System.out.println("ATTEMPTED HERO NAME " + tags.get(i));
            String modifiedName = Utilities.modifyHeroName(tags.get(i));
            String url = "http://dotamax.com/hero/detail/match_up_anti/" + modifiedName + "/?skill=vh";
            Document doc = null;
            try {
                doc = Jsoup.connect(url).header("Accept-Language", "en").get();
            } catch (Exception E) {
                System.out.println("UNABLE TO ESTABLISH CONNECTION GIVING GARBAGE DATA");
                E.printStackTrace();
            }
            Element table = doc.select("table[class=table table-hover table-striped sortable table-list table-thead-left]").first();
            Elements rows = table.select("tr");
            for (int j = 1; j < rows.size(); j++) { //Loops through each row
                Element currRow = rows.get(j);
                Elements columns = currRow.select("td");
                String heroName = columns.get(0).select("span").text(); //Gets hero name
                String rating   = columns.get(1).text().replace("%","");
                dataLine = modifiedName + "," + heroName + "," + rating;
                System.out.println(dataLine);
                //UNCOMMENT TO WRITE TO INTERNAL STORAGE
                /*try {
                    writer.write(dataLine + "\n");
                    writer.flush();
                } catch(IOException e) {
                    e.printStackTrace();
                }*/
            }

        } //end of main for loop
        try {
            writer.close();
            sharedPreferences();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    //Reads data from hero_data.txt then returns an list containing data for the selected heroes
    public LinkedList<List> useData(ArrayList<String> myHeroes) {
        System.out.println("Using data from internal storage at useData function");
        BufferedReader br = new BufferedReader(new InputStreamReader(fileIn));
        String dataSplit[];
        String dataLine;
        LinkedList<List> dataList = new LinkedList<>();
        try {
            while ((dataLine = br.readLine()) != null) {
                //Dataline split into (selected hero),(hero),(percentage)
                dataSplit = dataLine.split(",");
                //System.out.println(dataLine);
                //unModifyHeroName switches the selected heroes' names so that they match the tags
                //Each name has 2 versions: website, which uses outdated names, and then tag name, which uses the recognizable names.
                if (Utilities.isSelected(Utilities.unModifyHeroName(dataSplit[0]), myHeroes)) {
                    String heroName = dataSplit[1];
                    Double rating = Double.parseDouble(dataSplit[2]); //Converts win percentage to double
                    List<Object> heroRating = Arrays.asList((Object) heroName, (Object) rating);
                    dataList.add(heroRating);
                    //System.out.println(heroName + " " + rating);
                }
            }
        } catch(IOException e) {e.printStackTrace();}

        return dataList;
    }

    //Reads data from asset text file and transfers it to internal storage file
    public void writeOldData() { //Current data date 1/17/16
        System.out.println("Writing old data from asset text to internal storage at writeOldData() function");
        OutputStreamWriter writer = new OutputStreamWriter(fileOut); //Used to write to textFile
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(getAssets().open("hero_data.txt")));

            // do reading, usually loop until end of file reading
            String dataLine;
            while ((dataLine = br.readLine()) != null) {
                writer.write(dataLine + "\n");
            }
        } catch (IOException e){}
        finally {
            if (br != null) {
                try {
                    br.close();
                    writer.close();
                } catch (IOException e){}
            }
        }
    }
}


