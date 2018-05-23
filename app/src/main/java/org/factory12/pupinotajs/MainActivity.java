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
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private boolean m_converted = false;
    private SharedPreferences preferences;

    private boolean isConverted() {
        return this.m_converted;
    }

    private void setConverted( boolean converted ) {
        this.m_converted = converted;
    }

    private String m_text = "";

    /**
     *
     */
    private boolean isPreservingMacrons() {
        return this.preferences.getBoolean( this.getResources().getString( R.string.settings_key_keepMacrons ), this.getResources().getBoolean( R.bool.settings_defaults_keepMacrons ) );
    }

    private boolean isPreservingText() {
        return this.preferences.getBoolean( this.getResources().getString( R.string.settings_key_restore ), this.getResources().getBoolean( R.bool.settings_defaults_restore ) );
    }

    private String getText() {
        return this.isPreservingText() ? this.preferences.getString( this.getResources().getString( R.string.settings_key_lastEntry ), "" ) : this.m_text;
    }

    private void setText( String text ) {
        if ( this.isPreservingText() ) {
            final SharedPreferences.Editor editor;

            editor = this.preferences.edit();
            editor.putString( this.getResources().getString( R.string.settings_key_lastEntry ), text );
            editor.commit();
        }

        this.m_text = text;
    }

    private String symbol() {
        return this.preferences.getString( this.getResources().getString( R.string.settings_key_symbol ), this.getResources().getString( R.string.settings_defaults_symbol ) );
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        if ( this.isPreservingText() )
            this.setText( this.m_text );

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if ( this.isPreservingText() )
            this.setText( this.m_text );

        super.onDestroy();
    }

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        final Toolbar toolbar;
        final FloatingActionButton fab;
        final EditText editText;
        final InputMethodManager imm;

        // get preferences
        this.preferences = PreferenceManager.getDefaultSharedPreferences( this );

        // restore instance and inflate activity
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_main );

        // set up toolbar
        toolbar = this.findViewById( R.id.toolbar );
        this.setSupportActionBar( toolbar );

        // get action button
        fab = findViewById( R.id.fab );

        // get keyboard
        imm = ( ( InputMethodManager ) MainActivity.this.getSystemService( Context.INPUT_METHOD_SERVICE ) );
        if ( imm == null )
            return;

        // get edit
        editText = MainActivity.this.findViewById( R.id.editText );
        if ( this.isPreservingText() )
            editText.setText( this.getText() );

        editText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if ( MainActivity.this.isConverted() ) {
                    fab.performClick();
                    imm.toggleSoftInput( InputMethodManager.SHOW_FORCED, 0 );
                }
            }
        } );
        editText.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {
                if ( MainActivity.this.isConverted() ) {
                    fab.performClick();
                    imm.toggleSoftInput( InputMethodManager.SHOW_FORCED, 0 );
                } else {
                    MainActivity.this.m_text = editText.getText().toString();
                }
                return false;
            }
        } );

        // connect action button
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                int y;
                final LayerDrawable drawable;
                final ObjectAnimator animator;
                final int green, red;

                green = Color.parseColor( "#82a427" );
                red = Color.parseColor( "#AA0000" );

                animator = ObjectAnimator.ofInt( fab, "backgroundTint",
                        MainActivity.this.isConverted() ? green : red,
                        MainActivity.this.isConverted() ? red : green
                );
                animator.setDuration( 2000L );
                animator.setEvaluator( new ArgbEvaluator() );
                animator.setInterpolator( new DecelerateInterpolator( 2 ) );
                animator.addUpdateListener( new ObjectAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation ) {
                        int animatedValue = ( int ) animation.getAnimatedValue();
                        fab.setBackgroundTintList( ColorStateList.valueOf( animatedValue ) );
                    }
                } );
                animator.start();

                // check if anything has been written
                if ( editText.length() <= 0 && !MainActivity.this.isConverted() ) {
                    Snackbar.make( view, MainActivity.this.getResources().getText( R.string.text_warning ), Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
                    MainActivity.this.setConverted( false );
                    imm.toggleSoftInput( InputMethodManager.SHOW_FORCED, 0 );
                    return;
                }

                // set converted text
                if ( !MainActivity.this.isConverted() )
                    MainActivity.this.setText( editText.getText().toString() );

                // set or reset converted text
                editText.setText( MainActivity.this.isConverted() ? MainActivity.this.getText() : Translator.translate( MainActivity.this.getText(), MainActivity.this.symbol(), MainActivity.this.isPreservingMacrons() ) );

                // hide keyboard if necessary
                imm.hideSoftInputFromWindow( view.getWindowToken(), 0 );

                // get action button drawable
                fab.setImageDrawable( MainActivity.this.getDrawable( MainActivity.this.isConverted() ? R.drawable.icon_reversed : R.drawable.icon_animated ) );
                drawable = ( LayerDrawable ) fab.getDrawable();
                if ( drawable != null ) {
                    if ( drawable.getNumberOfLayers() == 2 ) {
                        for ( y = 0; y < 2; y++ ) {
                            final Animatable anima;

                            anima = ( Animatable ) drawable.getDrawable( y );
                            if ( anima != null )
                                anima.start();
                        }
                    }
                }

                // toggle state
                MainActivity.this.setConverted( !MainActivity.this.isConverted() );
            }
        } );
    }

    /**
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // inflate menu
        this.getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    /**
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        final int id;
        final FloatingActionButton fab;

        id = item.getItemId();
        fab = findViewById( R.id.fab );

        if ( id == R.id.action_settings ) {
            Intent settingsActivity;

            if ( this.isConverted() )
                fab.performClick();

            settingsActivity = new Intent( MainActivity.this, SettingsActivity.class );
            this.startActivity( settingsActivity );
            return true;
        } else if ( id == R.id.action_copy ) {
            final ClipboardManager clipboard;
            final ClipData clip;
            final String convertedString;

            convertedString = Translator.translate( this.getText(), this.symbol(), this.isPreservingMacrons() );
            if ( convertedString.length() > 0 ) {
                clipboard = ( ClipboardManager ) this.getSystemService( Context.CLIPBOARD_SERVICE );
                clip = ClipData.newPlainText( "Pupinotajs text", convertedString );
                if ( clipboard != null )
                    clipboard.setPrimaryClip( clip );
            }
        } else if ( id == R.id.action_clear ) {
            final EditText editText;

            // reset button and state
            if ( this.isConverted() )
                fab.performClick();

            // clear text if any
            editText = MainActivity.this.findViewById( R.id.editText );
            editText.setText( "" );
        }

        return super.onOptionsItemSelected( item );
    }
}
