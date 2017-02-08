package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jan on 17.07.2016.
 * Temporary storage for some infos
 */

public class Storage {
    public static String albumNumber, nameAndSurname, photoUserURL, peselNumber, choosenMultiKierunekValue = "";
    public static int currentSemester, currentSemesterListPointer = 0;
    public static Bitmap photoUser = null;
    public static Boolean oneMoreBackPressedButtonMeansExit = false;
    public static Boolean multiKierunek = false;
    public static HashMap<Integer, String> currentSemesterHTML = new HashMap<>();
    public static List<List<String>> summarySemesters = new ArrayList<>();
    public static List<List<String>> groupsAndModules = new ArrayList<>();
    public static List<String> multiKierunekValues = new ArrayList<>();
    public static List<String> multiKierunekLabelNames = new ArrayList<>();

    public static int getSemesterNumberById(int id){
        return Integer.parseInt(summarySemesters.get(id).get(2));
    }

    public static void multiKierunekClear(){
        multiKierunek = false;
        multiKierunekValues = new ArrayList<>();
        multiKierunekLabelNames = new ArrayList<>();
        choosenMultiKierunekValue = "";
    }
}
