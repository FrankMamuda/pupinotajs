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
// class (Translator)
//
public class Translator {
    //
    // method (translate) - the actual translation algorithm
    //
    public static String translate( final String inputBuffer, final String symbol, boolean keepMacrons ) {
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
            if ( Translator.isVowel( ch )) {
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

    //
    // static method (isVowel) - tests if the given char is a vowel
    //
    private static boolean isVowel( char ch ) {
        // special test for unicode entries: 257 aa, 275 ee, 299 ii, 363 uu, 256 AA, 274 EE, 298 II, 362 UU
        if ( ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u' || ch == 'y' ||
                ch == ( char ) 257 || ch == ( char ) 275 || ch == ( char ) 299 || ch == ( char ) 363 ||
                ch == 'A' || ch == 'E' || ch == 'I' || ch == 'O' || ch == 'U' || ch == 'Y' ||
                ch == ( char ) 256 || ch == ( char ) 274 || ch == ( char ) 298 || ch == ( char ) 362 ) {
            return true;
        }
        return false;
    }

    //
    // static method (stripMacron) - strips macrons for special chars
    //
    private static char stripMacron( char ch, boolean keepMacrons ) {
        // settings option - keepMacrons
        if ( keepMacrons )
            return ch;

        // determine appropriate char for conversion
        switch ( ( int ) ch ) {
            // aa
            case 257:
                return 'a';

            // ee
            case 275:
                return 'e';

            // ii
            case 299:
                return 'i';

            // uu
            case 363:
                return 'u';

            // AA
            case 256:
                return 'A';

            // EE
            case 274:
                return 'E';

            // II
            case 298:
                return 'I';

            // UU
            case 362:
                return 'U';
        }
        return ch;
    }
}
