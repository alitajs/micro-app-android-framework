package com.alitajs.micro.bean;

import java.io.Serializable;

public class ThemeBean implements Serializable {

    String textColor;
    String backgroundColor;

    public ThemeBean(String textColor, String backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
