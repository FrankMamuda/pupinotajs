/*
===========================================================================
Author:
    Felipe Herranz (felhr85@gmail.com)

Contributors:
    Francesco Verheye (verheye.francesco@gmail.com)
    Israel Dominguez (dominguez.israel@gmail.com)
===========================================================================
*/

//
// package (pupinotajs)
//
package org.factory12.pupinotajs;

//
// imports (android)
//

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

//
// imports (java)
//
import java.util.concurrent.atomic.AtomicBoolean;

//
// class: SoftKeyboardDetector
//
public class SoftKeyboardDetector implements View.OnFocusChangeListener {
    private static final int clearFocus = 0;
    private ViewGroup layout;
    private int layoutBottom;
    private int[] coords;
    private boolean isKeyboardShown;
    private SoftKeyboardChangesThread softKeyboardThread;
    private View tempView;

    //
    // constructor (SoftKeyboardDetector)
    //
    public SoftKeyboardDetector( ViewGroup layout, EditText editText ) {
        this.layout = layout;
        this.layout.setFocusable( true );
        this.layout.setFocusableInTouchMode( true );
        editText.setOnFocusChangeListener( this );
        editText.setCursorVisible( true );
        this.coords = new int[2];
        this.isKeyboardShown = false;
        this.softKeyboardThread = new SoftKeyboardChangesThread();
        this.softKeyboardThread.start();
    }

    //
    // method (setSoftKeyboardCallback)
    //
    public void setSoftKeyboardCallback( SoftKeyboardChanged mCallback ) {
        this.softKeyboardThread.setCallback( mCallback );
    }

    //
    // method (unRegisterSoftKeyboardCallback)
    //
    public void unRegisterSoftKeyboardCallback() {
        this.softKeyboardThread.stopThread();
    }

    //
    // interface (SoftKeyboardChanged)
    //
    public interface SoftKeyboardChanged {
        public void onSoftKeyboardHide();

        public void onSoftKeyboardShow();
    }

    //
    // method (getLayoutCoordinates)
    //
    private int getLayoutCoordinates() {
        this.layout.getLocationOnScreen( this.coords );
        return this.coords[1] + this.layout.getHeight();
    }

    //
    // overridden method (onFocusChange)
    //
    @Override
    public void onFocusChange( View view, boolean hasFocus ) {
        if ( hasFocus ) {
            this.tempView = view;
            if ( !this.isKeyboardShown ) {
                this.layoutBottom = getLayoutCoordinates();
                this.softKeyboardThread.keyboardOpened();
                this.isKeyboardShown = true;
            }
        }
    }

    // this handler will clear focus of selected EditText
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage( Message m ) {
            if ( m.what == clearFocus && SoftKeyboardDetector.this.tempView != null ) {
                SoftKeyboardDetector.this.tempView.clearFocus();
                SoftKeyboardDetector.this.tempView = null;
            }
        }
    };

    //
    // class (SoftKeyboardChangesThread) - thread, that detects SoftKeyboard visibility changes
    //
    private class SoftKeyboardChangesThread extends Thread {
        private AtomicBoolean started;
        private SoftKeyboardChanged mCallback;

        //
        // constructor (SoftKeyboardChangesThread)
        //
        public SoftKeyboardChangesThread() {
            this.started = new AtomicBoolean( true );
        }

        //
        // method (setCallback)
        //
        public void setCallback( SoftKeyboardChanged mCallback ) {
            this.mCallback = mCallback;
        }

        //
        // overridden method (run)
        //
        @Override
        public void run() {
            while ( started.get() ) {
                int currentBottomLocation;

                // wait until keyboard is requested to open
                synchronized ( this ) {
                    try {
                        wait();
                    } catch ( InterruptedException e ) {
                        e.printStackTrace();
                    }
                }

                currentBottomLocation = getLayoutCoordinates();

                // there is some lag between open soft-keyboard function and when it really appears
                while ( currentBottomLocation == SoftKeyboardDetector.this.layoutBottom && this.started.get() )
                    currentBottomLocation = SoftKeyboardDetector.this.getLayoutCoordinates();

                if ( this.started.get() )
                    this.mCallback.onSoftKeyboardShow();

                // when keyboard is opened from EditText, initial bottom location is greater than layoutBottom and at some moment equals layoutBottom
                // that broke the previous logic, so I added this new loop to handle this
                while ( currentBottomLocation >= layoutBottom && started.get() )
                    currentBottomLocation = SoftKeyboardDetector.this.getLayoutCoordinates();

                // now Keyboard is shown, keep checking layout dimensions until keyboard is gone
                while ( currentBottomLocation != layoutBottom && started.get() ) {
                    synchronized ( this ) {
                        try {
                            wait( 500 );
                        } catch ( InterruptedException e ) {
                            e.printStackTrace();
                        }
                    }
                    currentBottomLocation = SoftKeyboardDetector.this.getLayoutCoordinates();
                }

                if ( this.started.get() )
                    this.mCallback.onSoftKeyboardHide();

                // if keyboard has been opened clicking and EditText
                if ( SoftKeyboardDetector.this.isKeyboardShown && this.started.get() )
                    SoftKeyboardDetector.this.isKeyboardShown = false;

                // if an EditText is focused, remove its focus (on UI thread)
                if ( this.started.get() )
                    SoftKeyboardDetector.this.mHandler.obtainMessage( clearFocus ).sendToTarget();
            }
        }

        //
        // method (keyboardOpened)
        //
        public void keyboardOpened() {
            synchronized ( this ) {
                this.notify();
            }
        }

        //
        // method (stopThread)
        //
        public void stopThread() {
            synchronized ( this ) {
                this.started.set( false );
                this.notify();
            }
        }
    }
}
