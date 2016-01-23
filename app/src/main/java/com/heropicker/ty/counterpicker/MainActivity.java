package com.heropicker.ty.counterpicker;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Class containing the selection screen with all the buttons representing heroes.
 * This activity follows StarterActivity and sends the user to MiddleActivity
 *
 * @author Ty Trusty
 * @version 1/16/16
 */
//How to solve opacity performance: make a 10% transparency for each button, then switch drawables. (Tedious and time consuming).
//TODO Current Bug: Heroes that are selected are permanently not affected by the filters, even after reset. It's almost as if they are being added to new filters
//TODO Maybe use parse or have a debug time to store difference in seconds to see if it changes on update
public class MainActivity extends AppCompatActivity {
    ArrayList<Integer> myButtonIndexes = new ArrayList<>();
    ArrayList<ImageButton> myButtonImages = new ArrayList<>();
    ArrayList<String> selectedHeroes = new ArrayList<>();
    ArrayList<String> filteredHeroes = new ArrayList<>();
    AlphaAnimation fadeOutAnimation; AlphaAnimation fadeInAnimation;
    TextView counter;
    Button calcButton;
    AlphaAnimation fastFadeOut;
    SearchManager searchManager; //For handling search in actionbar
    SearchView searchView;

    //-------------------------------------------Settings the attribute for each hero. Used for filtering ------------------------------------------------------- //
    String[] strengthHeroes  = {"abaddon","alchemist","axe","beastmaster","brewmaster","bristleback","centaur","chaos_knight","doom_bringer","dragon_knight",     //
            "earthshaker","elder_titan","huskar","kunkka","legion_commander","life_stealer","lycan","magnus","night_stalker","omniknight","pudge","clockwork",    //
            "sand_king","timbersaw","wraith_king","slardar","sven","tidehunter","tiny","treant","tusk","undying","wisp"};                                         //
    //
    String[] agilitityHeroes = {"antimage","arc_warden","bloodseeker","bounty_hunter","broodmother","clinkz","drow_ranger","ember_spirit","faceless_void",        //
            "gyrocopter","juggernaut","lone_druid","luna","medusa","meepo","mirana","morphling","naga_siren","shadow_fiend","nyx_assassin","phantom_assassin",    //
            "phantom_lancer","razor","riki","slark","sniper","spectre","spirit_breaker","templar_assassin","terrorblade","troll_warlord","ursa","vengefulspirit", //
            "venomancer","viper","weaver" };                                                                                                                      //
    //
    String[] intelHeroes     = {"ancient_apparition","bane","batrider","chen","crystal_maiden","dark_seer","dazzle","death_prophet","disruptor","earth_spirit",   //
            "enchantress","enigma","furion","invoker","jakiro","keeper_of_the_light","leshrac","lich","lina","lion","necrolyte","outworld_devourer","ogre_magi",  //
            "oracle","phoenix","puck","pugna","queenofpain","rubick","shadow_demon","shadow_shaman","silencer","skywrath_mage","storm_spirit","techies","tinker", //
            "visage","warlock","windrunner","winter_wyvern","witch_doctor","zeus"};                                                                               //
    //------------------------------------------------------------------------------------------------------------------------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        counter = (TextView) findViewById(R.id.selection_count);
        selectedHeroes.add("sentinel value"); //Adding a dummy value so that the advanced for loop is entered

        //-----------------Creating fade animations for selections of heroes ---------------------//
        fadeOutAnimation = new AlphaAnimation(1.0f, 0.5f); fadeOutAnimation.setDuration(500); fadeOutAnimation.setFillAfter(true);
        fadeInAnimation = new AlphaAnimation(0.5f, 1.0f);  fadeInAnimation.setDuration(500);  fadeInAnimation.setFillAfter(true);
        fastFadeOut = new AlphaAnimation(1.0f, 0.5f); fastFadeOut.setDuration(200); fastFadeOut.setFillAfter(false); fastFadeOut.setRepeatMode(Animation.REVERSE); fastFadeOut.setRepeatCount(1);
        fastFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                MiddleActivity.myButtonImages = myButtonImages; //SENDING myButtonImages data to MiddleActivity by setting static variable
                Intent i = new Intent(getApplicationContext(), MiddleActivity.class);
                i.putExtra("heroes", selectedHeroes);        //Sending selected heroes data with an intent
                startActivity(i);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //----------------------------------------------------------------------------------------//
        //Initializing an array of the button indexes
        for (int i = 0; i < 112; i++) {
            try {
                int resID = getResources().getIdentifier("imageButton" + i, "id", getPackageName());
                myButtonIndexes.add(resID);
            } catch (Exception E) {
                System.out.println("Index " + i + " is not a button");
                break;
            }
        }
        //----------------------------------------------------------------------------------------//
        //Setting a listener for each image button
        //----------------------------------------------------------------------------------------//
        for (int index : myButtonIndexes) {
            if (index != 0) {
                myButtonImages.add((ImageButton) findViewById(index)); //IMPORTANT: Initializes array list of image buttons
                for (final ImageButton button : myButtonImages) {
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (String tag : selectedHeroes) { //Selected heroes is a list of currently pressed heroes

                                if (tag.equals((String) button.getTag())) { //If the pressed button is already faded out,
                                    button.startAnimation(fadeInAnimation); //then fade it in and remove it from selected heroes
                                    button.clearColorFilter();
                                    selectedHeroes.remove(tag);
                                    counter.setText(selectedHeroes.size()-1 + " / " + 5); //Setting new value for counter
                                    return; //Escapes the method
                                }
                            }
                            if( !(selectedHeroes.size() > 5)) {
                                button.startAnimation(fadeOutAnimation);        //If this is reached, the button was not selected,
                                selectedHeroes.add((String) button.getTag());   //and this fades it out and sets the button as selected
                                counter.setText(selectedHeroes.size() - 1 + " / " + 5); //Sets the counter
                                button.setColorFilter(Color.argb(150, 155, 155, 155));
                                String str = Utilities.formatHero(button.getTag().toString()) + " selected";
                                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Maximum number of heroes selected!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }
        //----------------------------------------------------------------------------------------//

        //-------------------------------Listener for calculate button----------------------------//
        calcButton = (Button) findViewById(R.id.calculate_button);
        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedHeroes.size() > 1)
                    calcButton.startAnimation(fastFadeOut);
                else
                    Toast.makeText(getApplicationContext(),"Select at least one hero!", Toast.LENGTH_SHORT).show();
            }
        });
        //----------------------------------------------------------------------------------------//
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu); //inflates menu with search bar
        searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.search).getActionView();
                  searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                  searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchHeroes(query);
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setIconified(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //System.out.println("CURR TEXT " + newText);
                searchHeroes(newText);
                return true;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.reset) { //If reset button is clicked, then un-select all heroes
            resetSelection();
            unFilter();
        //IF THE USER FILTERS FOR AGI HEROES ONLY
        } else if(id == R.id.menuFilterAgi) {
            unFilter();
            filterHeroes("agility");
        }
        //IF THE USER FILTERS FOR STR HEROES ONLY
        else if(id == R.id.menuFilterStr) {
            unFilter();
            filterHeroes("strength");
        }
        //IF THE USER FILTERS FOR INT HEROES ONLY
        else if(id == R.id.menuFilterInt) {
            unFilter();
            filterHeroes("intelligence");
        }
        return super.onOptionsItemSelected(item);
    }


    public void searchHeroes(String text) {
        if(text.equals("")) {
            unFilter();
        }
        int subNum = text.length();
        for (final ImageButton button : myButtonImages) {
            try {
                String subTag = button.getTag().toString().substring(0, subNum);
                if (text.equals(subTag)) {
                    button.setVisibility(View.VISIBLE);
                    button.setClickable(true);
                } else {
                    button.setVisibility(View.GONE);
                    button.setClickable(false);
                }
            } catch (Exception e) {}
        }

    }

    public void filterHeroes(String type) {
        String[] filter1 = null,filter2 = null;
        if(type.equals("strength"))          {filter1 = intelHeroes;    filter2 = agilitityHeroes;}
        else if(type.equals("intelligence")) {filter1 = strengthHeroes; filter2 = agilitityHeroes;}
        else if(type.equals("agility"))      {filter1 = strengthHeroes; filter2 = intelHeroes;}

        for (final ImageButton button : myButtonImages) {
            for(int i=0; i < filter1.length; i++) {
                if(filter1[i].equals(button.getTag())) {
                    filteredHeroes.add(filter1[i]);
                    //button.startAnimation(fullFadeOut);
                    button.setVisibility(View.INVISIBLE);
                    button.setClickable(false);
                    continue;
                }
            }
            for(int i=0; i < filter2.length; i++) {
                if(filter2[i].equals(button.getTag())) {
                    filteredHeroes.add(filter2[i]);
                    //button.startAnimation(fullFadeOut);
                    button.setVisibility(View.INVISIBLE);
                    button.setClickable(false);
                    continue;
                }
            }

        }
    }
    public void unFilter() {
        filteredHeroes.clear();
        for (final ImageButton button : myButtonImages) {
            button.setVisibility(View.VISIBLE);
            button.setClickable(true);
            /*
            for(String filter: filteredHeroes){
                if(button.getTag().equals(filter)) {
                    //button.startAnimation(fullFadeIn);
                    button.setVisibility(View.VISIBLE);
                    button.setClickable(true);
                    filteredHeroes.remove(filter); //Specific button is no longer filtered.
                    break;
                }
            }*/
        }
    }

    public void resetSelection() {
        unFilter();
        for (final ImageButton button : myButtonImages) {
            for (String tag : selectedHeroes) { //Selected heroes is a list of currently pressed heroes
                if (tag.equals((String) button.getTag())) { //If the pressed button is already faded out,
                    button.startAnimation(fadeInAnimation); //then fade it in and remove it from selected heroes
                    button.clearColorFilter();
                    selectedHeroes.remove(tag);
                    break;
                }
            }
        }
        counter.setText(selectedHeroes.size() - 1 + " / " + 5);
    }

}




