package com.heropicker.ty.counterpicker;

import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Class containing static utility methods. Used for organization purposes
 *
 * @author Ty Trusty
 * @version 1/16/16
 */
public class Utilities {
    public static String formatHero(String hero){
        String newHero = "";
        String[] heroArray = hero.split("_");
        for(String str : heroArray) {
            newHero = newHero + str.substring(0,1).toUpperCase() + str.substring(1) + " ";
        }
        return newHero;
    }

    //When
    public static String unFormatHero(String hero, ArrayList<String> tags){
        hero = modifyHeroName(hero);
        if(hero.equals("Shadow Shaman")) return "shadow_shaman";
        else if(hero.equals("Shadow Demon")) return "shadow_demon";
        else if(hero.equals("Earthshaker")) return "earthshaker";
        else if(hero.equals("Slark")) return "slark";
        else if(hero.equals("Tiny")) return "tiny";
        for(String tag: tags) {
            if(tag.length() > 4 && hero.length() > 4) {
                if (hero.substring(0, 4).toLowerCase().equals(tag.substring(0, 4))) {
                    return tag;
                }
            }
            else if(tag.length() > 3) {
                //System.out.println("TAG " + tag + "HERO" + hero);
                if(hero.substring(0,3).toLowerCase().equals(tag.substring(0,3))) {
                    //System.out.println("Hero " + hero + " equals tag " + tag);
                    return tag;
                }
            } else if(tag.length() > 2) {
                if (hero.substring(0, 2).toLowerCase().equals(tag.substring(0, 2))) {
                    return tag;
                }
            }
        }
        String newHero = "";
        String[] heroArray = hero.split("");
        for (String str : heroArray) {
            newHero = newHero + str.toLowerCase();
        }
        return newHero.replaceAll(" ", "_");

    }

    //Each button has a tag that is used to identify the hero. This returns a list containing those tags
    public static ArrayList<String> extractTags(ArrayList<ImageButton> buttons) {
        ArrayList<String> tags = new ArrayList<String>();
        for(ImageButton button : buttons) {
            if(button.getTag() != null)
                tags.add(button.getTag().toString());
        }
        return tags;
    }

    //When connecting to the website to extract data, the website uses wonky outdated names for many heroes. This changes that
    public static String modifyHeroName(String hero){
        if(hero.toLowerCase().equals("zeus"))
            return "zuus";
        else if(hero.toLowerCase().equals("io"))
            return "wisp";
        else if(hero.toLowerCase().equals("magnus"))
            return "magnataur";
        else if(hero.toLowerCase().equals("shadow_fiend"))
            return "nevermore";
        else if(hero.toLowerCase().equals("outworld_devourer"))
            return "obsidian_destroyer";
        else if(hero.toLowerCase().equals("clockwork"))
            return "rattletrap";
        else if(hero.toLowerCase().equals("wraith_king"))
            return "skeleton_king";
        else if(hero.toLowerCase().equals("timbersaw"))
            return "shredder";
        return hero;
    }
    //After extracting the data, this method reverses that. Probably a better way to do this, but this was easy...
    public static String unModifyHeroName(String hero){
        if(hero.toLowerCase().equals("zuus"))
            return "zeus";
        //else if(hero.toLowerCase().equals("wisp"))
           // return "io";
        else if(hero.toLowerCase().equals("magnataur"))
            return "magnus";
        else if(hero.toLowerCase().equals("nevermore"))
            return "shadow_fiend";
        else if(hero.toLowerCase().equals("obsidian_destroyer"))
            return "outworld_devourer";
        else if(hero.toLowerCase().equals("rattletrap"))
            return "clockwork";
        else if(hero.toLowerCase().equals("skeleton_king"))
            return "wraith_king";
        else if(hero.toLowerCase().equals("shredder"))
            return "timbersaw";
        else if(hero.toLowerCase().equals("nature's_prophet"))
            return "furion";
        return hero;
    }


    //Removes duplicate heroes and adds their percentages together
    //This function is used to process the data for each hero
        /*WHY THIS IS NEEDED: the function receives data that may look like this:
              abaddon, 0.5      \
              abaddon, 1.0       ->         abaddon, 1.8
              abaddon, 0.3      /
                                THIS FUNCTION RETURNS
              alchemist, 4.2    \
              alchemist, 2.1     ->         alchemist, 6.8
              alchemist, 0.5    /
        */
    public static LinkedList<List>  memberReduce(LinkedList<List> heroList, LinkedList<List> resultList) {
        if(heroList == null || heroList.isEmpty()) { //SAFE CASE
            return resultList;
        }
        List<Object> hero = heroList.get(0);
        String heroName = (String)hero.get(0);
        Double heroNum = (Double)hero.get(1);

        LinkedList<List>  newHeroList = heroList; newHeroList.remove(hero); //Removes first element so hero is not seen as a duplicate
        if(newHeroList.size() == 0) {//If list is empty when newHeroList removes the first element, then it is at the end of the list
            resultList.add(hero);
            return resultList;
        }
        for(int i = 0; i < newHeroList.size() ; i++) {
            List newHero = newHeroList.get(i);
            //System.out.println("HEROLIST SIZE " + newHeroList.size());
            //System.out.println("NEWHERO " + newHero.get(0));
            //System.out.println("heroName " + heroName);
            if (heroName.equals(newHero.get(0))) {
                //System.out.println("INSIDE FOR LOOP: HEROLIST SIZE " + newHeroList.size());
                heroNum += (Double)newHero.get(1);
                newHeroList.remove(newHero); //if(newHeroList.size() == 1)
                i--; //This ensures one more iteration
            } else { //Hits else when newHeroList.get(0) now represents a new hero
                List reducedHero = Arrays.asList((Object) heroName, (Object) heroNum);
                resultList.add(reducedHero);
                return memberReduce(newHeroList, resultList); //Recursive case
            }
        }
        if(newHeroList.size() == 0) {//If list is empty when newHeroList removes the first element, then it is at the end of the list
            List reducedHero = Arrays.asList((Object) heroName, (Object) heroNum);
            resultList.add(reducedHero);
            return resultList;
        }
        System.out.println("returning null in Utilities.memberReduce");
        return null;
    }

    //Simple boolean method that loops through selected heroes to see if the hero is in the list
    public static boolean isSelected(String hero, ArrayList<String> selectedHeroes) {
        for(String heroName : selectedHeroes) {
            //System.out.println("hero " + hero + " current selected hero " + heroName);
            if(heroName.equals(hero)) {
                return true;
            }
        }
        return false; //If it reaches this point, then hero is not in the list of selected heroes
    }

}
