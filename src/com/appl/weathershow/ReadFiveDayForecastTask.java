package com.appl.weathershow;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import com.appl.weathershow.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

class ReadFiveDayForecastTask extends AsyncTask<Object, Object, String> 
{
   private static final String TAG = "ReadFiveDayForecastTask";   
   
   private String zipcodeString;
   private FiveDayForecastLoadedListener weatherFiveDayForecastListener;
   private Resources resources;
   private DailyForecast[] forecasts;
   private static final int NUMBER_OF_DAYS = 5;

   public interface FiveDayForecastLoadedListener 
   {
      public void onForecastLoaded(DailyForecast[] forecasts);
   } 
 
   public ReadFiveDayForecastTask(String zipcodeString, 
      FiveDayForecastLoadedListener listener, Context context)
   {
      this.zipcodeString = zipcodeString;
      this.weatherFiveDayForecastListener = listener;
      this.resources = context.getResources();
      this.forecasts = new DailyForecast[NUMBER_OF_DAYS];
   } 
      
   @Override
   protected String doInBackground(Object... params) 
   {
        try 
      {
         URL webServiceURL = new URL("http://i.wxbug.net/REST/Direct/" +
            "GetForecast.ashx?zip="+ zipcodeString  + "&ht=t&ht=i&"
            + "nf=7&ht=cp&ht=fl&ht=h&api_key=xmzxnh7umuzyh7738kbak7dc");

         Reader forecastReader = new InputStreamReader(
            webServiceURL.openStream());

         JsonReader forecastJsonReader = new JsonReader(forecastReader);
             
         forecastJsonReader.beginObject();

         String name = forecastJsonReader.nextName();

         if (name.equals(resources.getString(R.string.forecast_list))) 
         {
            forecastJsonReader.beginArray();
            forecastJsonReader.skipValue();

            for (int i = 0; i < NUMBER_OF_DAYS; i++)
            {
               forecastJsonReader.beginObject(); 

               if (forecastJsonReader.hasNext())
               {

                  forecasts[i] = readDailyForecast(forecastJsonReader); 
               }
            } 
         }
              
         forecastJsonReader.close(); 
         
      }
      catch (MalformedURLException e) 
      {
         Log.v(TAG, e.toString());
      }
      catch (NotFoundException e) 
      {
    	  Log.v(TAG, e.toString());
      }  
      catch (IOException e) 
      {
    	  Log.v(TAG, e.toString());
      } 
      return null;
   } 

   private DailyForecast readDailyForecast(JsonReader forecastJsonReader)
   {

      String[] dailyForecast = new String[4]; 
      Bitmap iconBitmap = null; 
      
      try 
      {
          while (forecastJsonReader.hasNext()) 
         {
            String name = forecastJsonReader.nextName(); 

            if (name.equals(resources.getString(R.string.day_of_week)))
            {
               dailyForecast[DailyForecast.DAY_INDEX] = 
                  forecastJsonReader.nextString(); 
            } 
            else if (name.equals(resources.getString(
               R.string.day_prediction)))  
            {
               dailyForecast[DailyForecast.PREDICTION_INDEX] = 
                forecastJsonReader.nextString(); 
            }
            else if (name.equals(resources.getString(R.string.high))) 
            {
               dailyForecast[DailyForecast.HIGH_TEMP_INDEX] = 
                  forecastJsonReader.nextString(); 
            }
            else if (name.equals(resources.getString(R.string.low))) 
            {
               dailyForecast[DailyForecast.LOW_TEMP_INDEX] = 
                  forecastJsonReader.nextString(); 
            } 
               else if (name.equals(resources.getString(R.string.day_icon))) 
            {
              
               iconBitmap = ReadForecastTask.getIconBitmap(
                  forecastJsonReader.nextString(), resources, 0);
            } 
            else 
            {
               forecastJsonReader.skipValue();
            } 
         }
         forecastJsonReader.endObject();
      } 
      catch (IOException e) 
      {
         Log.e(TAG, e.toString());
      }
      
      return new DailyForecast(dailyForecast, iconBitmap);
   } 

   protected void onPostExecute(String forecastString) 
   {
      weatherFiveDayForecastListener.onForecastLoaded(forecasts);
   } 
}