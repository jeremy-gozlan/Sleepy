package com.example.paladashe.sleepy2;

import java.util.Vector;

/**
 * Created by jeremygozlan on 3/4/17.
 */

public class Globals
{

    public static float _recordingFrequencyInSeconds = 1;


    public static float _actualTimeInSecondsForLight = 0;
    public static float _actualTimeInSecondsForAccelerometer = 0;
    public static float _previousLightRecordingTime = 0;
    public static float _previousAccelerometerRecordingTime = 0;

    public  static boolean _timeToRecordLight=false;
    public static boolean  _timeToRecordAccelerometer=false;
    public static Vector<Integer> LightValues =new Vector < Integer>();
    public static Vector<Double> xValues =new Vector <>();
    public static Vector<Double> yValues =new Vector <>();
    public static Vector<Double> zValues =new Vector <>();
    public static Vector<Integer> soundValues = new Vector<Integer>();



    public static synchronized  boolean getTimeToRecordAccelerometer(){
        return _timeToRecordAccelerometer;
    }

    public static synchronized  boolean setTimeToRecordAccelerometer(boolean bool){
        return _timeToRecordAccelerometer=bool;
    }


    public static synchronized  boolean getTimeToRecordLight(){
        return _timeToRecordLight;
    }

    public static synchronized  void setTimeToRecordLight(boolean bool)
    {
        _timeToRecordLight=bool;
    }

    public static synchronized  float getRecordingFrequency(){return _recordingFrequencyInSeconds;}
    public static synchronized void setRecordingFrequency(float frequency) { _recordingFrequencyInSeconds = frequency;}


}
