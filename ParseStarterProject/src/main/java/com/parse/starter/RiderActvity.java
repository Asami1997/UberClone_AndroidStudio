package com.parse.starter;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.internal.GamesContract;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RiderActvity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Button button;
    boolean callActive;
    boolean driverActive= false;
    TextView driverLocationTextView;

    Handler requestUpdatesHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_actvity);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestUpdatesHandler = new Handler();

        driverLocationTextView = (TextView) findViewById(R.id.driverLocationTextView);

        button = (Button) findViewById(R.id.callCancelButton);

        ParseQuery<ParseObject> checkState = ParseQuery.getQuery("Requests");

        checkState.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

        checkState.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                    if(e == null)
                       {
                         if(objects.size() > 0)
                            {
                              callActive = true;

                                button.setText("CANCEL UBER");
                                checkForRequestUpdates();
                            }
                       }

            }
        });


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

             updateMap(location);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        if(Build.VERSION.SDK_INT <14)
           {
             locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
           }else
                   {
                      //check the permission
                       if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                          {
                              ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                          }else
                                {
                                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                                    mMap.clear();

                                    Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if(lastKnown!=null)
                                       {
                                         updateMap(lastKnown);
                                       }


                                }
                   }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
           {
               if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                  {
                      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                  }

           }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void callOrCancel (View view)
        {
           if(button.getText().equals("CALL AN UBER"))
              {

                  callAnUber();
                  button.setText("CANCEL UBER");
              }else if(button.getText().equals("CANCEL UBER"))
                        {
                          cancelUber();
                            button.setText("CALL AN UBER");
                        }
        }


        public void callAnUber ()
           {
               callActive = true;
               //create a new object in class requests
               ParseObject parseObject= new ParseObject("Requests");
               //store username
               parseObject.put("username", ParseUser.getCurrentUser().getUsername());

               parseObject.saveInBackground(new SaveCallback() {
                   @Override
                   public void done(ParseException e) {

                       if(e == null)
                       {
                           Log.i("username","saved");
                       }
                   }
               });

               //save user location using parse geopoint

               if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
               {
                   Location locationToBeSaved = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                   LatLng savedLatLng = new LatLng(locationToBeSaved.getLatitude(),locationToBeSaved.getLongitude());
                   if(locationToBeSaved != null)
                      {
                          ParseGeoPoint userCoordinates = new ParseGeoPoint(savedLatLng.latitude,savedLatLng.longitude);
                          parseObject.put("riderLocation",userCoordinates);
                          parseObject.saveInBackground(new SaveCallback() {
                              @Override
                              public void done(ParseException e) {
                                  if(e == null)
                                  {
                                      Log.i("Rider's Location","Saved");
                                  }else
                                  {
                                      Log.i("Rider's Location","Not Saved" +e.toString());
                                  }
                              }
                          });

                      }else
                            {
                                Toast.makeText(getApplicationContext(), "Couldn't find location , please try again later", Toast.LENGTH_SHORT).show();
                            }


               }


                  checkForRequestUpdates();
           }


           public void cancelUber()
              {
                 callActive = false;

                  ParseQuery<ParseObject> deleteQuery =  ParseQuery.getQuery("Requests");

                  deleteQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

                  deleteQuery.findInBackground(new FindCallback<ParseObject>() {
                      @Override
                      public void done(List<ParseObject> objects, ParseException e) {

                          for(ParseObject parseObject : objects)
                             {
                                 try {
                                     parseObject.delete();
                                     parseObject.saveInBackground();
                                     Log.i("Request Status","Deleted");
                                 } catch (ParseException e1) {
                                     e1.printStackTrace();
                                 }

                             }
                      }
                  });

                  driverLocationTextView.setText("");
              }


              public void logoutUser (View view)
                 {
                   ParseUser.logOut();
                     Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                     startActivity(intent);
                 }



                 public void checkForRequestUpdates()
                     {
                        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");

                         query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

                         query.whereExists("driverUsername"); //only the accepted ones


                         query.findInBackground(new FindCallback<ParseObject>() {
                             @Override
                             public void done(List<ParseObject> objects, ParseException e) {

                                 if(e == null)
                                    {
                                      if(objects.size() > 0)
                                         {



                                             //get the driver location so we can find the distance between it and the riderlocation

                                             ParseQuery<ParseUser> driverQuery = ParseUser.getQuery();

                                             driverQuery.whereEqualTo("username",objects.get(0).getString("username"));

                                             driverQuery.findInBackground(new FindCallback<ParseUser>() {
                                                 @Override
                                                 public void done(List<ParseUser> objects, ParseException e) {

                                                     // i need the two locations as parsegeopoints


                                                             if(e == null&& objects.size()>0)
                                                                {
                                                                    driverActive = true;

                                                                    ParseGeoPoint driverLocation = (ParseGeoPoint) objects.get(0).get("driverLocation");
                                                                    //need to listen to every location  so i just need to call the locationmanger for thr updates

                                                                    if(Build.VERSION.SDK_INT <14||ContextCompat.checkSelfPermission(RiderActvity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                                                                       {

                                                                           Location lastknown  = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                                           if(lastknown!= null)
                                                                              {
                                                                                  ParseGeoPoint riderLocation =  new ParseGeoPoint(lastknown.getLatitude(),lastknown.getLongitude());


                                                                                  Double distanceKiloM = driverLocation.distanceInKilometersTo(riderLocation);

                                                                                  //round it to one decimal , because its too accurate

                                                                                  Double distanceKiloRounded = (double) Math.round(distanceKiloM * 10)/10 ;

                                                                                  if(distanceKiloRounded == 0.0)
                                                                                     {
                                                                                      driverLocationTextView.setText("Driver has arrived");
                                                                                         driverActive = false;
                                                                                         button.setText("CALL AN UBER");

                                                                                         //delete request from parse

                                                                                         ParseQuery<ParseObject> deletRequest = ParseQuery.getQuery("Request");
                                                                                         query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                                                                                         query.findInBackground(new FindCallback<ParseObject>() {
                                                                                             @Override
                                                                                             public void done(List<ParseObject> objects, ParseException e) {

                                                                                                 if (e == null)
                                                                                                    {
                                                                                                      for(ParseObject delete : objects)
                                                                                                          {
                                                                                                           delete.deleteInBackground();
                                                                                                          }
                                                                                                    }
                                                                                             }
                                                                                         });


                                                                                    }else
                                                                                     {
                                                                                         driverLocationTextView.setText("Driver is " + distanceKiloRounded.toString() + " Kilometers away");



                                                                                         LatLng requestLocation = new LatLng(riderLocation.getLatitude(),riderLocation.getLongitude());

                                                                                         LatLng driverLocationForMarker = new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());

                                                                                         ArrayList<Marker> markers = new ArrayList<Marker>();
                                                                                         mMap.clear();
                                                                                         markers.clear();

                                                                                         markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Your Location")));

                                                                                         markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationForMarker).title("Driver").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));


                                                                                         //store the markrs in an arraylist
                                                                                         //you need the latlngs of the two locations to create a latlng


                                                                                         //To calculate the bounds of all the markrs

                                                                                         LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                                                         for (Marker marker : markers) {
                                                                                             builder.include(marker.getPosition());
                                                                                         }
                                                                                         //creating a Latlngbounds object

                                                                                         LatLngBounds bounds = builder.build();


                                                                                         int padding = 30; // offset from edges of the map in pixels
                                                                                         CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                                                                                         mMap.animateCamera(cu);
                                                                                     }

                                                                              }

                                                                       }


                                                                }

                                                 }
                                             });
                                         }
                                   }

                             }
                         });


                         //will keep checking every two seconds , wven if the excution is not here, if we ddient excute the function even from anthor place

                         requestUpdatesHandler.postDelayed(new Runnable() {
                             @Override
                             public void run() {

                                 checkForRequestUpdates();

                             }
                         },2000);

                     }


                     public void updateMap(Location location)
                        {
                            //if there is a driver we dont want to update map beacsue it will clash with the code we use to display two markers in checkforupdates which anyways will run evry two seconds
                         if(driverActive!=true)
                            {
                                mMap.clear();
                                driverLocationTextView.setText("");
                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));

                            }

                        }
}
