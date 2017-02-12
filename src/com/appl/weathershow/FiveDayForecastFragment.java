package com.appl.weathershow;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appl.weathershow.R;
import com.appl.weatherview.ReadFiveDayForecastTask.FiveDayForecastLoadedListener;
import com.appl.weatherview.ReadLocationTask.LocationLoadedListener;

public class FiveDayForecastFragment extends ForecastFragment 
{
   private static final String ZIP_CODE_KEY = "id_key";
   private static final int NUMBER_DAILY_FORECASTS = 5;
   
   private String zipcodeString; 
   private View[] dailyForecastViews = new View[NUMBER_DAILY_FORECASTS];
   
   private TextView locationTextView;
   
   public static FiveDayForecastFragment newInstance(String zipcodeString)
   {
         FiveDayForecastFragment newFiveDayForecastFragment = 
         new FiveDayForecastFragment();
         
      Bundle argumentsBundle = new Bundle(); 

      argumentsBundle.putString(ZIP_CODE_KEY, zipcodeString);
         
       newFiveDayForecastFragment.setArguments(argumentsBundle);
      return newFiveDayForecastFragment; 
   } 
  
   public static FiveDayForecastFragment newInstance(
      Bundle argumentsBundle) 
   {
      String zipcodeString = argumentsBundle.getString(ZIP_CODE_KEY);
      return newInstance(zipcodeString); 
   }   
 
   @Override 
   public void onCreate(Bundle argumentsBundle) 
   {
      super.onCreate(argumentsBundle);
  
      this.zipcodeString = getArguments().getString(ZIP_CODE_KEY);
   } 
   
   public String getZipcode() 
   {
      return zipcodeString;
   } 
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
      Bundle savedInstanceState) 
   {
      View rootView = inflater.inflate(R.layout.five_day_forecast_layout, 
         null);
     locationTextView = (TextView) rootView.findViewById(R.id.location);
      LinearLayout containerLinearLayout = 
      (LinearLayout) rootView.findViewById(R.id.containerLinearLayout);
     
      int id; 
    
      if (container.getContext().getResources().getConfiguration().
         orientation == Configuration.ORIENTATION_LANDSCAPE)
      {
         id = R.layout.single_forecast_layout_landscape;
      } 
      else 
      {
         id = R.layout.single_forecast_layout_portrait; 
         containerLinearLayout.setOrientation(LinearLayout.VERTICAL);
      } 
    
      View forecastView;
      for (int i = 0; i < NUMBER_DAILY_FORECASTS; i++)
      {
         forecastView = inflater.inflate(id, null);
        
         containerLinearLayout.addView(forecastView);
         dailyForecastViews[i] = forecastView; 
      }
     
      new ReadLocationTask(zipcodeString, rootView.getContext(),
         new WeatherLocationLoadedListener(zipcodeString, 
         rootView.getContext())).execute();
     
      return rootView;
   }
 
   private class WeatherLocationLoadedListener implements 
      LocationLoadedListener
   {
      private String zipcodeString;
      private Context context;
      
      public WeatherLocationLoadedListener(String zipcodeString, 
         Context context)
      {
         this.zipcodeString = zipcodeString;
         this.context = context;
      } 

      @Override
      public void onLocationLoaded(String cityString, String stateString,
         String countryString) 
      {
         if (cityString == null) 
         {
               Toast errorToast = Toast.makeText(context, 
               context.getResources().getString(R.string.null_data_toast), 
               Toast.LENGTH_LONG);
            errorToast.setGravity(Gravity.CENTER, 0, 0);
            errorToast.show();
            return; 
         } 
         
        
         locationTextView.setText(cityString + " " + stateString + ", " +
            zipcodeString + " " + countryString);
         
         new ReadFiveDayForecastTask(zipcodeString, 
            weatherForecastListener, 
            locationTextView.getContext()).execute();
      } 
   }
   
   FiveDayForecastLoadedListener weatherForecastListener = 
      new FiveDayForecastLoadedListener() 
   {
      @Override
      public void onForecastLoaded(DailyForecast[] forecasts) 
      {
           for (int i = 0; i < NUMBER_DAILY_FORECASTS; i++)
         {
      		 loadForecastIntoView(dailyForecastViews[i], forecasts[i]);    
         } 
      }
   };
   
   private void loadForecastIntoView(View view, 
      DailyForecast dailyForecast)
   {
      if (!FiveDayForecastFragment.this.isAdded()) 
      {
         return; 
      } 
   
      else if (dailyForecast == null || 
         dailyForecast.getIconBitmap() == null) 
      {
      
         Toast errorToast = Toast.makeText(view.getContext(), 
            view.getContext().getResources().getString(
            R.string.null_data_toast), Toast.LENGTH_LONG);
         errorToast.setGravity(Gravity.CENTER, 0, 0);
         errorToast.show();
         return; 
      }

      ImageView forecastImageView = (ImageView) view.findViewById(
         R.id.daily_forecast_bitmap);
      TextView dayOfWeekTextView = (TextView) view.findViewById(
         R.id.day_of_week);
      TextView descriptionTextView = (TextView) view.findViewById(
         R.id.daily_forecast_description);
      TextView highTemperatureTextView = (TextView) view.findViewById(
         R.id.high_temperature);
      TextView lowTemperatureTextView = (TextView) view.findViewById(
         R.id.low_temperature);
      
      forecastImageView.setImageBitmap(dailyForecast.getIconBitmap());
      dayOfWeekTextView.setText(dailyForecast.getDay());
      descriptionTextView.setText(dailyForecast.getDescription());
      highTemperatureTextView.setText(dailyForecast.getHighTemperature());
      lowTemperatureTextView.setText(dailyForecast.getLowTemperature());
   }
} 
