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
import android.util.Log;
import android.util.SparseArray;

class Translator {
    // initialize vowel array
    private static final SparseArray<Character> vowels = new SparseArray<>();
    static {
        Translator.vowels.put( 257, 'a' );
        Translator.vowels.put( 275, 'e' );
        Translator.vowels.put( 299, 'i' );
        Translator.vowels.put( 363, 'u' );
        Translator.vowels.put( 256, 'A' );
        Translator.vowels.put( 274, 'E' );
        Translator.vowels.put( 298, 'I' );
        Translator.vowels.put( 362, 'U' );
    }

    /**
     * @param inputBuffer
     * @param symbol
     * @param keepMacrons
     * @return
     */
    static String translate( final String inputBuffer, final String symbol, boolean keepMacrons ) {
        int y;
        String msg;

        // retrieve input text and store it in both a local and class buffer
        msg = inputBuffer;

        // make sure it is not empty
        if ( msg.length() == 0 )
            return "";

        // iterate through the whole message char by char
        for ( y = 0; y < msg.length(); y++ ) {
            char ch;

            // get the current char
            ch = msg.charAt( y );

            // test if the char is a vowel
            if ( Translator.isVowel( ch ) ) {
                String vowelArray = "";

                // strip macrons
                vowelArray += Translator.stripMacron( ch, keepMacrons );

                // read ahead
                while ( true ) {
                    // failsafe
                    if ( y == msg.length() - 1 )
                        break;

                    // another vowel?
                    if ( Translator.isVowel( msg.charAt( y + 1 ) ) ) {
                        vowelArray += Translator.stripMacron( msg.charAt( y + 1 ), keepMacrons );
                        y++;
                    } else
                        break;
                }

                // append the newly generated syllables to the local buffer
                msg = new StringBuffer( msg ).insert( y + 1, symbol + vowelArray.toLowerCase() ).toString();

                // advance
                y += vowelArray.length() + symbol.length();
            }
        }

        // all done, return the converted string
        return msg;
    }

    /**
     * tests if the given char is a vowel
     * @param ch
     * @return
     */
    private static boolean isVowel( char ch ) {
        // special test for unicode entries: 257 aa, 275 ee, 299 ii, 363 uu, 256 AA, 274 EE, 298 II, 362 UU
        return ( Translator.vowels.indexOfKey( ( int ) ch ) >= 0 || Translator.vowels.indexOfValue( ch ) >= 0 );
    }

    /**
     * static method (stripMacron) - strips macrons for special chars
     * @param ch
     * @param keepMacrons
     * @return
     */
    private static char stripMacron( char ch, boolean keepMacrons ) {

        Log.println( Log.DEBUG, "#########", "######### char " + ch + (int) ch );
        // determine appropriate char for conversion
        if ( Translator.vowels.indexOfKey( ( int ) ch ) >= 0 && !keepMacrons )
            return Translator.vowels.get( ( int ) ch );

        return ch;
    }
}
