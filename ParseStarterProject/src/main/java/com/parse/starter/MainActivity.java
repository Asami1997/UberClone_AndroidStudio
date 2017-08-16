/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class MainActivity extends AppCompatActivity {

  Switch switch2;

    Button getStarted ;

     String currentUserIs = "driver";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    switch2 = (Switch) findViewById(R.id.switch1);

    getStarted = (Button) findViewById(R.id.getStartedid);

      getSupportActionBar().hide();

    switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


        if( b == true)
           {
               currentUserIs = "rider";

             Log.i("Info","Switch Value " + currentUserIs);

           }else
                  {
                      currentUserIs = "driver";

                    Log.i("Info","Switch Value " + currentUserIs);
                  }

      }
    });

      if(ParseUser.getCurrentUser() == null)
         {
             ParseAnonymousUtils.logIn(new LogInCallback() {
                 @Override
                 public void done(ParseUser user, ParseException e) {

                     if(e == null)
                     {
                         Log.i("Status","Anonymous Login Successful");
                     }else
                     {
                         Log.i("Status","Anonymous Login unsuccessful");
                     }

                 }
             });

         }else
                {

                    if(ParseUser.getCurrentUser().get("riderOrdriver") != null)
                       {
                         Log.i("Info","Redirecting as " + ParseUser.getCurrentUser().get("riderOrdriver"));
                           activityStartType(ParseUser.getCurrentUser().get("riderOrdriver").toString());
                       }

                }


    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }

   public  void getStarted(View view)
      {

          //the user can use the app as a drive or a rider

          if(ParseUser.getCurrentUser().getUsername()!=null)
             {


                 ParseUser.getCurrentUser().put("riderOrdriver",currentUserIs);


                 ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                     @Override
                     public void done(ParseException e) {
                         if(e == null)
                         {
                             Log.i("userSaved as ",ParseUser.getCurrentUser().getUsername()); //save in the object of that user the type
                         }else
                         {
                             Log.i("error in saving",e.toString());

                         }

                     }
                 });

                 activityStartType(currentUserIs);
             }

      }


      public void activityStartType(String type)
          {

              if(type.equals("rider"))
                  {
                      Intent intent = new Intent(getApplicationContext(),RiderActvity.class);
                      startActivity(intent);

                  }else if (type.equals("driver"))
                            {
                                Log.i("in","driver");
                                Intent intent = new Intent(MainActivity.this,ViewRequestsActivity.class);
                                startActivity(intent);

                            }

          }

}