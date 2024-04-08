package g5.hangestfinal;

import android.icu.util.Calendar;
import android.text.format.Time;
import android.util.Log;

import java.util.Random;

/**
 * Created by Mahajan-PC on 2018-01-18.
 */

public class Randomizer {

    static String gestures[]={"five", "v shape", "c", "palm", "fist", "point" };

    static double THRESHOLD = 0.75;

    static void change()
    {
        Random rand = new Random(System.currentTimeMillis());

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((5 - 0) + 1) + 0;
        chosen_one = gestures[randomNum];
        Log.i("RAND","GENNEE");

    }

    static String chosen_one = "five";

    static Boolean isRunningGA = false;
    static Boolean isRunningGGA = false;
    static Boolean flagA=false;

    static Boolean isRunningGB = false;
    static Boolean isRunningGGB = false;
    static Boolean flagB=false;

    static Boolean isRunningGAL = false;
    static Boolean isRunningGBL = false;
    static void resetFlags()
    {
        isRunningGA = false;
        isRunningGAL = false;
        isRunningGGA = false;
        flagA=false;

        isRunningGB = false;
        isRunningGBL = false;
        isRunningGGB = false;
        flagB=false;
    }


}
