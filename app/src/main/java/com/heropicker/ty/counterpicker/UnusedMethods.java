package com.heropicker.ty.counterpicker;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class containing deprecated or unused methods in case I need them in the future.
 *
 * @author Ty Trusty
 * @version 1/16/16
 */
public class UnusedMethods {
    boolean isDataOld = false;
    FileInputStream fileIn;


    //This method was originally used to test if it was the user's first startup, but that has changed and I am now using shared preferences to determine that
    public void checkFileEmpty() { //FUNCTION NOT USED
        try {
            if(fileIn == null || fileIn.available() == 0) {
                isDataOld = true; //means there is no data present so this is set to true so that write data is called
                System.out.println("FILE IS EMPTY");
            } else {
                System.out.println("FILE IS NOT EMPTY");
            }
            System.out.println("AVAILABLE VALUE " + fileIn.available());

        } catch (IOException e) { e.printStackTrace();}

    }
}
