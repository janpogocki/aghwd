package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.graphics.Bitmap;
import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jan on 17.07.2016.
 * Temporary storage for some infos
 */

public class Storage {
    public static String albumNumber = "";
    public static String nameAndSurname = "";
    public static String peselNumber = "";
    public static String choosenMultiKierunekValue = "";
    public static String syllabusURL = "";
    public static String syllabusURLlinkDepartment = "";
    public static String sharedPreferencesStartScreen = "";
    public static String feedbackCrashList = "";
    public static int currentSemester = 0;
    public static int currentSemesterListPointer = 0;
    public static int currentSemesterListPointerPartialMarks = 0;
    public static int currentSemesterListPointerFiles = 0;
    public static long timeOfLastConnection = 0;
    public static Bitmap photoUser = null;
    public static Boolean openedBrowser = false;
    public static Boolean multiKierunek = false;
    public static Boolean firstRunMarksExplorer = true;
    public static boolean nightMode = false;
    public static HashMap<Integer, String> currentSemesterHTML = new HashMap<>();
    public static HashMap<Integer, List<String>> currentSemesterPartialMarksHTML = new HashMap<>();
    public static HashMap<Integer, String> currentFilesHTML = new HashMap<>();
    public static HashMap<Integer, List<List<String>>> currentFilesDocsHTML = new HashMap<>();
    public static List<List<String>> summarySemesters = new ArrayList<>();
    public static List<List<String>> groupsAndModules = new ArrayList<>();
    public static List<Appointment> schedule = new ArrayList<>();
    public static List<List<String>> diploma = new ArrayList<>();
    public static List<List<String>> skosList = new ArrayList<>();
    public static List<String> universityStatus = new ArrayList<>();
    public static List<String> multiKierunekValues = new ArrayList<>();
    public static List<String> multiKierunekLabelNames = new ArrayList<>();
    public static List<LabelListAndList<String>> scholarships = new ArrayList();
    public static int scheduleStatus;

    public static String getSemesterNumberById(int id) {
        return Storage.summarySemesters.get(id).get(2);
    }

    public static void appendCrash(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Storage.feedbackCrashList += sw.toString() + "\n\n==========\n\n";
    }

    public static String getUniversityStatusHash(){
        String strToMD5 = universityStatus.get(0)+universityStatus.get(1)+universityStatus.get(2)+universityStatus.get(4)+universityStatus.get(5)+universityStatus.get(9);

        try {
            return new String(Hex.encodeHex(MessageDigest.getInstance("MD5").digest(strToMD5.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }

        return "";
    }

    public static void clearStorage(){
        albumNumber = "";
        nameAndSurname = "";
        peselNumber = "";
        choosenMultiKierunekValue = "";
        syllabusURL = "";
        syllabusURLlinkDepartment = "";
        sharedPreferencesStartScreen = "";
        currentSemester = 0;
        currentSemesterListPointer = 0;
        currentSemesterListPointerPartialMarks = 0;
        currentSemesterListPointerFiles = 0;
        timeOfLastConnection = 0;
        photoUser = null;
        openedBrowser = false;
        multiKierunek = false;
        firstRunMarksExplorer = true;
        currentSemesterHTML = new HashMap<>();
        currentSemesterPartialMarksHTML = new HashMap<>();
        currentFilesHTML = new HashMap<>();
        currentFilesDocsHTML = new HashMap<>();
        summarySemesters = new ArrayList<>();
        groupsAndModules = new ArrayList<>();
        schedule = new ArrayList<>();
        diploma = new ArrayList<>();
        skosList = new ArrayList<>();
        universityStatus = new ArrayList<>();
        multiKierunekValues = new ArrayList<>();
        multiKierunekLabelNames = new ArrayList<>();
        scholarships = new ArrayList();
        scheduleStatus = -1;
    }
}
