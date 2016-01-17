package com.heropicker.ty.counterpicker;

/**
 * Listener used in the MiddleActivity (3rd).
 * Used in conjuction with LoadListener interface.
 * Used in MiddleActivity to test whether AsyncTask and addOverlay functions have finished execution.
 *
 *
 * @author Ty Trusty
 * @version 12/20/15
 */
public class IntListener {
    private LoadListener listener;

    private int intValue;

    public void setOnChangeListener(LoadListener listener) {
        this.listener = listener;
    }

    public int get() {
        return intValue;
    }

    public void set(int value) {
        intValue = value;

        if(listener != null) {
            listener.onChange(value);
        }
    }
}