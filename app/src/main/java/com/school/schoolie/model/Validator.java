package com.school.schoolie.model;

import java.util.regex.Pattern;

public class Validator {
    private static String mEmailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static String mPasswordRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,20}$";

    public static boolean isValidEmail(String value)
    {
        return Pattern.compile(mEmailRegex).matcher(value).matches() && value.endsWith(".edu");
    }

    public static boolean isValidPassword(String value)
    {
        return Pattern.compile(mPasswordRegex).matcher(value).matches();
    }


    public static boolean isNumeric(String value)
    {
        if (value == null || value.length() == 0) {
            return false;
        }

        for (char c : value.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    public static String invalidEmailMessage = "Email format error. Please use school email (ie. example@csulb.edu)";

    public static String invalidPasswordMessage =
            "Password must be between 8 and 20 characters with at least one digit, one uppercase alphabet, and one lowercase alphabet";

    public static String mustBeNumericMessage = "Must be numeric";
}
