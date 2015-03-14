/*
===========================================================================
Copyright (C) 2014-2015 Factory #12

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see http://www.gnu.org/licenses/.

===========================================================================
*/

//
// package (pupinotajs)
//
package org.factory12.pupinotajs;

//
// imports
//
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

//
// class (SettingsActivity)
//
public class SettingsActivity extends PreferenceActivity {
    //
    // overridden method (onCreate)
    //
    @SuppressLint( "NewApi" )
    @SuppressWarnings( "deprecation" )
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // set up settings for API<11
        if ( MainActivity.sdk() < Build.VERSION_CODES.HONEYCOMB ) {
            // set up toolbar
            this.setupToolbar();
            this.addPreferencesFromResource( R.xml.settings );
        } else {
           this.setContentView(R.layout.fragment_settings );
           this.getFragmentManager().beginTransaction().replace( R.id.settings_frame, new SettingsFragment2()).commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // set up toolbar for API>=11
        if ( MainActivity.sdk() >= Build.VERSION_CODES.HONEYCOMB )
            this.setupToolbar();
    }

    //
    // method (setupToolbar)
    //
    private void setupToolbar() {
        Toolbar toolbar;
        TextView textView;
        ViewGroup rootView;
        View view;
        LinearLayout rootLayout;

        // get root view/layout
        rootView = ( ViewGroup ) findViewById( android.R.id.content );
        view = rootView.getChildAt(0);
        rootView.removeAllViews();
        rootLayout = ( LinearLayout ) View.inflate( this, R.layout.activity_settings, null );
        rootLayout.addView( view );
        rootView.addView( rootLayout );

        // fetch toolbar
        toolbar = ( Toolbar ) rootLayout.findViewById( R.id.toolbar );
        toolbar.setTitle( this.getString( R.string.action_settings ) );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        SettingsActivity.this.finish();
            }
        } );
        toolbar.setNavigationIcon( R.drawable.abc_ic_ab_back_mtrl_am_alpha );

        // set toolbar title
        textView =  ( TextView ) rootLayout.findViewById( R.id.title );
        textView.setText( this.getString( R.string.action_settings ) );
    }

    //
    // overridden method (isValidFragment) - special code to avoid fragment injection
    //
    @TargetApi( Build.VERSION_CODES.KITKAT )
    @Override
    protected boolean isValidFragment( String name ) {
        return name.equals( SettingsActivity.class.getName() );
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingsFragment2 extends PreferenceFragment {
        //
        // overridden method (onCreate)
        //
        @Override
        public void onCreate( Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );
            this.addPreferencesFromResource( R.xml.settings );
        }
    }
}
