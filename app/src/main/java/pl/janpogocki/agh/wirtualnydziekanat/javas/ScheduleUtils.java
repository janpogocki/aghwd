package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by Jan on 29.10.2017.
 * Schedule Utils Class
 */

public class ScheduleUtils {
    public static void saveNewAppointment(Context c, Appointment appointment){
        try {
            String filename = Storage.getUniversityStatusHash() + "_mycal.json";
            File file = new File(c.getFilesDir() + "/" + filename);

            if (file.exists()) {
                StringBuilder jsonFromFile = new StringBuilder();
                FileInputStream inputStream = c.openFileInput(filename);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = r.readLine()) != null) {
                    jsonFromFile.append(line);
                }
                r.close();
                inputStream.close();

                JSONArray jsonArray = new JSONArray(jsonFromFile.toString());

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("startTimestamp", appointment.startTimestamp);
                jsonObject.put("stopTimestamp", appointment.stopTimestamp);
                jsonObject.put("name", appointment.name);
                jsonObject.put("description", appointment.description);
                jsonObject.put("location", appointment.location);
                jsonObject.put("lecture", appointment.lecture);
                jsonObject.put("aghEvent", appointment.aghEvent);
                jsonObject.put("tag", appointment.tag);
                jsonObject.put("group", appointment.group);
                jsonObject.put("showDateBar", appointment.showDateBar);

                jsonArray.put(jsonObject);

                // save to file
                PrintWriter out = new PrintWriter(file);
                out.print(jsonArray.toString());
                out.close();
            } else {
                JSONArray jsonArray = new JSONArray();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("startTimestamp", appointment.startTimestamp);
                jsonObject.put("stopTimestamp", appointment.stopTimestamp);
                jsonObject.put("name", appointment.name);
                jsonObject.put("description", appointment.description);
                jsonObject.put("location", appointment.location);
                jsonObject.put("lecture", appointment.lecture);
                jsonObject.put("aghEvent", appointment.aghEvent);
                jsonObject.put("tag", appointment.tag);
                jsonObject.put("group", appointment.group);
                jsonObject.put("showDateBar", appointment.showDateBar);

                jsonArray.put(jsonObject);

                // save to file
                PrintWriter out = new PrintWriter(file);
                out.print(jsonArray.toString());
                out.close();
            }
        } catch (Exception e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }
    }

    public static void addOrChangeOrRemoveTag(Context c, Appointment appointment, int tag, @Nullable List<Appointment> listOfAppointments){
        try {
            JSONObject jsonObject = null;
            String filename = Storage.getUniversityStatusHash() + "_tags.json";
            File file = new File(c.getFilesDir() + "/" + filename);

            if (file.exists()) {
                StringBuilder jsonFromFile = new StringBuilder();
                FileInputStream inputStream = c.openFileInput(filename);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = r.readLine()) != null) {
                    jsonFromFile.append(line);
                }
                r.close();
                inputStream.close();

                jsonObject = new JSONObject(jsonFromFile.toString());
            }

            String keyName = appointment.startTimestamp + appointment.stopTimestamp + appointment.name + appointment.description + appointment.aghEvent;

            // if single event tag
            if (listOfAppointments == null) {
                if (jsonObject == null) {
                    jsonObject = new JSONObject();
                    jsonObject.put(keyName, tag);
                } else {
                    // tag not exists => add
                    if (!jsonObject.has(keyName))
                        jsonObject.put(keyName, tag);
                    // new tag == old tag => delete
                    else if (jsonObject.has(keyName) && jsonObject.getInt(keyName) == tag)
                        jsonObject.remove(keyName);
                    // new tag != old tag => delete old, add new
                    else if (jsonObject.has(keyName) && jsonObject.getInt(keyName) != tag){
                        jsonObject.remove(keyName);
                        jsonObject.put(keyName, tag);
                    }
                }
            }
            // if multiple event tag
            else {
                // check whether add or remove tag
                boolean addtag;
                if (jsonObject == null) {
                    addtag = true;
                    jsonObject = new JSONObject();
                }
                else if (jsonObject.has(keyName) && jsonObject.getInt(keyName) == tag)
                    addtag = false;
                else
                    addtag = true;

                // iterate listOfAppointments
                for (int i=0; i<listOfAppointments.size(); i++){
                    if (listOfAppointments.get(i).name.equals(appointment.name) && listOfAppointments.get(i).description.equals(appointment.description)){
                        String tempKeyName = listOfAppointments.get(i).startTimestamp + listOfAppointments.get(i).stopTimestamp + listOfAppointments.get(i).name + listOfAppointments.get(i).description + listOfAppointments.get(i).aghEvent;

                        if (!jsonObject.has(tempKeyName) && addtag)
                            jsonObject.put(tempKeyName, tag);
                        else if (jsonObject.has(tempKeyName) && !addtag)
                            jsonObject.remove(tempKeyName);
                    }
                }
            }

            // save updated new jsonObject to file
            PrintWriter out = new PrintWriter(file);
            out.print(jsonObject.toString());
            out.close();
        } catch (Exception e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }
    }

    public static void copyAppointment(Context c, Appointment appointment, @Nullable List<Appointment> listOfAppointments){
        // single event copy
        if (listOfAppointments == null){
            Appointment currentAppointment = new Appointment(appointment);
            currentAppointment.aghEvent = false;
            currentAppointment.group = 0;

            saveNewAppointment(c, currentAppointment);
        }
        // multiple event copy
        else {
            // iterate listOfAppointments
            for (int i=0; i<listOfAppointments.size(); i++){
                if (listOfAppointments.get(i).name.equals(appointment.name) && listOfAppointments.get(i).description.equals(appointment.description)){
                    Appointment currentAppointment = new Appointment(listOfAppointments.get(i));
                    currentAppointment.aghEvent = false;
                    currentAppointment.group = 0;

                    saveNewAppointment(c, currentAppointment);
                }
            }
        }
    }

    public static void editAppointment(Context c, Appointment oldAppointment, Appointment newAppointment){
        removeAppointment(c, oldAppointment);
        saveNewAppointment(c, newAppointment);

        if (oldAppointment.tag != -1)
            addOrChangeOrRemoveTag(c, newAppointment, oldAppointment.tag, null);
    }

    public static void removeAppointment(Context c, Appointment appointment){
        try {
            String filename = Storage.getUniversityStatusHash() + "_mycal.json";
            File file = new File(c.getFilesDir() + "/" + filename);

            if (file.exists()) {
                StringBuilder jsonFromFile = new StringBuilder();
                FileInputStream inputStream = c.openFileInput(filename);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = r.readLine()) != null) {
                    jsonFromFile.append(line);
                }
                r.close();
                inputStream.close();

                JSONArray jsonArray = new JSONArray(jsonFromFile.toString());
                JSONArray newJsonArray = new JSONArray();

                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject currentJsonObject = jsonArray.getJSONObject(i);

                    if (currentJsonObject.getLong("startTimestamp") == appointment.startTimestamp
                            && currentJsonObject.getLong("stopTimestamp") == appointment.stopTimestamp
                            && currentJsonObject.getString("name").equals(appointment.name)
                            && currentJsonObject.getString("description").equals(appointment.description)
                            && currentJsonObject.getString("location").equals(appointment.location)
                            && currentJsonObject.getBoolean("aghEvent") == appointment.aghEvent
                            && currentJsonObject.getDouble("group") == appointment.group){
                        // not put - remove from tags, if necessary
                        if (appointment.tag != -1)
                            addOrChangeOrRemoveTag(c, appointment, appointment.tag, null);
                    }
                    else {
                        newJsonArray.put(currentJsonObject);
                    }
                }

                // save to file
                PrintWriter out = new PrintWriter(file);
                out.print(newJsonArray.toString());
                out.close();
            }
        } catch (Exception e) {
            Log.i("aghwd", "aghwd", e);
            Storage.appendCrash(e);
        }
    }
}
