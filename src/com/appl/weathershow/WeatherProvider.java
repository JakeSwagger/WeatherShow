package com.appl.weathershow;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.appl.weathershow.R;
import com.appl.weathershow.ReadForecastTask.ForecastListener;
import com.appl.weathershow.ReadLocationTask.LocationLoadedListener;

public class WeatherProvider extends AppWidgetProvider 
{
 private static final int BITMAP_SAMPLE_SIZE = 4;   

   @Override
   public void onUpdate(Context context, 
      AppWidgetManager appWidgetManager, int[] appWidgetIds) 
   {
      startUpdateService(context); 
   }

   private String getZipcode(Context context)
   {
      SharedPreferences preferredCitySharedPreferences = 
         context.getSharedPreferences(
         WeatherViewerActivity.SHARED_PREFERENCES_NAME, 
         Context.MODE_PRIVATE);
      String zipcodeString = preferredCitySharedPreferences.getString(
         WeatherViewerActivity.PREFERRED_CITY_ZIPCODE_KEY, 
         context.getResources().getString(R.string.default_zipcode));
      return zipcodeString; 
   } 
   
   @Override
   public void onReceive(Context context, Intent intent)
   {
      if (intent.getAction().equals(
         WeatherViewerActivity.WIDGET_UPDATE_BROADCAST_ACTION))
      {
         startUpdateService(context);
      } 
      super.onReceive(context, intent);
   } 

   private void startUpdateService(Context context)
   {
      Intent startServiceIntent;
      startServiceIntent = new Intent(context, WeatherService.class);
      startServiceIntent.putExtra(context.getResources().getString(
         R.string.zipcode_extra), getZipcode(context));
      context.startService(startServiceIntent);
   }

   public static class WeatherService extends IntentService implements 
      ForecastListener
   {
      public WeatherService() 
      {
         super(WeatherService.class.toString());
      } 
      private Resources resources;
      private String zipcodeString; 
      private String locationString;

      @Override
      protected void onHandleIntent(Intent intent) 
      {
         resources = getApplicationContext().getResources();
        
         zipcodeString = intent.getStringExtra(resources.getString(
            R.string.zipcode_extra));
         new ReadLocationTask(zipcodeString, this,
            new WeatherServiceLocationLoadedListener(
            zipcodeString)).execute();
      } 

      @Override
      public void onForecastLoaded(Bitmap image, String temperature,
         String feelsLike, String humidity, String precipitation) 
      {
         Context context = getApplicationContext();
         
         if (image == null) 
         {
            Toast.makeText(context, context.getResources().getString(
               R.string.null_data_toast), Toast.LENGTH_LONG);
            return;
         } 
         
         Intent intent = new Intent(context, WeatherViewerActivity.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(
            getBaseContext(), 0, intent, 0);

         RemoteViews remoteView = new RemoteViews(getPackageName(), 
            R.layout.weather_app_widget_layout);

         remoteView.setOnClickPendingIntent(R.id.containerLinearLayout, 
            pendingIntent);
         remoteView.setTextViewText(R.id.location, locationString);
         remoteView.setTextViewText(R.id.temperatureTextView, 
            temperature + (char)0x00B0 + resources.getString(
            R.string.temperature_unit));
         remoteView.setTextViewText(R.id.feels_likeTextView, feelsLike + 
            (char)0x00B0 + resources.getString(R.string.temperature_unit));
         remoteView.setTextViewText(R.id.humidityTextView, humidity + 
            (char)0x0025);
         remoteView.setTextViewText(R.id.precipitationTextView, 
            precipitation + (char)0x0025);
         remoteView.setImageViewBitmap(R.id.weatherImageView, image);
         ComponentName widgetComponentName = new ComponentName(this, 
            WeatherProvider.class);
         AppWidgetManager manager = AppWidgetManager.getInstance(this);
         manager.updateAppWidget(widgetComponentName, remoteView);
      } 
      private class WeatherServiceLocationLoadedListener implements 
         LocationLoadedListener
      {
         private String zipcodeString; 
         public WeatherServiceLocationLoadedListener(String zipcodeString)
         {
            this.zipcodeString = zipcodeString;
         } 
         @Override
         public void onLocationLoaded(String cityString, 
            String stateString, String countryString) 
         {
            Context context = getApplicationContext();
            
            if (cityString == null) 
            {
               Toast.makeText(context, context.getResources().getString(
                  R.string.null_data_toast), Toast.LENGTH_LONG);
               return;
            } 
            locationString = cityString + " " + stateString + ", " +
               zipcodeString + " " + countryString;
            ReadForecastTask readForecastTask = new ReadForecastTask(
               zipcodeString, (ForecastListener) WeatherService.this, 
               WeatherService.this);
            readForecastTask.setSampleSize(BITMAP_SAMPLE_SIZE);
            readForecastTask.execute();
         } 
      } 
   } 
} 