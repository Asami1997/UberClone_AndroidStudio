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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ViewRequestsActivity extends AppCompatActivity {

    ListView listView;

    ArrayList<String> requests;

    ArrayAdapter<String> requestsAdapter;

    LocationManager locationManager;


    LocationListener locationListener;


    ArrayList<Double> lats = new ArrayList<Double>();

    ArrayList<Double> longts = new ArrayList<Double>();

    ArrayList<String> userNames =  new ArrayList<String>();

    Button driverLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        driverLogoutButton = (Button) findViewById(R.id.driverLogout);

        listView = (ListView) findViewById(R.id.requestsListView);

        requests = new ArrayList<String>();

        requestsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,requests);

        listView.setAdapter(requestsAdapter);

        requests.clear();

        requests.add("Getting Data....");


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //getting the driver lastknow location

                if(ContextCompat.checkSelfPermission(ViewRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                   {
                       Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                       //to check if we have atleast items more than i ie(position) in the arraylist
                       if(lastKnown!= null && lats.size() > i && longts.size() > i )
                          {
                              Intent intent = new Intent(getApplicationContext(),DriverLocationActivity.class);

                              intent.putExtra("requestLatitude",lats.get(i));

                              intent.putExtra("requestLongtitude",longts.get(i));

                              intent.putExtra("driverLatitude",lastKnown.getLatitude());

                              intent.putExtra("driverLongtitude",lastKnown.getLongitude());

                              intent.putExtra("requestUserName",userNames.get(i));

                              startActivity(intent);
                          }
                   }





            }
        });


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {


                updateListView(location);

                ParseUser.getCurrentUser().put("driverLocation", new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
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
            if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else
               {
                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);


                   Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                   if(lastKnown!=null)
                   {

                       updateListView(lastKnown);
                   }



               }
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                //although there may be possibility that there will be a lastknown location when  he first acceots, ie : when
                //he first start using  the app

                Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnown!=null)
                {

                    updateListView(lastKnown);

                }


            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //update the listView as the driver moves to get only the nearby uber requests
    private void updateListView(final Location location)
       {


           if(location != null)
              {
                  ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");

                  requests.clear();
                  lats.clear();
                  longts.clear();

                  //i need it as geopoint to calculate the distance between it and the geopint in the server

                  final ParseGeoPoint driverLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());

                  // i need to have a geopoint in the requests for this to work
                  query.whereNear("riderLocation",driverLocation);

                  //only get requests that only doent have a driver's name , ie: not accepted yet

                  query.whereDoesNotExist("driverUsername");

                  query.findInBackground(new FindCallback<ParseObject>() {
                      @Override
                      public void done(List<ParseObject> objects, ParseException e) {

                          if(e == null)
                            {
                              if(objects.size() > 0)
                                 {
                                     requests.clear();
                                     lats.clear();
                                     longts.clear();

                                     for(ParseObject locations : objects)
                                        {
                                            //get the location of that specific location which is a geo point
                                            ParseGeoPoint requestLocation =   (ParseGeoPoint) locations.get("riderLocation");


                                          Double distanceKiloM = driverLocation.distanceInKilometersTo(requestLocation);

                                            //round it to one decimal , because its too accurate

                                           Double distanceKiloRounded = (double) Math.round(distanceKiloM * 10)/10 ;

                                            requests.add(distanceKiloRounded.toString() + " Kilos");


                                            lats.add(requestLocation.getLatitude());


                                            longts.add(requestLocation.getLongitude());


                                            userNames.add(locations.get("username").toString());


                                        }


                                 }else
                                       {
                                           requests.add("No nearby requests");
                                       }
                                requestsAdapter.notifyDataSetChanged();
                            }

                      }
                  });



              }


       }


       public void driverLogout(View view)
          {
              ParseUser.logOut();

              Intent intent = new Intent(getApplicationContext(),MainActivity.class);

              startActivity(intent);
          }


}


