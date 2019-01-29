package edu.kennesaw.core.converters;

public abstract class Hexadecimal{
    public static int toInt(String hex) throws NumberFormatException{
        hex = stripFormatter(hex);
        return Integer.parseInt(hex, 16);
    }

    private static String stripFormatter(String value){
        return value.replace("0x", "");
    }
}
