package com.appl.weathershow;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
class ReadLocationTask extends AsyncTask<Object, Object, String>
{
   private static final String TAG = "ReadLocatonTask.java";   
   private String zipcodeString; 
   private Context context;
   private Resources resources;
   private String cityString;
   private String stateString;
   private String countryString;
   private LocationLoadedListener weatherLocationLoadedListener; 
   public interface LocationLoadedListener 
   {
      public void onLocationLoaded(String cityString, String stateString, 
         String countryString);
   } 
   public ReadLocationTask(String zipCodeString, Context context, 
      LocationLoadedListener listener)
   {
      this.zipcodeString = zipCodeString;
      this.context = context;
      this.resources = context.getResources();
      this.weatherLocationLoadedListener = listener;
   } 
   @Override
   protected String doInBackground(Object... params) 
   {
      try 
      {
          URL url = new URL(resources.getString(
                 R.string.location_url_pre_zipcode) + zipcodeString + 
                 "&api_key=xmzxnh7umuzyh7738kbak7dc");
          Reader forecastReader = new InputStreamReader(
            url.openStream());
          JsonReader forecastJsonReader = new JsonReader(forecastReader);
         forecastJsonReader.beginObject(); 
         String name = forecastJsonReader.nextName();
         if (name.equals(resources.getString(R.string.location)))
         {
              forecastJsonReader.beginObject();  
              String nextNameString;
              while (forecastJsonReader.hasNext()) 
            {
               nextNameString = forecastJsonReader.nextName();
               if ((nextNameString).equals(
                  resources.getString(R.string.city))) 
               {
                      cityString = forecastJsonReader.nextString();
               } 
               else if ((nextNameString).equals(resources.
                  getString(R.string.state))) 
               {
                  stateString = forecastJsonReader.nextString();
               } 
               else if ((nextNameString).equals(resources.
                  getString(R.string.country))) 
               {
                  countryString = forecastJsonReader.nextString();
               }
               else 
               {
                  forecastJsonReader.skipValue();
               } 
            }
                  forecastJsonReader.close(); 
         }
      } 
      catch (MalformedURLException e) 
      {
         Log.v(TAG, e.toString()); 
      } 
      catch (IOException e) 
      {
         Log.v(TAG, e.toString()); 
      }
     
      return null; 
   } 
     protected void onPostExecute(String nameString)
   {
         if (cityString != null)
      {
       
           weatherLocationLoadedListener.onLocationLoaded(cityString, 
            stateString, countryString);
      }
      else 
      {
          Toast errorToast = Toast.makeText(context, resources.getString(
            R.string.invalid_zipcode_error), Toast.LENGTH_LONG);
         errorToast.setGravity(Gravity.CENTER, 0, 0);
         errorToast.show();
      } 
   } 
}