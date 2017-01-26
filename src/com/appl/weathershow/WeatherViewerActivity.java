
package com.appl.weathershow;

import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.appl.weathershow.AddCityDialogFragment.DialogFinishedListener;
import com.appl.weathershow.CitiesFragment.CitiesListChangeListener;
import com.appl.weathershow.ReadLocationTask.LocationLoadedListener;

public class WeatherViewerActivity extends Activity implements 
   DialogFinishedListener
{
   public static final String WIDGET_UPDATE_BROADCAST_ACTION = 
      "com.appl.weathershow.UPDATE_WIDGET";
   
   private static final int BROADCAST_DELAY = 10000;
   
   private static final int CURRENT_CONDITIONS_TAB = 0;
   
   public static final String PREFERRED_CITY_NAME_KEY = 
      "preferred_city_name";
   public static final String PREFERRED_CITY_ZIPCODE_KEY = 
      "preferred_city_zipcode";
   public static final String SHARED_PREFERENCES_NAME = 
      "weather_viewer_shared_preferences";
   private static final String CURRENT_TAB_KEY = "current_tab";
   private static final String LAST_SELECTED_KEY = "last_selected";
   
   private int currentTab;
   private String lastSelectedCity; 
   private SharedPreferences weatherSharedPreferences;
   
   private Map<String, String> favoriteCitiesMap;
   private CitiesFragment listCitiesFragment;
   private Handler weatherHandler;
   

   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState); 
      setContentView(R.layout.main);
      
  
      listCitiesFragment = (CitiesFragment) 
         getFragmentManager().findFragmentById(R.id.cities);
      
      listCitiesFragment.setCitiesListChangeListener(
         citiesListChangeListener);
      

      favoriteCitiesMap = new HashMap<String, String>();
      
      weatherHandler = new Handler();
      
      weatherSharedPreferences = getSharedPreferences(
         SHARED_PREFERENCES_NAME, MODE_PRIVATE);

      setupTabs(); 
   } 
   
   
   @Override
   public void onSaveInstanceState(Bundle savedInstanceStateBundle) 
   {
      savedInstanceStateBundle.putInt(CURRENT_TAB_KEY, currentTab);
      savedInstanceStateBundle.putString(LAST_SELECTED_KEY, 
         lastSelectedCity); 
      super.onSaveInstanceState(savedInstanceStateBundle);
   } 
   

   @Override
   public void onRestoreInstanceState(Bundle savedInstanceStateBundle) 
   {
      super.onRestoreInstanceState(savedInstanceStateBundle);
     
      currentTab = savedInstanceStateBundle.getInt(CURRENT_TAB_KEY);
      lastSelectedCity = savedInstanceStateBundle.getString(
         LAST_SELECTED_KEY); 
   } 

   @Override
   public void onResume() 
   {
      super.onResume();
      
      if (favoriteCitiesMap.isEmpty()) 
      {
         loadSavedCities(); 
      } 
      
    
      if (favoriteCitiesMap.isEmpty()) 
      {
         addSampleCities(); 
      }
      
     
      getActionBar().selectTab(getActionBar().getTabAt(currentTab));
      loadSelectedForecast();
   } 
   
   
   private CitiesListChangeListener citiesListChangeListener = 
      new CitiesListChangeListener()
   {
   
      @Override
     public void onSelectedCityChanged(String cityNameString) 
      {
         
         selectForecast(cityNameString);     
     } 
 
     @Override
     public void onPreferredCityChanged(String cityNameString) 
     {
       
        setPreferred(cityNameString);   
     } 
   }; 
   
  
   private void loadSelectedForecast()
   {
      if (lastSelectedCity != null) 
      {
         selectForecast(lastSelectedCity); 
      }
      else
      {
           String cityNameString = weatherSharedPreferences.getString(
            PREFERRED_CITY_NAME_KEY, getResources().getString(
            R.string.default_zipcode));
         selectForecast(cityNameString); // load preferred city's forecast
      } 
   } 
   
   
   public void setPreferred(String cityNameString)
   {
      
      String cityZipcodeString = favoriteCitiesMap.get(cityNameString);
      Editor preferredCityEditor = weatherSharedPreferences.edit();
      preferredCityEditor.putString(PREFERRED_CITY_NAME_KEY, 
         cityNameString);
      preferredCityEditor.putString(PREFERRED_CITY_ZIPCODE_KEY, 
         cityZipcodeString);
      preferredCityEditor.apply();
      lastSelectedCity = null; 
      loadSelectedForecast();
     
      final Intent updateWidgetIntent = new Intent(
         WIDGET_UPDATE_BROADCAST_ACTION);
    
      weatherHandler.postDelayed(new Runnable()
      {
         @Override
         public void run() 
         {
            sendBroadcast(updateWidgetIntent); 
         }
      }, BROADCAST_DELAY);
   }
   
   private void loadSavedCities()
   {
      Map<String, ?> citiesMap = weatherSharedPreferences.getAll();

      for(String cityString : citiesMap.keySet())
      {
    	   if (!(cityString.equals(PREFERRED_CITY_NAME_KEY) || 
            cityString.equals(PREFERRED_CITY_ZIPCODE_KEY)))
         {
            addCity(cityString, (String) citiesMap.get(cityString), false);
         }
      }
   }
   
   private void addSampleCities() 
   {
      
      String[] sampleCityNamesArray = getResources().getStringArray(
         R.array.default_city_namesw);
      
      String[] sampleCityZipcodesArray = getResources().getStringArray(
         R.array.default_city_zipcodesw);

      for (int i = 0; i < sampleCityNamesArray.length; i++)
      {
        if (i == 0) 
         {
            setPreferred(sampleCityNamesArray[i]);
         } 
        
         addCity(sampleCityNamesArray[i], sampleCityZipcodesArray[i], 
            false);   
      }
   } 
   
  
   public void addCity(String city, String zipcode, boolean select) 
   {
      favoriteCitiesMap.put(city, zipcode); 
      listCitiesFragment.addCity(city, select);
      Editor preferenceEditor = weatherSharedPreferences.edit();
      preferenceEditor.putString(city, zipcode);
      preferenceEditor.apply();
   } 
   
  
   public void selectForecast(String name) 
   {      
      lastSelectedCity = name; 
      String zipcodeString = favoriteCitiesMap.get(name); 
      if (zipcodeString == null) 
      {
         return;
      }
      
      ForecastFragment currentForecastFragment = (ForecastFragment) 
         getFragmentManager().findFragmentById(R.id.forecast_replacer);
      
      if (currentForecastFragment == null || 
         !(currentForecastFragment.getZipcode().equals(zipcodeString) && 
         correctTab(currentForecastFragment)))
      {
            if (currentTab == CURRENT_CONDITIONS_TAB)
         {
            
            currentForecastFragment = SingleForecastFragment.newInstance(
               zipcodeString);
         } 
         else
         {
          
            currentForecastFragment = FiveDayForecastFragment.newInstance(
               zipcodeString);
         }
         
         FragmentTransaction forecastFragmentTransaction = 
            getFragmentManager().beginTransaction();

         forecastFragmentTransaction.setTransition(
            FragmentTransaction.TRANSIT_FRAGMENT_FADE);
          
         forecastFragmentTransaction.replace(R.id.forecast_replacer, 
            currentForecastFragment);
         
         forecastFragmentTransaction.commit(); 
      } 
   } 
   
   private boolean correctTab(ForecastFragment forecastFragment)
   {
        if (currentTab == CURRENT_CONDITIONS_TAB)
      {
         
         return (forecastFragment instanceof SingleForecastFragment);
      } 
      else
      {
         return (forecastFragment instanceof FiveDayForecastFragment); 
      }
   }
 
   private void selectTab(int position)
   {
      currentTab = position; 
      loadSelectedForecast();
   } 
  
   @Override
   public boolean onCreateOptionsMenu(Menu menu) 
   {
      super.onCreateOptionsMenu(menu); 
      MenuInflater inflater = getMenuInflater(); 
     
      inflater.inflate(R.menu.actionmenu, menu); 
      return true; 
   } 
   
  
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
     
      if (item.getItemId() == R.id.add_city_item)
      {
         showAddCityDialog(); 
         return true; 
      } 
      
      return false; 
   } 
   
   private void showAddCityDialog()
   {
     
      AddCityDialogFragment newAddCityDialogFragment = 
         new AddCityDialogFragment();
      
     FragmentManager thisFragmentManager = getFragmentManager();
      
      FragmentTransaction addCityFragmentTransition = 
         thisFragmentManager.beginTransaction();
      
    newAddCityDialogFragment.show(addCityFragmentTransition, "");
   } 
 
   @Override
   public void onDialogFinished(String zipcodeString, boolean preferred) 
   {
    getCityNameFromZipcode(zipcodeString, preferred);
   } 
   
   private void getCityNameFromZipcode(String zipcodeString, 
      boolean preferred)
   {
	
      if (favoriteCitiesMap.containsValue(zipcodeString))
      {
    	    Toast errorToast = Toast.makeText(WeatherViewerActivity.this, 
            WeatherViewerActivity.this.getResources().getString(
            R.string.duplicate_zipcode_error), Toast.LENGTH_LONG);
         errorToast.setGravity(Gravity.CENTER, 0, 0);
         errorToast.show();      
      } 
      else 
      {
            new ReadLocationTask(zipcodeString, this, 
            new CityNameLocationLoadedListener(zipcodeString, preferred)).
            execute();
      } 
   } 
   
   private class CityNameLocationLoadedListener implements 
      LocationLoadedListener
   {
      private String zipcodeString; 
      private boolean preferred;
    
      public CityNameLocationLoadedListener(String zipcodeString, 
         boolean preferred)
      {
         this.zipcodeString = zipcodeString;
         this.preferred = preferred;
      } 
      
      @Override
      public void onLocationLoaded(String cityString, String stateString,
            String countryString) 
      {
         
         if (cityString != null)
         {
            addCity(cityString, zipcodeString, !preferred);
            
            if (preferred) 
            {
               
               setPreferred(cityString);
            } 
         } 
         else 
         {
            
            Toast zipcodeToast = Toast.makeText(WeatherViewerActivity.this, 
               WeatherViewerActivity.this.getResources().getString(
               R.string.invalid_zipcode_error), Toast.LENGTH_LONG);
            zipcodeToast.setGravity(Gravity.CENTER, 0, 0);
            zipcodeToast.show(); 
         } 
      } 
   }
  
   private void setupTabs()
   {
      ActionBar weatherActionBar = getActionBar();
      
     
      weatherActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
      
      Tab currentConditionsTab = weatherActionBar.newTab();
      
      currentConditionsTab.setText(getResources().getString(
         R.string.current_conditions));
      
      
      currentConditionsTab.setTabListener(weatherTabListener);
      weatherActionBar.addTab(currentConditionsTab);
   
      Tab fiveDayForecastTab = weatherActionBar.newTab();
      fiveDayForecastTab.setText(getResources().getString(
         R.string.five_day_forecast));
      fiveDayForecastTab.setTabListener(weatherTabListener);
      weatherActionBar.addTab(fiveDayForecastTab);
      
      currentTab = CURRENT_CONDITIONS_TAB; 
   } 
   
 
   TabListener weatherTabListener = new TabListener()
   {
    
      @Override
      public void onTabReselected(Tab arg0, FragmentTransaction arg1) 
      {
      }
      @Override
      public void onTabSelected(Tab tab, FragmentTransaction arg1) 
      {
       
         selectTab(tab.getPosition());
      } 

      @Override
      public void onTabUnselected(Tab arg0, FragmentTransaction arg1) 
      {
      } 
   }; 
}