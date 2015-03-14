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
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

//
// class: MainActivity
//
public class MainActivity extends ActionBarActivity {
    private ImageButton uiButtonTranslate;
    private EditText uiTextInput;
    private SoftKeyboardDetector softKeyboard;
    private Settings settings;

    //
    // property (clearMode)
    //
    private boolean propClearMode = false;

    public boolean clearMode() {
        return this.propClearMode;
    }

    public void setClearMode( boolean value ) {
        this.propClearMode = value;
    }

    //
    // property (softKeyboardState)
    //
    static enum ViewModes {
        Default,
        TextInput,
        ReadOnly/*,
        Clear*/
    }

    private ViewModes propViewMode;

    public final ViewModes viewMode() {
        return this.propViewMode;
    }

    private void setViewMode( ViewModes viewMode ) {
        this.propViewMode = viewMode;
    }

    //
    // property (softKeyboardState)
    //
    static enum SoftKeyboardStates {
        Hidden,
        Raised,
    }

    private SoftKeyboardStates propSoftKeyboardState;

    public final SoftKeyboardStates softKeyboardState() {
        return this.propSoftKeyboardState;
    }

    private void setSoftKeyboardState( SoftKeyboardStates state ) {
        this.propSoftKeyboardState = state;
    }

    //
    // property (originalString)
    //
    public String propOriginalString = "";

    public final String originalString() {
        return this.propOriginalString;
    }

    public void setOriginalString( String originalString ) {
        this.propOriginalString = originalString;
    }

    //
    // overridden method (onCreate)
    //
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        final Toolbar toolbar;
        final ActionBar actionBar;
        RelativeLayout rootLayout;

        // set up ui
        super.onCreate( savedInstanceState );
        this.setContentView( R.layout.activity_main );

        // set up the toolbar/actionbar
        toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        this.setSupportActionBar( toolbar );
        actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled( true );
        actionBar.setIcon( R.drawable.app_icon );

        // get the 'translate' button and connect 'setOnClickListener' to 'buttonPressed'
        this.uiButtonTranslate = ( ImageButton ) this.findViewById( R.id.button_translate );
        this.uiButtonTranslate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                MainActivity.this.buttonPressed();
            }
        } );

        // initialize settings
        this.settings = new Settings( this );

        // get the 'input' EditText and override 'setOnClickListener'
        this.uiTextInput = ( EditText ) findViewById( R.id.inputText );
        this.uiTextInput.setTextColor( this.getResources().getColor( R.color.colorPrimary ) );
        this.uiTextInput.setHintTextColor( this.getResources().getColor( R.color.colorPrimary ) );

        // detect soft keyboard hide/show change
        this.setSoftKeyboardState( SoftKeyboardStates.Hidden );
        this.setViewMode( ViewModes.Default );
        rootLayout = ( RelativeLayout ) findViewById( R.id.main_activity );
        softKeyboard = new SoftKeyboardDetector( rootLayout, this.uiTextInput );
        softKeyboard.setSoftKeyboardCallback( new SoftKeyboardDetector.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                // this code must be run on the Ui thread
                MainActivity.this.runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        // reset button drawable
                        MainActivity.this.setSoftKeyboardState( SoftKeyboardStates.Hidden );
                        MainActivity.this.uiButtonTranslate.setBackgroundResource( R.drawable.button_animation );

                        // show hint
                        MainActivity.this.uiTextInput.setHint( MainActivity.this.getString( R.string.ui_inputHint ) );
                    }
                } );
            }

            @Override
            public void onSoftKeyboardShow() {
                // this code must be run on the Ui thread
                MainActivity.this.runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        // set view mode to 'TextInput' and display a semi-transparent button
                        MainActivity.this.setSoftKeyboardState( SoftKeyboardStates.Raised );
                        MainActivity.this.setViewMode( ViewModes.TextInput );

                        // make button semi-transparent only if soft keyboard is present
                        /*if ( !MainActivity.this.hwKeyboard())*/
                        MainActivity.this.uiButtonTranslate.setBackgroundResource( R.drawable.button_alpha50 );

                        // hide hint
                        MainActivity.this.uiTextInput.setHint( "" );
                    }
                } );
            }
        } );

        // restore text if needed
        if ( this.settings.restore() && this.settings.lastEntry().length() > 0 )
            this.uiTextInput.setText( this.settings.lastEntry() );
    }

    //
    // overridden method (onDestroy)
    //
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.softKeyboard.unRegisterSoftKeyboardCallback();
    }

    //
    // method (hideSoftKeyboard)
    //
    private void hideSoftKeyboard() {
        InputMethodManager imm;

        // get and hide software keyboard
        imm = ( InputMethodManager ) getSystemService( Context.INPUT_METHOD_SERVICE );
        imm.hideSoftInputFromWindow( this.uiTextInput.getWindowToken(), 0 );
    }

    //
    // method (buttonPressed) - performs Ui transitions with subsequent text conversion
    //
    private void buttonPressed() {
        AnimationDrawable animation;
        LayerDrawable button;

        // clear mode
        if ( this.clearMode() ) {
            this.uiTextInput.setText( "" );
            this.setOriginalString( "" );
            this.uiTextInput.clearFocus();
            this.uiTextInput.setHint( this.getString( R.string.ui_inputHint ) );
        }

        // copy text input string
        if ( this.viewMode() != ViewModes.ReadOnly && !this.clearMode() )
            this.setOriginalString( this.uiTextInput.getText().toString() );

        // check whether input is empty
        if ( this.originalString().length() == 0 && !this.clearMode() ) {
            Toast.makeText( MainActivity.this, this.getString( R.string.ui_emptyInputWarning ), Toast.LENGTH_SHORT ).show();
            return;
        }

        // hide soft keyboard
        if ( this.softKeyboardState() == SoftKeyboardStates.Raised )
            this.hideSoftKeyboard();

        // handle view modes
        if ( this.viewMode() == ViewModes.Default || this.viewMode() == ViewModes.TextInput ) {
            String convertedText;

            // set animation resource regardless of view state
            this.uiButtonTranslate.setBackgroundResource( R.drawable.button_animation );

            // abort on clear
            if ( this.clearMode() ) {
                this.setViewMode( ViewModes.Default );
                this.setClearMode( false );
                return;
            }

            // reset view mode
            this.setViewMode( ViewModes.ReadOnly );

            // hide cursor and disable hint
            this.uiTextInput.setCursorVisible( false );
            this.uiTextInput.setFocusableInTouchMode( false );
            this.uiTextInput.setEnabled( false );

            // convert the text
            convertedText = Translator.translate( this.originalString(), this.settings.symbol(), this.settings.keepMacrons() );
            this.uiTextInput.setText( convertedText );

            // store last entry if needed
            if ( this.settings.restore())
                this.settings.setLastEntry( this.originalString() );
            else
                this.settings.setLastEntry( "" );
        } else if ( this.viewMode() == ViewModes.ReadOnly ) {
            // set backwards animation
            this.uiButtonTranslate.setBackgroundResource( R.drawable.button_animation_backwards );
            this.setViewMode( ViewModes.Default );

            // restore cursor and hint
            this.uiTextInput.setCursorVisible( true );
            this.uiTextInput.setFocusableInTouchMode( true );
            this.uiTextInput.setEnabled( true );

            // restore previous text
            this.uiTextInput.setText( this.originalString() );
        }

        // perform button animations
        button = ( LayerDrawable ) this.uiButtonTranslate.getBackground();
        animation = ( AnimationDrawable ) button.getDrawable( 1 );
        animation.start();

        // all done
        this.setClearMode( false );
    }

    //
    // overridden method (onCreateOptionsMenu)
    //
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // inflate the menu
        this.getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    //
    // overridden method (onOptionsItemSelected) - handles toolbar/actionbar events
    //
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id;

        // handle menu actions
        id = item.getItemId();
        if ( id == R.id.actionSettings ) {
            this.startActivity( new Intent( this, SettingsActivity.class ) );
        } else if ( id == R.id.actionCopy ) {
            // store translateBuffer into clipboard
            this.copyToClipBoard();
        } else if ( id == R.id.actionClear ) {
            // TODO: so this does not work (on all view modes at least)
            this.setClearMode( true );
            this.buttonPressed();
        }

        return super.onOptionsItemSelected( item );
    }

    //
    // method (copyToClipBoard) - stores converted text into the clipboard
    //
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void copyToClipBoard() {
        String convertedString;

        // get the converted string
        convertedString = Translator.translate( this.originalString(), this.settings.symbol(), this.settings.keepMacrons() );
        if ( convertedString.length() > 0 ) {
            // handle legacy clipboard (if we ever plan to support API < 14)
            if ( MainActivity.sdk() < android.os.Build.VERSION_CODES.HONEYCOMB ) {
                android.text.ClipboardManager clipboard;

                // get the clipboardManager and store the string
                clipboard = ( android.text.ClipboardManager ) getSystemService( Context.CLIPBOARD_SERVICE );
                clipboard.setText( convertedString );
            } else {
                android.content.ClipboardManager clipboard;
                android.content.ClipData clip;

                // get the clipboardManager and store the string
                clipboard = ( android.content.ClipboardManager ) getSystemService( Context.CLIPBOARD_SERVICE );
                clip = android.content.ClipData.newPlainText( "WordKeeper", convertedString );
                clipboard.setPrimaryClip( clip );
            }
        }
    }

    //
    // overridden method (onResume)
    //
    @Override
    protected void onResume () {
        // execute overridden method
        super.onResume();

        // reload settings
        if ( this.settings != null )
            this.settings.load();
    }

    //
    // static method (sdk) - returns android API version
    //
    public static int sdk() {
        return android.os.Build.VERSION.SDK_INT;
    }
}
