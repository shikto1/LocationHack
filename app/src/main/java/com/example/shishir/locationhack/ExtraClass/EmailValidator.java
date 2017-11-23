package com.example.shishir.locationhack.ExtraClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Shishir on 11/23/2017.
 */

public class EmailValidator {
    public static boolean emailIsValid(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
