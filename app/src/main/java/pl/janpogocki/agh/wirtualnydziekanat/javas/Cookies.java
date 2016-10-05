package pl.janpogocki.agh.wirtualnydziekanat.javas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 15.07.2016.
 * Managing cookies received from Wirtualna Uczelnia
 */

public class Cookies {
    private static List<String> cookiesStorage;
    public static Boolean setList = false;

    public static void setCookies(List<String> _cookies){
        cookiesStorage = new ArrayList<>(_cookies);
        removeSemicolons();
    }

    public static void updateCookies(List<String> _cookies){
        for (int i=0; i<_cookies.size(); i++){
            for (int j=0; j<cookiesStorage.size(); j++){
                if (cookiesStorage.get(j).split("=")[0].equals(_cookies.get(i).split("=")[0])){
                    cookiesStorage.remove(j);
                    cookiesStorage.add(j, _cookies.get(i));
                }
            }
        }
        removeSemicolons();
    }

    public static String getCookies(){
        String ret = "";

        for (int i=0; i<cookiesStorage.size(); i++){
            ret += cookiesStorage.get(i) + "; ";
        }

        return ret;
    }

    private static void removeSemicolons(){
        for (int i=0; i<cookiesStorage.size(); i++){
            String toChange = cookiesStorage.get(i).split(";")[0];
            cookiesStorage.remove(i);
            cookiesStorage.add(i, toChange);
        }
    }
}
