package com.google.android.stardroid.views;

import java.util.ArrayList;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.content.SharedPreferences;

import com.yoidles.android.bronze.R;
import com.google.android.stardroid.activities.DynamicStarMapActivity;
import com.google.android.stardroid.control.AstronomerModelImpl;
import com.google.android.stardroid.layers.EarthLayer;
import com.google.android.stardroid.units.EarthMark;
import com.google.android.stardroid.util.MiscUtil;

/**
 * Implementation of the time travel dialog.
 *
 * @author Dominic Widdows
 * @author John Taylor
 */
public class EarthmarksDialog extends Dialog {

	  private static final String TAG = MiscUtil.getTag(EarthmarksDialog.class);
	  private Spinner earthmarks_spinner;
	  private AstronomerModelImpl model;
	  private boolean seenFirstSelect = false;

	  public EarthmarksDialog(final DynamicStarMapActivity parentActivity,
	                          final AstronomerModelImpl model) {
	    super(parentActivity);
	    this.model = model;
	  }

	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    setContentView(R.layout.earthmark_dialog);
	    // Assumes that the dialog's title should be the same as the menu option.
	    setTitle(R.string.menu_earth);
	    // Capture and wire up the buttons
	    Button addLandmarkButton = (Button) findViewById(R.id.add_landmark);
	    addLandmarkButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
// TODO RLP nothing to be done here yet... :-)
	        }
	      });

	    Button cancelButton = (Button) findViewById(R.id.earthmarksCancel);
	    cancelButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {	        	
	          hide();
	        }
	      });

	    earthmarks_spinner = (Spinner) findViewById(R.id.earthmarks_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, ((AstronomerModelImpl)model).getMarkNames());
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    earthmarks_spinner.setAdapter(adapter);
	    earthmarks_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	      // The callback received when the user selects a menu item.
	      @Override
	      public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long rowid) {
// TODO RLP set the current earthmarks viewpoint to the selected item if possible	
	    	  ArrayList<EarthMark> earthMarks = model.getEarthMarks();
			  String viewpointName = (String) arg0.getItemAtPosition(position);
	    	  model.setEarthMarkViewpointName(viewpointName);
	    	  SharedPreferences preferences = arg1.getContext().getSharedPreferences(viewpointName, 1);
	    	  if (!seenFirstSelect) {
	    		  Log.w(TAG, "This is the first select. viewpoint = " + viewpointName);
	    		  seenFirstSelect = true;
	    	  }
	    	  else {
	    		  hide();
	    		  EarthLayer.EarthSource.setNewLocation(preferences, earthMarks, viewpointName);
	    		  EarthLayer.EarthSource.updateSourcesForViewpoint(model, earthMarks, viewpointName);
	    	  }
	      }
	    	 	    	 
	      @Override
	      public void onNothingSelected(AdapterView<?> arg0) {
	        // Do nothing in this case.
	      }
	    });

	  }
}
