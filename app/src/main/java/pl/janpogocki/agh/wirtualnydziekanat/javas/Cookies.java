package pl.janpogocki.agh.wirtualnydziekanat.javas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 15.07.2016.
 * Managing cookies received from AGH Wirtualna Uczelnia
 */

public class Cookies {
    private static List<String> cookiesStorage = null;
    public static Boolean setList = false;

    public static void setCookies(List<String> _cookies){
        cookiesStorage = new ArrayList<>(_cookies);
        Cookies.removeSemicolons();
        setList = true;
    }

    public static void updateCookies(List<String> _cookies){
        if (setList && cookiesStorage.size() > 0) {
            for (int i = 0; i < _cookies.size(); i++) {
                for (int j = 0; j < cookiesStorage.size(); j++) {
                    if (cookiesStorage.get(j).split("=")[0].equals(_cookies.get(i).split("=")[0])) {
                        cookiesStorage.remove(j);
                        cookiesStorage.add(j, _cookies.get(i));
                    }
                }
            }
            Cookies.removeSemicolons();
        }
        else
            Cookies.setCookies(_cookies);
    }

    public static String getCookies(){
        String ret = "";

        if (setList && cookiesStorage != null) {
            for (int i = 0; i < cookiesStorage.size(); i++) {
                ret += cookiesStorage.get(i) + "; ";
            }
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