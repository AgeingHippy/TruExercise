package org.charles.truexercise.utility;

public class Utilities {

    public static String maskString(String stringToMask) {
        //Mask for logging
        //for simplicity we are assuming strings under 6 characters do not need masking
        String maskedString = stringToMask;
        if (stringToMask != null && stringToMask.length() > 6) {
            StringBuilder stringBuilder = new StringBuilder(stringToMask);
            for (int i = 2; i < stringToMask.length() - 4; i++) {
                stringBuilder.setCharAt(i, '*');
            }
            maskedString = String.valueOf(stringBuilder);
        }
        return maskedString;
    }

}
