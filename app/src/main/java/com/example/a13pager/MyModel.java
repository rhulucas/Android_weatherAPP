package com.example.a13pager;

public class MyModel {
    String mv_city;
    String mv_cond;
    int mv_color;
    double mv_temp;
    double mv_temp_high;
    double mv_temp_low;
    String  mv_weather_icon;
    String mv_sun, mv_mon, mv_tue;
    String mv_sun_icon, mv_mon_icon, mv_tue_icon;
    String mv_tempF;

    public MyModel(String city, String cond, int color,double temp, double temp_high, double temp_low,
                   String weather_icon, String sun,String mon, String tue,
                   String sunIcon, String monIcon, String tusIcon) {
        mv_city = city;
        mv_cond = cond;
        mv_color = color;
        mv_temp = temp;
        mv_temp_high = temp_high;
        mv_temp_low = temp_low;
        mv_weather_icon = weather_icon;
        mv_sun = sun;
        mv_mon = mon;
        mv_tue = tue;
        mv_sun_icon = sunIcon;
        mv_mon_icon = monIcon;
        mv_tue_icon = tusIcon;
    }

    public String mf_getCity() {
        return mv_city;
    }

    public String mf_getCond() {
        return mv_cond;
    }

    public int mf_getColor() {
        return mv_color;
    }
    public double mf_getTemp() {
        return mv_temp;
    }
    public double mf_getTempHigh() {return mv_temp_high;}
    public double mf_getTempLow() {return mv_temp_low;}
    public String mf_getWeatherIcon(){return mv_weather_icon;}
    public String mf_getSun(){return mv_sun;}
    public String mf_getMon(){return mv_mon;}
    public String mf_getTue(){return mv_tue;}
    public String mf_getSunIcon(){return mv_sun_icon;}
    public String mf_getMonIcon(){return mv_mon_icon;}
    public String mf_getTueIcon(){return mv_tue_icon;}

    public String mf_getTempF() {
        return mv_tempF;
    }
    public void setCity(String city){this.mv_city = city;}
    public void setTemp(double temp) {
        this.mv_temp = temp;
    }

    public void setTempHigh(double tempHigh) {
        this.mv_temp_high = tempHigh;
    }

    public void setTempLow(double tempLow) {
        this.mv_temp_low = tempLow;
    }

    public void setSun(String sun) {
        this.mv_sun = sun;
    }

    public void setMon(String mon) {
        this.mv_mon = mon;
    }

    public void setTue(String tue) {
        this.mv_tue = tue;
    }
    public void setCond(String cond) {
        this.mv_cond = cond;
    }
    public void setWeatherIcon(String weather_icon) {this.mv_weather_icon = weather_icon;}
    public void setSunIcon(String sunIcon) {this.mv_sun_icon = sunIcon;}
    public void setMonIcon(String monIcon){this.mv_mon_icon = monIcon;}
    public void setTusIcon(String tusIcon) {this.mv_tue_icon = tusIcon;}



}
