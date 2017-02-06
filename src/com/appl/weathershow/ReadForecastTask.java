package com.appl.weathershow;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import com.appl.weathershow.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
class ReadForecastTask extends AsyncTask<Object, Object, String> 
{
   private String zipcodeString; 
   private Resources resources;
   private ForecastListener weatherForecastListener; 
   private static final String TAG = "ReadForecastTask.java";
   
   private String temperatureString; 
   private String feelsLikeString; 
   private String humidityString; 
   private String chanceOfPrecipitationString; 
   private Bitmap iconBitmap;
   
   private int bitmapSampleSize = -1;
   public interface ForecastListener 
   {
      public void onForecastLoaded(Bitmap image, String temperature, 
         String feelsLike, String humidity, String precipitation);
   }
   public ReadForecastTask(String zipcodeString, 
      ForecastListener listener, Context context)
   {
      this.zipcodeString = zipcodeString;
      this.weatherForecastListener = listener;
      this.resources = context.getResources();
   } 
   public void setSampleSize(int sampleSize)
   {
      this.bitmapSampleSize = sampleSize;
   } 
    protected String doInBackground(Object... args) 
   {
      try 
      {
         URL webServiceURL = new URL(resources.getString(
            R.string.pre_zipcode_url) + zipcodeString  + "&ht=t&ht=i&"
            + "ht=cp&ht=fl&ht=h&api_key=xmzxnh7umuzyh7738kbak7dc");
          Reader forecastReader = new InputStreamReader(
            webServiceURL.openStream());
         JsonReader forecastJsonReader = new JsonReader(forecastReader);
         forecastJsonReader.beginObject();
         String name = forecastJsonReader.nextName();
         if (name.equals(resources.getString(R.string.hourly_forecast))) 
         {
            readForecast(forecastJsonReader); 
         } 
         forecastJsonReader.close(); 
      }
      catch (MalformedURLException e) 
      {
         Log.v(TAG, e.toString());
      } 
      catch (IOException e) 
      {
         Log.v(TAG, e.toString());
      }
      catch (IllegalStateException e)
      {
        Log.v(TAG, e.toString() + zipcodeString);
      } 
      return null;  
   } 

   protected void onPostExecute(String forecastString) 
   {
      weatherForecastListener.onForecastLoaded(iconBitmap, 
         temperatureString, feelsLikeString, humidityString, 
         chanceOfPrecipitationString);
   }
  
   public static Bitmap getIconBitmap(String conditionString, 
      Resources resources, int bitmapSampleSize) 
   {
      Bitmap iconBitmap = null;
      try 
      {
            URL weatherURL = new URL(resources.getString(
            R.string.pre_condition_url) + conditionString + 
            resources.getString(R.string.post_condition_url));
         
         BitmapFactory.Options options = new BitmapFactory.Options();
         if (bitmapSampleSize != -1) 
         { 
            options.inSampleSize = bitmapSampleSize;
         } 
         
         iconBitmap = BitmapFactory.decodeStream(weatherURL.
            openStream(), null, options);
      }
      catch (MalformedURLException e) 
      {
         Log.e(TAG, e.toString());
      } 
      catch (IOException e) 
      {
         Log.e(TAG, e.toString());
      } 
      return iconBitmap; 
   } 

   private String readForecast(JsonReader reader)
   {
      try 
      {
         reader.beginArray();
         reader.beginObject(); 

         while (reader.hasNext()) 
         {
            String name = reader.nextName(); 

            if (name.equals(resources.getString(R.string.temperature)))
            {
               temperatureString = reader.nextString(); 
            } 
                else if (name.equals(resources.getString(R.string.feels_like)))  
            {
                feelsLikeString = reader.nextString(); 
            }
                else if (name.equals(resources.getString(R.string.humidity))) 
            {
               humidityString = reader.nextString(); 
            } 
               else if (name.equals(resources.getString(
               R.string.chance_of_precipitation))) 
            {
              
               chanceOfPrecipitationString = reader.nextString();
            } 
               else if (name.equals(resources.getString(R.string.icon))) 
            {
              
                iconBitmap = getIconBitmap(reader.nextString(), resources, 
                   bitmapSampleSize);
            }
            else 
            {
               reader.skipValue();
            } 
         }
      }
      catch (IOException e) 
      {
         Log.e(TAG, e.toString());
      }
      return null;
   } 
}
