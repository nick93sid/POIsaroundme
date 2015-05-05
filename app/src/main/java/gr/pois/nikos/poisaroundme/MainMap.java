package gr.pois.nikos.poisaroundme;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainMap extends FragmentActivity implements GoogleMap.OnInfoWindowClickListener {
    private int defaultMarker =  R.drawable.defaultmark;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location outerLocation = null; //ekswterikh metablith ths topothesias gia krathma tou location se kathe onLocationChange()
    private int mapTypeInt = 0; //metablhth gia to krathma tou typou tou xarth apo to dialogbox
    private int poisDistance = 6; //metablhth gia to krathma tis apostashs twn poi apo thn thesh tou xrhsth epiloghs apo to dlgbox
    private int poisDistanceArray[] = {50 ,100,250,500,1000,2000,3000}; //pinakas twn apostasewn
    private boolean firstTimeRun = true; //prwth fora treksimo ths efarmoghs
    private Marker[] placeMarkers; //epiloges gia to kathe marker poi sto xarth
    private final int MAX_PLACES = 20; //^^^
    private MarkerOptions[] places;//^^^
    //pinakas me tous typous twn poi
    private String placeTypesArray[] = {"accounting", "airport", "amusement_park", "aquarium", "art_gallery", "atm", "bakery", "bank",
            "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley", "bus_station", "cafe", "campground", "car_dealer",
            "car_rental", "car_repair", "car_wash", "casino", "cemetery", "church", "city_hall", "clothing_store", "convenience_store",
            "courthouse", "dentist", "department_store", "doctor", "electrician", "electronics_store", "embassy", "establishment",
            "finance", "fire_station", "florist", "food", "funeral_home", "furniture_store", "gas_station", "general_contractor",
            "grocery_or_supermarket", "gym", "hair_care", "hardware_store", "health","hindu_temple", "home_goods_store", "hospital",
            "insurance_agency", "jewelry_store", "laundry","lawyer", "library", "liquor_store", "local_government_office", "locksmith",
            "lodging", "meal_delivery","meal_takeaway", "mosque", "movie_rental", "movie_theater", "moving_company", "museum", "night_club",
            "painter", "park", "parking", "pet_store", "pharmacy", "physiotherapist", "place_of_worship", "plumber", "police", "post_office",
            "real_estate_agency", "restaurant", "roofing_contractor", "rv_park", "school", "shoe_store", "shopping_mall", "spa", "stadium",
            "storage", "store", "subway_station", "synagogue", "taxi_stand", "train_station", "travel_agency", "university", "veterinary_care",
            "zoo"};

    //pinakas me ta epilegmena poi apo ton xrhsth h ta arxika epilegmena poi apo thn efarmogh
    private Integer selectedTypes[] ={14};

    String placesSearchStr = ""; //sting tou teliou url gia ebresh twn merwn
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        setUpMapIfNeeded();

        placeMarkers = new Marker[MAX_PLACES];


        findViewById(R.id.action_map_type).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertBox(2, getString(R.string.mapType), "");
            }
        });

        findViewById(R.id.action_distance).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertBox(3, getString(R.string.poiDistance), "");
            }
        });

        findViewById(R.id.action_poi_type).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertBox(4, getString(R.string.poiType), "");
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                mMap.setOnInfoWindowClickListener(this);
                mMap.getUiSettings().setMapToolbarEnabled(false);
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        if (locationEnabled()) {
        } else {
            alertBox(1, getString(R.string.locationIsOff), getString(R.string.locationOffMessage));
        }

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                outerLocation = location;
                if (firstTimeRun) {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    changeCameraView();
                    launchPlaces();
                }

            }
        });


    }

    private void changeCameraView() { //metakinhsh ths kameras
        if (outerLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(outerLocation.getLatitude(), outerLocation.getLongitude()), 16));
            firstTimeRun = false;
        }
    }


    private boolean locationEnabled() { //true=gps or network || false=not location
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (service.isProviderEnabled(LocationManager.GPS_PROVIDER) || (service.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            return true;
        } else {
            return false;
        }
    }

    private void alertBox(int choose, String title, String message) {
        switch (choose) {
            case 1:
                new MaterialDialog.Builder(this) //popup gia location services
                        .title(title)
                        .theme(Theme.LIGHT)
                        .content(message)
                        .positiveText(getString(R.string.turnOn))
                        .negativeText(getString(R.string.cancel))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent myIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.cancel();
                            }
                        })

                        .show();
                break;

            case 2:
                new MaterialDialog.Builder(this) //popup gia epilogh typou xarth
                        .title(title)
                        .theme(Theme.LIGHT)
                        .items(R.array.mapTypes)
                        .itemsCallbackSingleChoice(mapTypeInt, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        mapTypeInt = 0;
                                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                        break;
                                    case 1:
                                        mapTypeInt = 1;
                                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                        break;
                                    case 2:
                                        mapTypeInt = 2;
                                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                        break;
                                    case 3:
                                        mapTypeInt = 3;
                                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                        break;

                                }
                                return true;

                            }
                        })
                        .positiveText(getString(R.string.choose))
                        .negativeText(getString(R.string.cancel))
                        .show();
                break;


            case 3:
                new MaterialDialog.Builder(this) //popup gia epilogh apostashs pois
                        .title(title)
                        .theme(Theme.LIGHT)
                        .items(R.array.distances)
                        .itemsCallbackSingleChoice(poisDistance, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        poisDistance = 0;
                                        break;
                                    case 1:
                                        poisDistance = 1;
                                        break;
                                    case 2:
                                        poisDistance = 2;
                                        break;
                                    case 3:
                                        poisDistance = 3;
                                        break;
                                    case 4:
                                        poisDistance = 4;
                                        break;
                                    case 5:
                                        poisDistance = 5;
                                        break;
                                    case 6:
                                        poisDistance = 6;
                                        break;


                                }
                                launchPlaces();
                                return true;

                            }
                        })
                        .positiveText(getString(R.string.choose))
                        .negativeText(getString(R.string.cancel))
                        .show();
                break;

            case 4:
                new MaterialDialog.Builder(this) //popup gia epilogh typou pois
                        .title(title)
                        .theme(Theme.LIGHT)
                        .items(R.array.types)
                        .itemsCallbackMultiChoice(selectedTypes, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                selectedTypes=which;
                                launchPlaces();
                                return true;
                            }
                        })
                        .positiveText(getString(R.string.choose))
                        .negativeText(getString(R.string.cancel))
                        .show();
                break;


        }
    }

    private void launchPlaces() {
        String placeTypes ="";

        for(int i =0;i<selectedTypes.length;i++){
            placeTypes = placeTypes +placeTypesArray[selectedTypes[i]] +"%7C";

        }


        placeTypes=placeTypes.substring(0, (placeTypes.length() - 3));

                 placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/"+
                "json?location="+outerLocation.getLatitude()+","+outerLocation.getLongitude()+
                "&radius="+poisDistanceArray[poisDistance]+
                "&sensor=true"+
                "&types="+placeTypes + //food%7Cbar%7Cstore%7Cmuseum%7Cart_gallery"+
                "&key=AIzaSyCoAS7z1W3jbsCd073AjOER4aFx2lQNWPA";//to kleidi twn places einai hardcoded


        new GetPlaces().execute("AIzaSyCoAS7z1W3jbsCd073AjOER4aFx2lQNWPA");
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        String messageContent = "";
            new MaterialDialog.Builder(this)
                .title(R.string.navigate)
                .theme(Theme.LIGHT)
                .content(getResources().getString(R.string.navigateText) + " "+ marker.getTitle())
                .positiveText(R.string.navigateYes)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr="+marker.getPosition().latitude+","+marker.getPosition().longitude));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.cancel();
                    }
                })
                .show();

    }

    private class GetPlaces extends AsyncTask<String, Void, String> {//klash gia to metafora twn merwn apo to server sthn efarmogh asunxrona
            @Override
            protected String doInBackground(String... placesURL) {
                ArrayList<Place> resultList = null;

                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                try {

                    URL url = new URL(placesSearchStr);
                    conn = (HttpURLConnection) url.openConnection();
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    int read;
                    char[] buff = new char[1024];
                    while ((read = in.read(buff)) != -1) {
                        jsonResults.append(buff, 0, read);
                    }
                } catch (MalformedURLException e) {
                    Log.e("ERRROR", "Error processing Places API URL", e);

                } catch (IOException e) {
                    Log.e("ERROR", "Error connecting to Places API", e);

                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
                return jsonResults.toString();
            }

            protected  void onPreExecute(){
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            }

            protected void onPostExecute(String result) { //diaxeirhsh tou json string afou exei epistrafei apo to server

                if(placeMarkers!=null){
                    for(int pm=0; pm<placeMarkers.length; pm++){
                        if(placeMarkers[pm]!=null)
                            placeMarkers[pm].remove();
                    }
                }

                try {
                    JSONObject resultObject = new JSONObject(result);
                    JSONArray placesArray = resultObject.getJSONArray("results");
                    places = new MarkerOptions[placesArray.length()];

                    for (int p=0; p<placesArray.length(); p++) {//loop sta merh
                        //diaxeirhsh kathe merous
                        boolean missingValue=false;
                        LatLng placeLL=null;
                        String placeName="";
                        String vicinity="";
                        Bitmap currIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.defaultmark);
                        try{//ebresh dedomenwn merous
                            missingValue=false;
                            JSONObject placeObject = placesArray.getJSONObject(p);
                            JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
                            //JSONObject openNow = placeObject.getJSONObject("opening_hours");
                            placeLL = new LatLng(Double.valueOf(loc.getString("lat")),Double.valueOf(loc.getString("lng")));

                            //kwdikas gia to katebasma twn png kai prosthesh se eikona gia to marker Sto mellon

                            vicinity = placeObject.getString("vicinity");
                            placeName = placeObject.getString("name");

                        }
                        catch(JSONException jse){
                            missingValue=true;
                            jse.printStackTrace();
                        }

                        if(missingValue){
                            places[p]=null;
                        }
                        else{
                            places[p]=new MarkerOptions()
                                    .position(placeLL)
                                    .title(placeName)
                                    .icon(BitmapDescriptorFactory.fromBitmap(currIcon))
                                    .snippet(vicinity);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                if(places!=null && placeMarkers!=null){
                    for(int p=0; p<places.length && p<placeMarkers.length; p++){
                        if(places[p]!=null)
                            placeMarkers[p]=mMap.addMarker(places[p]); //an ola kala kai epistrafhsan merh prosthetoume to meros sto xarth
                    }
                }

                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }

        }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}



