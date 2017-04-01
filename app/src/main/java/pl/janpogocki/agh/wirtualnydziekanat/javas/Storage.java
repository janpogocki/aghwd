package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.res.Resources;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jan on 17.07.2016.
 * Temporary storage for some infos
 */

public class Storage {
    public static String albumNumber = "";
    public static String nameAndSurname = "";
    public static String photoUserURL = "";
    public static String peselNumber = "";
    public static String choosenMultiKierunekValue = "";
    public static String syllabusURL = "";
    public static String syllabusURLlinkDepartment = "";
    public static String sharedPreferencesStartScreen = "";
    public static int currentSemester = 0;
    public static int currentSemesterListPointer = 0;
    public static int currentSemesterListPointerPartialMarks = 0;
    public static long timeOfLastConnection = 0;
    public static Resources resource = null;
    public static Bitmap photoUser = null;
    public static Boolean oneMoreBackPressedButtonMeansExit = false;
    public static Boolean openedBrowser = false;
    public static Boolean loggedIn = false;
    public static Boolean multiKierunek = false;
    public static Boolean firstRunMarksExplorer = true;
    public static HashMap<Integer, String> currentSemesterHTML = new HashMap<>();
    public static HashMap<Integer, List<String>> currentSemesterPartialMarksHTML = new HashMap<>();
    public static List<List<String>> summarySemesters = new ArrayList<>();
    public static List<List<String>> groupsAndModules = new ArrayList<>();
    public static List<List<String>> schedule = new ArrayList<>();
    public static List<List<String>> diploma = new ArrayList<>();
    public static List<List<String>> skosList = new ArrayList<>();
    public static List<String> universityStatus = new ArrayList<>();
    public static List<String> multiKierunekValues = new ArrayList<>();
    public static List<String> multiKierunekLabelNames = new ArrayList<>();

    public static String getSemesterNumberById(int id){
        return Storage.summarySemesters.get(id).get(2);
    }

    public static void clearStorage(){
        albumNumber = "";
        nameAndSurname = "";
        photoUserURL = "";
        peselNumber = "";
        choosenMultiKierunekValue = "";
        syllabusURL = "";
        syllabusURLlinkDepartment = "";
        sharedPreferencesStartScreen = "";
        currentSemester = 0;
        currentSemesterListPointer = 0;
        currentSemesterListPointerPartialMarks = 0;
        timeOfLastConnection = 0;
        resource = null;
        photoUser = null;
        oneMoreBackPressedButtonMeansExit = false;
        openedBrowser = false;
        loggedIn = false;
        multiKierunek = false;
        firstRunMarksExplorer = true;
        currentSemesterHTML = new HashMap<>();
        currentSemesterPartialMarksHTML = new HashMap<>();
        summarySemesters = new ArrayList<>();
        groupsAndModules = new ArrayList<>();
        schedule = new ArrayList<>();
        diploma = new ArrayList<>();
        skosList = new ArrayList<>();
        universityStatus = new ArrayList<>();
        multiKierunekValues = new ArrayList<>();
        multiKierunekLabelNames = new ArrayList<>();
    }
}
