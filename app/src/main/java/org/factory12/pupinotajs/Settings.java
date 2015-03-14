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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

//
// class (Settings)
//
public class Settings {
    private boolean prop_keepMacrons;
    private boolean prop_restore;
    private String prop_symbol;
    private String prop_lastEntry;
    private SharedPreferences settings;
    private Context parent;

    //
    // constructor (Settings)
    //
    public Settings( Context context ){
        this.parent = context;
        this.settings = PreferenceManager.getDefaultSharedPreferences( parent );
        this.load();
    }

    //
    // method (load)
    //
    public void load() {
        this.prop_keepMacrons = settings.getBoolean( this.parent.getString( R.string.settings_key_keepMacrons ), this.parent.getResources().getBoolean( R.bool.settings_defaults_keepMacrons ) );
        this.prop_restore = settings.getBoolean( this.parent.getString( R.string.settings_key_restore ), this.parent.getResources().getBoolean( R.bool.settings_defaults_restore ));
        this.prop_symbol = settings.getString( this.parent.getString( R.string.settings_key_symbol ), this.parent.getString( R.string.settings_defaults_symbol ) );
        this.prop_lastEntry = settings.getString( this.parent.getString( R.string.settings_key_lastEntry ), "" );
    }

    //
    // method (keepMacrons)
    //
    public final boolean keepMacrons( ) {
       return this.prop_keepMacrons;
    }

    //
    // method (restore)
    //
    public final boolean restore( ) {
        return this.prop_restore;
    }

    //
    // method (symbol)
    //
    public final String symbol( ) {
        return this.prop_symbol;
    }

    //
    // method (lastEntry)
    //
    public final String lastEntry( ) {
        return this.prop_lastEntry;
    }

    //
    // method (setLastEntry)
    //
    public void setLastEntry( String lastEntry ) {
       this.prop_lastEntry = lastEntry;

        // store entry using a sdk specific method
        if ( MainActivity.sdk() >= android.os.Build.VERSION_CODES.GINGERBREAD )
            this.settings.edit().putString( this.parent.getString( R.string.settings_key_lastEntry ), lastEntry ).apply();
        else
            this.settings.edit().putString( this.parent.getString( R.string.settings_key_lastEntry ), lastEntry ).commit();
    }
}
