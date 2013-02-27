// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.stardroid.layers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.yoidles.android.bronze.R;
import com.google.android.stardroid.base.TimeConstants;
import com.google.android.stardroid.control.AstronomerModel;
import com.google.android.stardroid.control.AstronomerModelImpl;
import com.google.android.stardroid.renderer.RendererObjectManager.UpdateType;
import com.google.android.stardroid.source.AbstractAstronomicalSource;
import com.google.android.stardroid.source.AstronomicalSource;
import com.google.android.stardroid.source.PointSource;
import com.google.android.stardroid.source.Sources;
import com.google.android.stardroid.source.TextSource;
import com.google.android.stardroid.source.impl.PointSourceImpl;
import com.google.android.stardroid.source.impl.TextSourceImpl;
import com.google.android.stardroid.units.EarthMark;
import com.google.android.stardroid.units.GeocentricCoordinates;
import com.google.android.stardroid.units.LatLong;
import com.google.android.stardroid.units.Vector3;

/**
 * An implementation of the {@link AbstractFileBasedLayer} for displaying earth features
 * in the Renderer. 
 *
 * @author Brent Bryan
 * @author John Taylor
 * @author modified by Ray Phoenix
 */
public class EarthLayer extends AbstractFileBasedLayer {
	  private final AstronomerModel model;
	
	public EarthLayer(AstronomerModel model, AssetManager assetManager, Resources resources) {
	  	super(assetManager, resources, "earth.binary", true);
	    this.model = model;
  }

	@Override
	protected void initializeAstroSources(ArrayList<AstronomicalSource> sources) {
		EarthSource earthSource = new EarthSource(model, getResources());
		sources.add(earthSource);
	}

	@Override
  public int getLayerId() {
    return -100;
  }

	// TODO RLP http://code.google.com/p/protobuf/source/checkout    @Override
  public String getPreferenceId() {
    return "source_provider.7";
  }
  
  @Override
  protected int getLayerNameId() {
	return R.string.show_earth_pref;  // TODO(johntaylor): rename this Id
  }
  
  public static class EarthSource extends AbstractAstronomicalSource {
	    private static ArrayList<EarthMark> earthmarks = new ArrayList<EarthMark>();
	    
	    private static AstronomerModel model = null;

	    private long lastUpdateTimeMs = 0L;
	    private static final long UPDATE_FREQ_MS = 1L * TimeConstants.MILLISECONDS_PER_SECOND;
	    private static final ArrayList<String> markNames = new ArrayList<String>(); 
	    private static final ArrayList<TextSource> textSources = new ArrayList<TextSource>();
	    private static final ArrayList<PointSource> pointSources = new ArrayList<PointSource>();
    	final static String PREFS_NAME = "EarthMarkPreferences";
    	static int largestIndexIntoEarthMarks = 0;
    	static Context myContext; 
    	SharedPreferences earthMarkPreferences;
    	static SharedPreferences.Editor editor;

	    public EarthSource(AstronomerModel model, Resources res) {
	      EarthSource.model = model;
	      myContext = ((AstronomerModelImpl)model).getMainContext();
	      
	      // Context.MODE_WORLD_WRITEABLE = 2; I want to do this in case I want to make the EarthMarkPreferences file writable from the PC (would it be in any case?)
	      earthMarkPreferences = myContext.getSharedPreferences(PREFS_NAME, 2);
	      editor = earthMarkPreferences.edit();
	      List<Address> addresses = new ArrayList<Address>();
	      ArrayList<EarthMark> earthmarks = new ArrayList<EarthMark>();
	     Geocoder geocoder;
		geocoder = ((AstronomerModelImpl)model).getGeocoder();
	    while (geocoder == null) { 
	    	Log.d("RLP", "in EL, geocoder = " + geocoder + ", model = " + model);
	        try {
			   Thread.sleep(1000);
			} 
	        catch (InterruptedException e1) {
			}
		geocoder = ((AstronomerModelImpl)model).getGeocoder();
		}
	     Log.d("RLP", "in EL(2nd), geocoder = " + geocoder);
	     // Note: I finally gave up completely on any other form of persistent storage
	     // I was unable to use an XML file because I couldn't debug it in Eclipse. The phone's external storage was tied up by my computer.
	     // I'd love to use SQLite, but it seemed relatively complex. I may move to that later.
	     // I'm storing the initial earthmarks in a preference file called EarthMarkPreferences.
	     // It may eventually contain other info.
	       // if (earthMarkPreferences.getString("1", "Done").equals("Done")) {
	    	 makeInitialEarthMarkNames();
	       //   }
	     
	     //makeListFromPreferences(markNames);
	      
	     for (String markName: markNames) {
	          try {
	              addresses = geocoder.getFromLocationName(markName, 1);
	           } catch (Exception e) {
	            	@SuppressWarnings("unused")
	            	Integer a = 1;
	           }
	           Log.d("RLP", "RLP " + markName + ", addresses.size() = " + addresses.size()); 
	           if (addresses.size() == 0) {
	              @SuppressWarnings("unused")
	  			  Integer a = 1;
	           }
	           Address address = addresses.get(0);
	           Double latitude = address.getLatitude();
	           Double longitude = address.getLongitude();
	           LatLong latlong = new LatLong(latitude, longitude);
	           earthmarks.add(new EarthMark(markName, latlong));
	      }
	     earthmarks.add(new EarthMark("1", new LatLong( 20.87866351,-156.4557744)));
	     earthmarks.add(new EarthMark("2", new LatLong( 21.39351424922648, -157.7421590196418)));
	     earthmarks.add(new EarthMark("3", new LatLong( 44.01752302922384, -107.944641401221)));
	     earthmarks.add(new EarthMark("4", new LatLong( 39.75505525,-105.0243276)));
	     earthmarks.add(new EarthMark("5", new LatLong(39.05059567,-95.72568486)));
	     earthmarks.add(new EarthMark("6", new LatLong( 33.99815615269141, -81.01713030535551)));
	     earthmarks.add(new EarthMark("7", new LatLong( 38.901831,-99.31793)));
	     earthmarks.add(new EarthMark("8", new LatLong(39.40917124,-81.43013987)));
	     earthmarks.add(new EarthMark("9", new LatLong(36.04184726,-115.1716433)));
	     earthmarks.add(new EarthMark("10", new LatLong(41.42352676,-81.73923164)));
	     earthmarks.add(new EarthMark("11", new LatLong(35.61987302,-82.55487666)));
	     earthmarks.add(new EarthMark("12", new LatLong(39.86911213,-84.88569952)));
	     earthmarks.add(new EarthMark("13", new LatLong(47.66462934,-122.3147947)));
	     earthmarks.add(new EarthMark("14", new LatLong(35.50069666,-80.86036361)));
	     earthmarks.add(new EarthMark("15", new LatLong(45.99775026,-112.5378007)));
	     earthmarks.add(new EarthMark("16", new LatLong(43.650895,-93.36146577)));
	     earthmarks.add(new EarthMark("17", new LatLong(48.84669675,-122.5930136)));
	     earthmarks.add(new EarthMark("18", new LatLong(49.32360679,-123.122304)));
	     earthmarks.add(new EarthMark("19", new LatLong(49.72115923,-123.149291)));
	     earthmarks.add(new EarthMark("20", new LatLong(39.62767071,-106.062851)));
	     earthmarks.add(new EarthMark("21", new LatLong(45.51983189,-122.6788756)));
	     earthmarks.add(new EarthMark("22", new LatLong(39.76963034,-104.898627)));
	     earthmarks.add(new EarthMark("23", new LatLong(49.28222376,-123.1077451)));
	     earthmarks.add(new EarthMark("24", new LatLong(29.67623146,-95.28981172)));
	     earthmarks.add(new EarthMark("25", new LatLong(39.7160165,-82.57948268)));
	     earthmarks.add(new EarthMark("26", new LatLong(30.419425,-97.747846)));
	     earthmarks.add(new EarthMark("27", new LatLong(39.35434557,-82.97494981)));
	     earthmarks.add(new EarthMark("28", new LatLong(34.12809288,-116.3951846)));
	     earthmarks.add(new EarthMark("29", new LatLong(37.63857384,-118.9654085)));
	     earthmarks.add(new EarthMark("30", new LatLong(33.98426422,-118.4432829)));
	     
//	     markNames.add("Walmart Kahului, HI");
//	     markNames.add("Safeway Denver, CO");
//	     markNames.add("Hays, KS 67601");
//	     
//	     
//	     markNames.add("x");
//	     markNames.add("xx");
//	     markNames.add("xxx");
//	     markNames.add("xxxx");
//	     markNames.add("xxxxx");
//	     markNames.add("xxxxxx");
//	     markNames.add("xxxxxxx");
	     
	   /*   earthmarks.add(new EarthMark("#1 Bronze Crayon in Kihului, HI", new LatLong(-156.4557743814053,20.87866351101885)));
	      earthmarks.add(new EarthMark("#2 Bronze Crayon in Kailua, HI", new LatLong(-157.7421590196418,21.39351424922648)));
	      earthmarks.add(new EarthMark("#3 Bronze Crayon in Worland, WY", new LatLong(-107.944641401221,44.01752302922384)));
	      earthmarks.add(new EarthMark("#4 Bronze Crayon in Denver, CO", new LatLong(-105.0243275884506,39.75505524857422)));
	      earthmarks.add(new EarthMark("#5 Bronze Crayon in Topeka, KS", new LatLong(-95.72568485927653,39.05059567097005)));
	      earthmarks.add(new EarthMark("#6 Bronze Crayon in Columbia, SC", new LatLong(-81.01713030535551,33.99815615269141)));
	      
	      markNames.add("#1 Bronze Crayon in Kihului, HI"); //These markNames are items to be shown in the Create Cities and Landmarks menu list
	      markNames.add("#2 Bronze Crayon in Kailua, HI");
	      markNames.add("#3 Bronze Crayon in Worland, WY");
	      markNames.add("#4 Bronze Crayon in Denver, CO");
	      markNames.add("#5 Bronze Crayon in Topeka, KS");
	      markNames.add("#6 Bronze Crayon in Columbia, SC");*/
          EarthMark viewpoint = earthmarks.get(5);
          model.setEarthMarkViewpointName(viewpoint.getName());
		  ((AstronomerModelImpl)model).setEarthMarks(earthmarks);
		  ((AstronomerModelImpl)model).setMarkNames(markNames);
	      makeSourcesForViewpoint(model, earthmarks, viewpoint);
	    }

		public static void makeSourcesForViewpoint(AstronomerModel model,
			ArrayList<EarthMark> earthmarks, EarthMark viewpoint) {
		    int redColorForText = Color.argb(20, 255, 0 , 0);
			int redColorForPoint = Color.argb(20, 0, 0 , 255);
		  	for (EarthMark targetMark: earthmarks) {
			    if (!targetMark.getName().equals(model.getEarthMarkViewpointName())) {
			      	GeocentricCoordinates geoCoord = viewpoint.findPointOnUnitSphereToViewMark(model.getTime(), targetMark);
			      	TextSourceImpl ts =	new TextSourceImpl(geoCoord, targetMark.getName(), redColorForText );
			        textSources.add(ts);
			        pointSources.add(new PointSourceImpl(geoCoord, redColorForPoint, 5));
			        Log.w("RLP", "textSource = " + ts.getText() + " " + ts.getLocation());
			    }
			  	Log.d("RLP", "RLP mark = " + targetMark.toString());
			}
			((AstronomerModelImpl)model).setEarthMarkTextSources(textSources);
			((AstronomerModelImpl)model).setEarthMarkPointSources(pointSources);
		}

		public static void updateSourcesForViewpoint(AstronomerModel model,
					ArrayList<EarthMark> earthmarks, EarthMark viewpoint) {
			int i = 0;
			for (EarthMark targetMark: earthmarks) {
			    if (!targetMark.getName().equals(model.getEarthMarkViewpointName())) {
			      	GeocentricCoordinates geoCoord = viewpoint.findPointOnUnitSphereToViewMark(model.getTime(), targetMark);
			      	TextSource ts =	textSources.get(i);
			        Log.w("RLP", "textSource entry = " + ts.toString() + ", geoCoord = " + geoCoord.toString());
					ts.setText(targetMark.getName());
					ts.updateLocationFromVector3(geoCoord);
			        pointSources.get(i).updateLocationFromVector3(geoCoord);
			        Log.w("RLP", "textSource changed = " + ts.toString());
			        i++;
			    }
			  	Log.d("RLP", "RLP mark = " + targetMark.toString());
			}
			((AstronomerModelImpl)model).setEarthMarks(earthmarks);
		}

		public static void updateSourcesForViewpoint(AstronomerModel model,
				ArrayList<EarthMark> earthMarks, String viewpointName) {
			EarthMark viewpoint = findEarthMarkForName(earthMarks,
					viewpointName);
			if (viewpoint == null) {
				Log.w("RLP", "We didn't find the viewpoint!");
			}
			updateSourcesForViewpoint(model, earthMarks, viewpoint);
		}

		private static EarthMark findEarthMarkForName(
				ArrayList<EarthMark> earthMarks, String viewpointName) {
			EarthMark viewpoint = null;
			for (EarthMark viewpointQ: earthMarks) {
				if (viewpointQ.getName().equals(viewpointName)) {
					Log.w("RLP", "We found the viewpoint, and it is: " + viewpointQ + ", based on " + viewpointName + viewpointQ.getName());
					viewpoint = viewpointQ;
					break;
				}
			}
			return viewpoint;
		}

		public static int getPositionOfMarkName(String markName) {
			int position = 0;
			ArrayList<EarthMark> earthMarks = model.getEarthMarks();
			for (EarthMark viewpointQ: earthMarks) {
				if (viewpointQ.getName().equals(markName)) {
					Log.w("RLP", "We found the viewpoint, and its position is: " + Integer.toString(position));
					break;
				}
				position++;
			}
			return position;
		}

	    private void makeListFromPreferences(List<String> earthmarkNames) {
	    	int key = 1;
	    	boolean done = false;
	    	largestIndexIntoEarthMarks = earthMarkPreferences.getInt("largestIndexIntoEarthMarks", 0);
	    	while (!done) {
	    		String markName = (earthMarkPreferences.getString(Integer.toString(key), "Done"));
	    		if (!markName.equals("Done")) {
	    			earthmarkNames.add(markName);
	    			key++;
	    		}
	    		// ignore empty slots that occur before we're done
	    		else if (key >= largestIndexIntoEarthMarks) {
	    			done = true;
	    		}
	    	}
	  	}
	    
void updateCoords(Date modelTime) {
	try {
	    earthmarks = model.getEarthMarks();
        int i = 0;
	    for (EarthMark toMark: earthmarks) {
	    	if (!toMark.getName().equals(model.getEarthMarkViewpointName())) {
	    	    adjustLocationsForCurrentTime(i, toMark);
	    	    i++;
	    	}
	    }
	} catch (Exception e) {
	    Log.d("RLP", "RLP we had a problem here");
	}	
}

private void adjustLocationsForCurrentTime(int i, EarthMark toMark) {
	// in the following: the earth rotates 2PI per day and 1 day = 1000 * 60 * 1440 milliseconds       (1000 milliseconds/second * 60 seconds/minute * 1440 minutes/day)  
	  double newAngle = toMark.getAngle() - 2 * Math.PI * (System.currentTimeMillis() - toMark.getStarttime()) / (1000 * 60 * 1440 ); 
	  double radius = toMark.getRadius();
	  float newFloatX = (float)(Math.sin(newAngle) * radius);
	  float newFloatY = (float) (Math.cos(newAngle) * radius);
	  TextSource textSourceToBeUpdated = textSources.get(i);
//	  Log.d("RLP", String.format("label for %s, and markname is %s", textSourceToBeUpdated.toString(), toMark.getName()));
	  float zRemainsConstant = textSourceToBeUpdated.getLocation().z;
	  Vector3 newCoordinates = new Vector3(newFloatX, newFloatY, zRemainsConstant);
	  textSourceToBeUpdated.updateLocationFromVector3(newCoordinates);  
	  pointSources.get(i).updateLocationFromVector3(newCoordinates);
}

	    @Override
	    public Sources initialize() {
	      updateCoords(model.getTime());
	      return this;
	    }

	    @Override
	    public EnumSet<UpdateType> update() {
	      EnumSet<UpdateType> updateTypes = EnumSet.noneOf(UpdateType.class);
	      Date modelTime = model.getTime();
	      if (Math.abs(modelTime.getTime() - lastUpdateTimeMs) > 12 * UPDATE_FREQ_MS) {
	    	lastUpdateTimeMs = modelTime.getTime();
      	    updateCoords(modelTime);
	    	updateTypes.add(UpdateType.UpdatePositions);
	    	updateTypes.add(UpdateType.Reset);
	      }
	      return updateTypes;
	    }

	    private void makeInitialEarthMarkNames() {
	  	    editor.putString("1", "San Diego, CA");
	  	    editor.putString("2", "San Francisco, CA");
	  	    editor.putString("3", "Seattle, WA");
	  	    editor.putString("4", "Mexico City, Mexico");
	  	    editor.putString("5", "Los Angeles, CA");
	  	    editor.putString("6", "Tijuana, Mexico");
	   	    editor.putString("7", "Boston, MA");
	        editor.putString("8", "Rio De Janeiro, Brazil");
	        editor.putString("9", "Mount Everest");
	        editor.putString("10", "Timbuktu");
	        editor.putString("11", "Paris");
	        editor.putString("12", "Berlin");
	        editor.putString("13", "Washington, D.C.");
	        editor.putString("14", "Phoenix, AZ");
	        editor.putString("15", "Fort Sumter");
	        editor.putInt("largestIndexIntoEarthMarks", 15);
	        editor.commit();
	    	largestIndexIntoEarthMarks = 15;
	    	File f = myContext.getDatabasePath("MyPrefsFile.xml");
	    	if (f != null)
	    	    Log.i("RLP", f.getAbsolutePath());	    	
	    	String city1 = earthMarkPreferences.getString("1", "any string");
	    	Log.d("RLP", "Stored and fetched " + city1);
	    } 	
	    
	    @Override
	    public List<? extends TextSource> getLabels() {
	    	Log.d("RLP", "RLP the first label is: " + textSources.get(0).getText());
	    	return textSources;
	    }

	    @Override
	    public List<? extends PointSource> getPoints() {
	      return pointSources;
	    }

	    public static void addToMarkNamesIfNew(String markName, double latitude, double longitude) {
	    	if (nameIsNew(markName)) { 
		    	largestIndexIntoEarthMarks++;
		    	editor.putInt("largestIndexIntoEarthMarks", largestIndexIntoEarthMarks);
		    	editor.putString(Integer.toString(largestIndexIntoEarthMarks), markName);
		    	editor.commit();
		    	markNames.add(markName);
		    	earthmarks.add(new EarthMark(markName, new LatLong(latitude, longitude)));
		    	textSources.add(new TextSourceImpl((float)latitude, (float)longitude, markName, 5));
		// TODO RLP fix color here, 0 to redforpoint. For this, I need to know the best way to create a global constant in a package (g)
		    	pointSources.add(new PointSourceImpl((float)latitude, (float)longitude, 0, 5));
	    	}
	    }

		// TODO RLP It might be wise to check lat and long, as some unequal names map to the same place
		private static Boolean nameIsNew(String targetMarkName) {
			for (String markName: markNames) {
				if (markName.equals(targetMarkName)) {
					return false;
				}
			}
			return true;
		}

		public static void setNewLocation(SharedPreferences preferences, ArrayList<EarthMark> earthMarks,
				String viewpointName) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
            Editor sharededitor = sharedPreferences.edit();
            EarthMark selectedEarthmark = findEarthMarkForName(earthMarks, viewpointName);
            sharededitor.putString("latitude", Float.toString(selectedEarthmark.getLatitude()));
            sharededitor.putString("longitude", Float.toString(selectedEarthmark.getLongitude()));
            sharededitor.putString("location", viewpointName);
            sharededitor.commit();
            Editor editor = sharedPreferences.edit();
            editor.putString("latitude", Float.toString(selectedEarthmark.getLatitude()));
            editor.putString("longitude", Float.toString(selectedEarthmark.getLongitude()));
            editor.putString("location", viewpointName);
            editor.commit();
		}
	  }

}

