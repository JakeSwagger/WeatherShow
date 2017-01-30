package com.appl.weathershow;

import com.appl.weathershow.R;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddCityDialogFragment extends DialogFragment 
   implements OnClickListener 
{
   public interface DialogFinishedListener 
   {
           void onDialogFinished(String zipcodeString, boolean preferred);
   }  
   
   EditText addCityEditText;
   CheckBox addCityCheckBox; 
   
    @Override
   public void onCreate(Bundle bundle)
   {
      super.onCreate(bundle);
      
    
      this.setCancelable(true); 
   } 
      
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, 
      Bundle argumentsBundle)
   {
       View rootView = inflater.inflate(R.layout.add_city_dialog, container, 
         false);
        addCityEditText = (EditText) rootView.findViewById(
         R.id.add_city_edit_text);
        addCityCheckBox = (CheckBox) rootView.findViewById(
         R.id.add_city_checkbox);
      
      if (argumentsBundle != null)
      {
         addCityEditText.setText(argumentsBundle.getString(
            getResources().getString(
               R.string.add_city_dialog_bundle_key)));
      } 
    
      getDialog().setTitle(R.string.add_city_dialog_title); 
      
       Button okButton = (Button) rootView.findViewById(
         R.id.add_city_button);
      okButton.setOnClickListener(this);
      return rootView; 
   } 
  
   @Override
   public void onSaveInstanceState(Bundle argumentsBundle)
   {
      argumentsBundle.putCharSequence(getResources().getString(
         R.string.add_city_dialog_bundle_key), 
         addCityEditText.getText().toString());
      super.onSaveInstanceState(argumentsBundle);
   } 

   @Override
   public void onClick(View clickedView) 
   {
      if (clickedView.getId() == R.id.add_city_button)
      {
         DialogFinishedListener listener = 
            (DialogFinishedListener) getActivity();
         listener.onDialogFinished(addCityEditText.getText().toString(), 
            addCityCheckBox.isChecked() );
         dismiss();
      }
   } 
} 