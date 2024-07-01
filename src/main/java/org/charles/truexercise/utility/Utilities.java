package org.charles.truexercise.utility;

public class Utilities {

    public static String maskString(String stringToMask) {
        //Mask for logging
        //for simplicity we are assuming strings under 5 characters do not need masking
        StringBuilder maskedString = new StringBuilder(stringToMask);
        if (stringToMask.length() > 6) {
            for (int i = 2; i < stringToMask.length() - 4; i++) {
                maskedString.setCharAt(i, '*');
            }
        }
        return String.valueOf(maskedString);
    }

}
