package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.util.Log;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Jan on 15.04.2017.
 * Calculator based on Sunrise/SunsetLib - Java
 */

public class SunriseAndSunsetCalculator {
    private static String [] getArrayOfHours(){
        double longitude = 20;
        double latitude = 50;

        String [] returned = new String[2];

        Location location = new Location(latitude, longitude);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Europe/Warsaw");

        returned[0] = calculator.getOfficialSunriseForDate(Calendar.getInstance());
        returned[1] = calculator.getOfficialSunsetForDate(Calendar.getInstance());

        return returned;
    }

    public static boolean isDaylight(){
        String [] arrayOfHours = getArrayOfHours();

        Long currentTime = (System.currentTimeMillis() - TimeZone.getDefault().getOffset(System.currentTimeMillis())) / 1000;
        Date currentTime2Date = new Date(((currentTime*1000) + TimeZone.getDefault().getOffset(currentTime*1000)));
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String currentTimeStr = format.format(currentTime2Date);

        Date sunriseT = null, sunsetT = null, timeT = null;
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            sunriseT = timeFormat.parse(arrayOfHours[0]);
            sunsetT = timeFormat.parse(arrayOfHours[1]);
            timeT = timeFormat.parse(currentTimeStr);
            long timeTlong = timeT.getTime();
            timeT = new Date(timeTlong);
        } catch (ParseException e) {
            Log.i("aghwd", "Problem with date parsing", e);
        }

        return !(timeT.before(sunriseT) || timeT.after(sunsetT));
    }
}
