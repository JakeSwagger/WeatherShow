package com.appl.weathershow;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CitiesFragment extends ListFragment 
{
   private int currentCityIndex; 
  private static final String CURRENT_CITY_KEY = "current_city";

   public ArrayList<String> citiesArrayList; 
   private CitiesListChangeListener citiesListChangeListener; 
   private ArrayAdapter<String> citiesArrayAdapter;
   
   public interface CitiesListChangeListener 
   {
  public void onSelectedCityChanged(String cityNameString);
      
      public void onPreferredCityChanged(String cityNameString);
   } 
      
   @Override
   public void onActivityCreated(Bundle savedInstanceStateBundle) 
   {
     super.onActivityCreated(savedInstanceStateBundle);
      
     if (savedInstanceStateBundle != null) 
     {
        currentCityIndex = savedInstanceStateBundle.getInt(
           CURRENT_CITY_KEY);
     }

     citiesArrayList = new ArrayList<String>(); 

     setListAdapter(new CitiesArrayAdapter<String>(getActivity(), 
        R.layout.city_list_item, citiesArrayList)); 
   ListView thisListView = getListView(); 
     citiesArrayAdapter = (ArrayAdapter<String>)getListAdapter();
     thisListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
     thisListView.setBackgroundColor(Color.WHITE); 
     thisListView.setOnItemLongClickListener(citiesOnItemLongClickListener);
   } 
   
   public void setCitiesListChangeListener(
      CitiesListChangeListener listener)
   {
      citiesListChangeListener = listener; 
   }    
     private class CitiesArrayAdapter<T> extends ArrayAdapter<String> 
   {
      private Context context; 
      public CitiesArrayAdapter(Context context, int textViewResourceId, 
         List<String> objects) 
      {
         super(context, textViewResourceId, objects);
         this.context = context;
      } 
  @Override
      public View getView(int position, View convertView, ViewGroup parent) 
      {
       
         TextView listItemTextView = (TextView) 
            super.getView(position, convertView, parent);
         

         if (isPreferredCity(listItemTextView.getText().toString()))
         {
                       listItemTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 
               android.R.drawable.btn_star_big_on, 0);
         } 
         else 
         {
                       listItemTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 
               0, 0);
         } 
         return listItemTextView;
      }
      private boolean isPreferredCity(String cityString)
      {
           SharedPreferences preferredCitySharedPreferences = 
            context.getSharedPreferences(
            WeatherViewerActivity.SHARED_PREFERENCES_NAME, 
            Context.MODE_PRIVATE);
           return cityString.equals(preferredCitySharedPreferences.getString(
            WeatherViewerActivity.PREFERRED_CITY_NAME_KEY, null));
      }
   } 
   
   private OnItemLongClickListener citiesOnItemLongClickListener = 
      new OnItemLongClickListener() 
   {
      @Override
      public boolean onItemLongClick(AdapterView<?> listView, View view, 
         int arg2, long arg3) 
      {
    final Context context = view.getContext();
         
         final Resources resources = context.getResources();
         final String cityNameString = 
            ((TextView) view).getText().toString();
    
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
                 builder.setMessage(resources.getString(
            R.string.city_dialog_message_prefix) + cityNameString + 
            resources.getString(R.string.city_dialog_message_postfix));
         
         builder.setPositiveButton(resources.getString(
            R.string.city_dialog_preferred), 
            new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialog, int which) 
               {
                  citiesListChangeListener.onPreferredCityChanged(
                     cityNameString);
                  citiesArrayAdapter.notifyDataSetChanged();
               }
           }); 
     builder.setNeutralButton(resources.getString(
            R.string.city_dialog_delete), 
            new DialogInterface.OnClickListener() 
            {
       public void onClick(DialogInterface dialog, int id) 
               {
                  
                  if (citiesArrayAdapter.getCount() == 1)
                  {
                     
                     Toast lastCityToast = 
                        Toast.makeText(context, resources.getString(
                        R.string.last_city_warning), Toast.LENGTH_LONG);
                     lastCityToast.setGravity(Gravity.CENTER, 0, 0);
                     lastCityToast.show();
                     return; 
                  }
                  
                 
                  citiesArrayAdapter.remove(cityNameString); 
                  
                  SharedPreferences sharedPreferences = 
                     context.getSharedPreferences(
                        WeatherViewerActivity.SHARED_PREFERENCES_NAME, 
                 Context.MODE_PRIVATE);
                 Editor preferencesEditor = sharedPreferences.edit();
                  preferencesEditor.remove(cityNameString);
                  preferencesEditor.apply();
                  
                 
                  String preferredCityString = 
                     sharedPreferences.getString(
                        WeatherViewerActivity.PREFERRED_CITY_NAME_KEY, 
                        resources.getString(R.string.default_zipcode));
                
                  if (cityNameString.equals(preferredCityString))
                  {
                   
                     citiesListChangeListener.onPreferredCityChanged(
                        citiesArrayList.get(0));
                  }
                  else if (cityNameString.equals(citiesArrayList.get(
                     currentCityIndex)))
                  {
                 
                    citiesListChangeListener.onSelectedCityChanged(
                        preferredCityString); 
                  } 
               } 
            }); 
          builder.setNegativeButton(resources.getString(
            R.string.city_dialog_cancel), 
            new DialogInterface.OnClickListener() 
            {
             public void onClick(DialogInterface dialog, int id) 
               {
                  dialog.cancel(); 
               } 
            });
            
         builder.create().show();     
         return true;
      } 
   }; 
   
   @Override 
   public void onSaveInstanceState(Bundle outStateBundle) 
   {
      super.onSaveInstanceState(outStateBundle);
      outStateBundle.putInt(CURRENT_CITY_KEY, currentCityIndex);
   }   
   public void addCity(String cityNameString, boolean select) 
   {
      citiesArrayAdapter.add(cityNameString);
      citiesArrayAdapter.sort(String.CASE_INSENSITIVE_ORDER);
     
      if (select) 
      {
         
         citiesListChangeListener.onSelectedCityChanged(cityNameString);
      } 
   }
   
   @Override
   public void onListItemClick(ListView l, View v, int position, long id) 
   {
           citiesListChangeListener.onSelectedCityChanged(((TextView)v).
         getText().toString());
      currentCityIndex = position; 
   } 
} 