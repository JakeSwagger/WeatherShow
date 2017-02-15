package com.appl.weathershow;

import android.graphics.Bitmap;

public class DailyForecast 
{
   public static final int DAY_INDEX = 0;
   public static final int PREDICTION_INDEX = 1;
   public static final int HIGH_TEMP_INDEX = 2;
   public static final int LOW_TEMP_INDEX = 3;
   
   final private String[] forecast; 
   final private Bitmap iconBitmap; 

   public DailyForecast(String[] forecast, Bitmap iconBitmap)
   {
      this.forecast = forecast;
      this.iconBitmap = iconBitmap;
   } 

   public Bitmap getIconBitmap()
   {
      return iconBitmap;
   }
  
   public String getDay()
   {
      return forecast[DAY_INDEX];      
   }
   
   public String getDescription()
   {
      return forecast[PREDICTION_INDEX];      
   } 

   public String getHighTemperature()
   {
      return forecast[HIGH_TEMP_INDEX];      
   }   
   
   public String getLowTemperature()
   {
      return forecast[LOW_TEMP_INDEX];      
   } 
}