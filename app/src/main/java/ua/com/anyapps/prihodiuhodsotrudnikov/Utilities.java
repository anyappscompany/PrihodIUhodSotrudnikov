package ua.com.anyapps.prihodiuhodsotrudnikov;

import android.content.Context;

import java.util.Random;

public class Utilities {
    public static String generateActivationKey(int _keyLength){
        String characters = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(_keyLength);

        for (int i = 0; i < _keyLength; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
