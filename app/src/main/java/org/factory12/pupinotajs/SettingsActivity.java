/*
 * Copyright (C) 2014-2018 Factory #12
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 */

package org.factory12.pupinotajs;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        this.getFragmentManager().beginTransaction().replace( android.R.id.content, new SettingsFragment() ).commit();
        setupActionBar();
    }

    /**
     *
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if ( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );
    }

    @Override
    public boolean onMenuItemSelected( int featureId, MenuItem item ) {
        int id = item.getItemId();
        if ( id == android.R.id.home ) {
            if ( !super.onMenuItemSelected( featureId, item ) ) {
                NavUtils.navigateUpFromSameTask( this );
            }
            return true;
        }
        return super.onMenuItemSelected( featureId, item );
    }

    public static class SettingsFragment extends PreferenceFragment {

        /**
         * @param savedInstanceState
         */
        @Override
        public void onCreate( Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );
            SettingsFragment.this.addPreferencesFromResource( R.xml.pref_general );
        }
    }
}
