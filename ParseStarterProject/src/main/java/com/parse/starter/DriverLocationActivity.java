package com.parse.starter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Button acceptButton ;

    Intent intent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        acceptButton = (Button) findViewById(R.id.acceptButton);

        RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.map_layout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //and write code, which you can see in answer above
                intent = getIntent();

                LatLng requestLocation = new LatLng(intent.getDoubleExtra("requestLatitude",0),intent.getDoubleExtra("requestLongtitude",0));

                LatLng driverLocation = new LatLng(intent.getDoubleExtra("driverLatitude",0),intent.getDoubleExtra("driverLongtitude",0));

                ArrayList<Marker> markers = new ArrayList<Marker>();
                markers.clear();

                    markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Rider")));

                markers.add(mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));


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

    public void acceptRequest(View view)
       {
           //save in the parse that the request is accepted
             //request username is sent from the intent

           //put the drivername in the request object in parse
           //then show directions

           ParseQuery<ParseObject> getRequests = ParseQuery.getQuery("Requests");

           getRequests.whereEqualTo("username",intent.getStringExtra("requestUserName"));

           getRequests.findInBackground(new FindCallback<ParseObject>() {
               @Override
               public void done(List<ParseObject> objects, ParseException e) {

                   if(e == null)
                      {
                        if(objects.size() > 0)
                           {
                             for(ParseObject username : objects)
                                {

                                    username.put("driverUsername", ParseUser.getCurrentUser().getUsername());

                                    username.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            if(e == null)
                                               {
                                                  //show directions
                                                   Log.i("hellloooo","ppp");

                                                   //provide the longitude and latitude for both markrs in the uri

                                                   Intent directionsIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + intent.getDoubleExtra("driverLatitude",0) + "," + intent.getDoubleExtra("driverLongtitude",0) + "&daddr="+intent.getDoubleExtra("requestLatitude",0)+"," + intent.getDoubleExtra("requestLongtitude",0)));
                                                   startActivity(directionsIntent);

                                               }
                                        }
                                    });
                                }
                           }
                      }

               }
           });

       }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;




    }



}
