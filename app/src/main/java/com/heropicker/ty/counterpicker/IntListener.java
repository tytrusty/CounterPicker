package com.heropicker.ty.counterpicker;

/**
 * Created by TY on 12/20/2015.
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