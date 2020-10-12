
package com.example.paladashe.sleepy2;

//-------------------------------------------------------------IMPORTS--------------------------------------------------------------------------------------------------------
import android.Manifest;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.text.InputType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.os.Handler;
import java.util.logging.LogRecord;
 import android.app.Service;
import android.content.Intent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;
import android.os.Looper;

public class MainActivity extends AppCompatActivity

{

    // --------------------------- VARIABLES AND CONSTANT USED-------------------------------------------------------------------------------------------------------------------


    // AUDIO VARIABLES AND CONFIGURATIONS------------------------------------------------
    private boolean _isRecording = false;
    private boolean isPlaying = false;
    private recordAudio recordTask;
    PlayAudio playTask;
    private int frequency = 11025;
    private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    File recordingFile;
    private static String TAG = "Permission";
    private static final int RECORD_REQUEST_CODE = 101;

    // VARIABLES SENT TO MATLAB FOR ANALYSIS AND INFORMATIONS----------------------------

    String emailAddress;
    String password;
    String ipAddress;
    String defaultIpAddress = "192.168.0.11";

    // VARIABLE PASSED TO SHUTDOWNSERVER TO SHUT DOWN OR NOT THE MATLAB SERVER--------------

    private int serverIsOn = 1;

    // ADMIN MODE VARIABLE ----------------------------------------------------------------

    private String adminPassword = "admin";
    private String passwordEntered = "";


    //VARIABLES USED FOR TIME MANAGEMENT---------------------------------------------------

    public long _chronoStoppingTime = 0;

    // ANDROID WIDGETS USED ----------------------------------------------------------------

    // Buttons
    Button startRecordingButton, stopRecordingButton, startPlaybackButton, stopPlaybackButton, sendBTN, infoButton, clearValuesButton, stopServerButton;
    // EditText
    EditText emailAddress_available, password_available, dev_frequency_available, ipAddress_available;
    // Chronos
    Chronometer chronometerLight;
    Chronometer chronometerAccelerometer;
    // Switch
    Switch devModeSwitch;
    // Sensors


    Intent LightSensorCLassThread;
    Intent AccelerometerSensorClassThread;
    Thread timeThread;
    boolean timeThreadIsOn=true;

    //-------------------------------------------------------- ON CREATE START--------------------------------------------------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState)

    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // PERMISSIONS FOR AUDIO AND WRITE
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        }

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest2();
        }
        // END OF PERMISSIONS

        // ASSIGNING DESIGN WIDGETS TO ACTUAL VARIABLES AND STARTING STATES-------

        // EditText

        emailAddress_available = (EditText) findViewById(R.id.editEmailAddress);
        password_available = (EditText) findViewById(R.id.editPassword);
        dev_frequency_available = (EditText) findViewById(R.id.devModeEdit);
        ipAddress_available = (EditText) findViewById(R.id.IpAddressEditText);

        // Chronos
        chronometerLight = (Chronometer) findViewById(R.id.chronometer4);

        chronometerAccelerometer = (Chronometer) findViewById(R.id.chronometer5);
        chronometerAccelerometer.setVisibility(View.INVISIBLE);

        // Buttons

        sendBTN = (Button) findViewById(R.id.sendEmail);

        infoButton = (Button) findViewById(R.id.infobutton);

        stopServerButton = (Button) findViewById(R.id.stopServer);

        startRecordingButton = (Button) this.findViewById(R.id.StartRecordingButton);

        stopRecordingButton = (Button) this.findViewById(R.id.StopRecordingButton);
        stopRecordingButton.setEnabled(false);
        stopRecordingButton.setAlpha(0.2f);

        startPlaybackButton = (Button) this.findViewById(R.id.StartPlaybackButton);
        startPlaybackButton.setEnabled(false);
        startPlaybackButton.setAlpha(0.2f);

        stopPlaybackButton = (Button) this.findViewById(R.id.StopPlaybackButton);
        stopPlaybackButton.setEnabled(false);
        stopPlaybackButton.setAlpha(0.2f);

        clearValuesButton = (Button) findViewById(R.id.ResetValuesButton);
        clearValuesButton.setEnabled(false);
        clearValuesButton.setAlpha(0.2f);

        devModeSwitch = (Switch) findViewById(R.id.devSwitch);
        devModeSwitch.setChecked(false);

        // Sensors



        //-------------------------------LISTENERS----------------------------------------------


        // CHRONOMETER LISTENER
        // DEV MODE SWITCH LISTENER
        // START RECORDING BUTTON LISTENER
        // STOP RECORDING BUTTON LISTENER
        // START PLAYBACK BUTTON LISTENER
        // STOP PLAYBACK BUTTON LISTENER
        // SEND BUTTON LISTENER
        // INFO BUTTON LISTENER
        // STOP SERVER BUTTON LISTENER
        // CLEAR VALUES BUTTON LISTENER

        super.onResume();

        //CREATION THREADS
        LightSensorCLassThread = new Intent(this, LightSensorClass.class ); // SERVICE
        AccelerometerSensorClassThread = new Intent(this, AccelerometerSensorClass.class);
        timeThread = new Thread(new UpdateThread());
        timeThread.start();


        // CHRONOMETER LISTENER OVERRIDE FOR HOURS

        chronometerLight.setOnChronometerTickListener(new OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {


                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                cArg.setText(String.format("%02d:%02d:%02d", h, m, s));



            }
        });

        //  DEV MODE SWITCH LISTENER

        devModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked)

                {
                    final AlertDialog.Builder adminPasswordDialog = new AlertDialog.Builder(MainActivity.this);
                    adminPasswordDialog.setTitle("Sleepy");
                    adminPasswordDialog.setMessage("Please enter password");
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    adminPasswordDialog.setView(input);
                    adminPasswordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            passwordEntered = input.getText().toString();
                            if (passwordEntered.equals(adminPassword)) {
                                dev_frequency_available.setVisibility(View.VISIBLE);
                                dev_frequency_available.setEnabled(true);
                                ipAddress_available.setEnabled(true);
                                ipAddress_available.setVisibility(View.VISIBLE);
                                stopServerButton.setVisibility(View.VISIBLE);
                                stopServerButton.setEnabled(true);
                                Toast.makeText(MainActivity.this, "Admin mode activated", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            } else {
                                devModeSwitch.setChecked(false);
                                Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        }
                    });
                    adminPasswordDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            devModeSwitch.setChecked(false);
                            dialog.cancel();
                        }
                    });
                    adminPasswordDialog.show();

                } else {
                    dev_frequency_available.setVisibility(View.INVISIBLE);
                    dev_frequency_available.setEnabled(false);
                    ipAddress_available.setVisibility(View.INVISIBLE);
                    ipAddress_available.setEnabled(false);
                    stopServerButton.setVisibility(View.INVISIBLE);
                    stopServerButton.setEnabled(false);
                }

            }
        });


        if (devModeSwitch.isChecked()) {
            dev_frequency_available.setVisibility(View.VISIBLE);
            dev_frequency_available.setEnabled(true);
            ipAddress_available.setVisibility(View.VISIBLE);
            ipAddress_available.setEnabled(true);
            stopServerButton.setVisibility(View.VISIBLE);
            stopServerButton.setEnabled(true);

        } else {
            dev_frequency_available.setVisibility(View.INVISIBLE);
            dev_frequency_available.setEnabled(false);
            ipAddress_available.setVisibility(View.INVISIBLE);
            ipAddress_available.setEnabled(false);
            stopServerButton.setVisibility(View.INVISIBLE);
            stopServerButton.setEnabled(false);
        }


        // START RECORDING LISTENER
        startRecordingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                record();
                startchronos();
            }
        });

        // STOP RECORDING LISTENER
        stopRecordingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                stopRecording();
                stopChronos();


            }
        });

        // START PLAYBACK LISTENER
        startPlaybackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                play();
            }
        });

        // STOP PLAYBACK LISTENER
        stopPlaybackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                stopPlaying();
            }
        });

        // SEND BUTTON LISTENER
        sendBTN.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                emailAddress = emailAddress_available.getText().toString();
                password = password_available.getText().toString();
                ipAddress = ipAddress_available.getText().toString();
                boolean ipFormatIsCorrect = false;

                if (!devModeSwitch.isChecked()) {
                    Globals.setRecordingFrequency(1);
                }
                if (devModeSwitch.isChecked() && (ipAddress_available.getText().toString().matches("[0-9.]*") && ipAddress_available.getText().toString().length() > 8)) {
                    ipFormatIsCorrect = true;
                    ipAddress = ipAddress_available.getText().toString();
                }
                CharSequence emailAddressToSequence = emailAddress;

                if (emailAddress != null && !password.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddressToSequence).matches() && Globals.LightValues.size() != 0 && serverIsOn == 1) {
                    setRecordingFrequencyInSeconds();
                    long elapsedTime = _chronoStoppingTime - chronometerLight.getBase();
                    Integer recordingDurationInSeconds = (int) elapsedTime / 1000;
                    Integer hours = recordingDurationInSeconds / 3600;
                    Integer minutes = (recordingDurationInSeconds % 3600) / 60;
                    Integer seconds = recordingDurationInSeconds % 60;
                    String recordingToTimeFomat = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    if (!ipFormatIsCorrect) {
                        ipAddress = defaultIpAddress;
                    }

                    final AlertDialog.Builder sendButtonDialog = new AlertDialog.Builder(MainActivity.this);
                    sendButtonDialog.setTitle("Sleepy");
                    sendButtonDialog.setMessage("Do you want to send this recording of :" + recordingToTimeFomat + " ?");
                    sendButtonDialog.setPositiveButton("Yes !", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ShutDownServer serverOn = new ShutDownServer(MainActivity.this, serverIsOn, ipAddress);
                            serverOn.execute();
                            Client client1 = new Client(MainActivity.this, Globals.LightValues, Globals.soundValues, Globals.getRecordingFrequency(), Globals.xValues, Globals.yValues, Globals.zValues, emailAddress, password, ipAddress);
                            client1.execute();
                            Globals._previousLightRecordingTime = 0;
                            Globals._previousAccelerometerRecordingTime = 0;
                            Globals._actualTimeInSecondsForAccelerometer = 0;
                            Globals._actualTimeInSecondsForLight = 0;
                            chronometerLight.setBase(SystemClock.elapsedRealtime());
                            chronometerAccelerometer.setBase(SystemClock.elapsedRealtime());
                            startRecordingButton.setEnabled(true);
                            startRecordingButton.setAlpha(1);


                        }
                    });
                    sendButtonDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    sendButtonDialog.show();

                } else if (serverIsOn == 0) {
                    Toast.makeText(MainActivity.this, "Server is shut down", Toast.LENGTH_SHORT).show();
                    sendBTN.setEnabled(false);
                    sendBTN.setAlpha(0.2f);
                } else if ((emailAddress != null && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddressToSequence).matches()) || (password.isEmpty())) {
                    Toast.makeText(MainActivity.this, "Wrong email address or password", Toast.LENGTH_SHORT).show();
                } else if (Globals.LightValues.size() == 0) {
                    Toast.makeText(MainActivity.this, "No recordings to send", Toast.LENGTH_SHORT).show();
                } else if (ipFormatIsCorrect == false) {
                    Toast.makeText(MainActivity.this, "Wrong Ip Address format", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "A problem occured, try again", Toast.LENGTH_SHORT).show();
                }


            }
        });



        // INFO BUTTON LISTENER
        infoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder infoButtonDialog = new AlertDialog.Builder(MainActivity.this);
                infoButtonDialog.setTitle("Sleepy");
                infoButtonDialog.setMessage("Sleepy main goals are to analyze your sleep by recording surrounding lights, sounds, or mouvements. \n\n" + "                    Sacha Gozlan,2016");
                infoButtonDialog.setPositiveButton("Understood !", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                infoButtonDialog.show();

            }
        });

        // STOP SERVER BUTTON LISTENER
        stopServerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder stopServerButton = new AlertDialog.Builder(MainActivity.this);
                stopServerButton.setTitle("Sleepy");
                stopServerButton.setMessage("Are you sure to shut down the server, you will no longer be able to send datas");
                stopServerButton.setPositiveButton("Shut it down !", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (devModeSwitch.isChecked())
                        {
                            if (ipAddress_available.getText().toString().matches("[0-9.]*") && ipAddress_available.getText().toString().length() > 8) {
                                ipAddress = ipAddress_available.getText().toString();
                            } else {
                                ipAddress = defaultIpAddress;
                            }
                            dialog.cancel();
                            serverIsOn = 0;
                            ShutDownServer serverDown = new ShutDownServer(MainActivity.this, serverIsOn, ipAddress);
                            serverDown.execute();
                        } else {
                            Toast.makeText(MainActivity.this, "Ip Address format is wrong", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                stopServerButton.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                stopServerButton.show();

            }
        });

        // CLEAR VALUES BUTTON LISTENER
        clearValuesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder clearValuesDialog = new AlertDialog.Builder(MainActivity.this);
                clearValuesDialog.setTitle("Sleepy");
                clearValuesDialog.setMessage("Do you want to clear all previous recording values and parameters?");
                clearValuesDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (_isRecording == false) {
                            Globals.LightValues.clear();
                            Globals.soundValues.clear();
                            Globals.xValues.clear();
                            Globals.yValues.clear();
                            Globals.zValues.clear();
                            dialog.cancel();
                            startPlaybackButton.setAlpha(0.2f);
                            stopPlaybackButton.setAlpha(0.2f);
                            startPlaybackButton.setEnabled(false);
                            stopPlaybackButton.setEnabled(false);
                            clearValuesButton.setAlpha(0.2f);
                            clearValuesButton.setEnabled(false);
                            Globals._previousLightRecordingTime = 0;
                            Globals._previousAccelerometerRecordingTime = 0;
                            Globals._actualTimeInSecondsForAccelerometer = 0;
                            Globals._actualTimeInSecondsForLight = 0;
                            chronometerLight.setBase(SystemClock.elapsedRealtime());
                            chronometerAccelerometer.setBase(SystemClock.elapsedRealtime());
                            startRecordingButton.setEnabled(true);
                            startRecordingButton.setAlpha(1);

                            Toast.makeText(MainActivity.this, "Previous recording erased", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Erasing is not possible while recording", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }

                    }
                });
                clearValuesDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                clearValuesDialog.show();

            }
        });


        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sleepyrecording23.pcm");
        path.mkdir();
        try {
            recordingFile = File.createTempFile("SSAMPLE", ".pcm", path);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD Card");
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        timeThreadIsOn=false;
    }

    //---------------------------------------------------------------------------END OF ON CREATE --------------------------------------------------------------------------------------------------


    // --------------------------------------PUBLIC/PROTECTED FUNCTIONS-----------------------------------------------------------------

    //  AUDIO REQUEST AND PERMISSIONS FUNCTIONS

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_REQUEST_CODE);
    }

    protected void makeRequest2() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                RECORD_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                return;
            }
        }
    }


//--------------- RECORDING AND PLAYBACK FUNCTIONS-----------------------------


    public void record() {

        setRecordingFrequencyInSeconds();
        startService(LightSensorCLassThread);
        startService(AccelerometerSensorClassThread);
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
        recordTask = new recordAudio();
        recordTask.execute();
        startRecordingButton.setAlpha(.2f);
        stopRecordingButton.setAlpha(1);

    }

    public void stopRecording() {

        _isRecording = false;

        startRecordingButton.setAlpha(0.2f);
        startRecordingButton.setEnabled(false);
        startPlaybackButton.setEnabled(true);
        startPlaybackButton.setAlpha(1);
        stopRecordingButton.setEnabled(false);
        stopRecordingButton.setAlpha(0.2f);
        clearValuesButton.setEnabled(true);
        clearValuesButton.setAlpha(1);

        stopService(LightSensorCLassThread);
        stopService(AccelerometerSensorClassThread);
        Globals. _previousLightRecordingTime = 0;
        Globals._previousAccelerometerRecordingTime = 0;
        Globals._actualTimeInSecondsForAccelerometer = 0;
        Globals._actualTimeInSecondsForLight = 0;


    }

    public void play() {
        if (Globals.soundValues.size() != 0) {
            startPlaybackButton.setEnabled(false);
            stopPlaybackButton.setEnabled(true);
            playTask = new PlayAudio();
            playTask.execute();
            startPlaybackButton.setAlpha(0.2f);
            stopPlaybackButton.setAlpha(1);
        }

    }

    public void stopPlaying() {
        isPlaying = false;
        stopPlaybackButton.setEnabled(false);
        startPlaybackButton.setEnabled(true);
        stopPlaybackButton.setAlpha(0.2f);
        startPlaybackButton.setAlpha(1);

    }

    //---------TIME MANAGEMENT FUNCTIONS ----------------------------------------

    // 1) CHRONOMETERS

    public void startchronos() {
        chronometerLight.setBase(SystemClock.elapsedRealtime());
        chronometerAccelerometer.setBase(SystemClock.elapsedRealtime());
        chronometerLight.start();
        chronometerAccelerometer.start();
    }

    public void stopChronos() {
        chronometerLight.stop();
        chronometerAccelerometer.stop();
        _chronoStoppingTime = SystemClock.elapsedRealtime();
    }

    public void setActualChronoTimeForLight() {
        long elapsedTime = SystemClock.elapsedRealtime() - chronometerLight.getBase();
        Globals._actualTimeInSecondsForLight = (float) elapsedTime / 1000;


    }

    public void setActualChronoTimeForAccelerometer() {
        long elapsedTime = SystemClock.elapsedRealtime() - chronometerAccelerometer.getBase();
        Globals._actualTimeInSecondsForAccelerometer = (float) elapsedTime / 1000;


    }

    // 2) TIME
    public void RecordLightSensorValues() {



        if (Globals._previousLightRecordingTime == 0) {
            Globals.setTimeToRecordLight(true);
            Globals._previousLightRecordingTime = Globals._actualTimeInSecondsForLight;

        }

        if (Globals._actualTimeInSecondsForLight >= (Globals._previousLightRecordingTime + Globals.getRecordingFrequency())) {
            Globals.setTimeToRecordLight(true);
            System.out.println("actual: "+Globals._actualTimeInSecondsForLight+", previous: "+Globals._previousLightRecordingTime +", recording interval: "+Globals.getRecordingFrequency());
            Globals._previousLightRecordingTime = Globals._actualTimeInSecondsForLight;

        }

    }

    public void RecordAccelerometerSensorValues() {



        if (Globals._previousAccelerometerRecordingTime == 0) {
            Globals.setTimeToRecordAccelerometer(true);
            Globals._previousAccelerometerRecordingTime = Globals._actualTimeInSecondsForAccelerometer;

        }

        if (Globals._actualTimeInSecondsForAccelerometer >= (Globals._previousAccelerometerRecordingTime + Globals.getRecordingFrequency())) {
            Globals.setTimeToRecordAccelerometer(true);
            Globals._previousAccelerometerRecordingTime = Globals._actualTimeInSecondsForAccelerometer;
        }

    }

    public void setRecordingFrequencyInSeconds() {
        if (devModeSwitch.isChecked() && (dev_frequency_available.getText().toString().matches("[0-9.]*") && dev_frequency_available.getText().toString().length() >= 1)) {
            float recordingFrequencyInHertz = Float.parseFloat(dev_frequency_available.getText().toString());
            Globals.setRecordingFrequency(1 / recordingFrequencyInHertz);
        } else {
            Globals.setRecordingFrequency(1);
        }
    }


//----------------------------------------------------------------------------------------------------------------------

    //----------------------- RECORDING AND PLAYBACK FUNCTIONS RUNNING ON A BACKGROUND THREAD------------------------------

    // RECORDING

    public class recordAudio extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) { // s'execute quand on lance le thread Asynctas
            _isRecording = true;

            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(recordingFile)));
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
                int r = 0;
                short[] buffer = new short[bufferSize];
                audioRecord.startRecording();
                publishProgress("Start recording");
                while (_isRecording) {

                    int sum = 0;
                    int BufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    for (int i = 0; i < BufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                        sum += buffer[i] * buffer[i];
                    }
                    if (BufferReadResult > 0) {
                        Globals.soundValues.add((int) Math.sqrt(sum / BufferReadResult));
                    }


                    r++;

                }
                publishProgress("Recording stopped");
                audioRecord.stop();
                audioRecord.release();
                dos.close();
            } catch (FileNotFoundException e) {
                System.out.println(e);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            startRecordingButton.setEnabled(true);
            stopRecordingButton.setEnabled(false);

        }


        protected void onProgressUpdate(String... message)

        {


            if (message[0] == "Start recording") {
                Toast.makeText(MainActivity.this, "Recording stopped", Toast.LENGTH_SHORT).cancel();
                Toast.makeText(MainActivity.this, "Recording started", Toast.LENGTH_SHORT).show();

            } else if (message[0] == "Recording stopped") {
                Toast.makeText(MainActivity.this, "Recording started", Toast.LENGTH_SHORT).cancel();
                Toast.makeText(MainActivity.this, "Recording stopped", Toast.LENGTH_SHORT).show();

            }

        }
    }


    // PLAYBACK

    private class PlayAudio extends AsyncTask<Void, String, Void>  {
        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            short[] Audiodata = new short[bufferSize / 4];
            publishProgress("Start playing");
            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(recordingFile)));
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
                audioTrack.play();
                while (isPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < Audiodata.length) {
                        Audiodata[i] = dis.readShort();
                        i++;
                    }
                    audioTrack.write(Audiodata, 0, Audiodata.length);
                }

                publishProgress("Stop playing");
                dis.close();

            } catch (Throwable t) {
                Log.e("AudioTrack", "Playback Failed");
            }

            return null;
        }

        protected void onProgressUpdate(String... message)

        {


            if (message[0] == "Start playing") {
                Toast.makeText(MainActivity.this, "Playback stopped", Toast.LENGTH_SHORT).cancel();
                Toast.makeText(MainActivity.this, "Recording started", Toast.LENGTH_SHORT).show();

            } else if (message[0] == "Stop playing") {
                Toast.makeText(MainActivity.this, "Playback started", Toast.LENGTH_SHORT).cancel();
                Toast.makeText(MainActivity.this, "Playback stopped", Toast.LENGTH_SHORT).show();

            }

        }
    }
public static class LightSensorClass extends Service implements SensorEventListener {


    private SensorManager sensorManager = null;
    private Sensor LightSensor = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HandlerThread handlerThread1 = new HandlerThread("MyHandlerThread1",Thread.MAX_PRIORITY);
        handlerThread1.start();
        Looper looper1 = handlerThread1.getLooper();
        Handler handler1 = new Handler(looper1);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        LightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this,LightSensor,
                SensorManager.SENSOR_DELAY_FASTEST,handler1);
        return START_STICKY;
    }

    @Override
    public void onCreate(){

        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy()
    {

        sensorManager.unregisterListener(this);
    }



    public void onSensorChanged(SensorEvent event) {



        if (Globals.getTimeToRecordLight()==true) {
                Globals.LightValues.add((int) event.values[0]);
                System.out.println(" light " + Globals.LightValues.size());
                Globals.setTimeToRecordLight(false);
            }


        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    };



    public static class AccelerometerSensorClass extends Service implements SensorEventListener {


        private SensorManager sensorManager = null;
        private Sensor AccelerometerSensor = null;
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            System.out.println(Thread.currentThread().getName());

            HandlerThread handlerThread2 = new HandlerThread("MyHandlerThread2",Thread.MAX_PRIORITY);
            handlerThread2.start();
            Looper looper2 = handlerThread2.getLooper();
            Handler handler2 = new Handler(looper2);

            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            AccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this,AccelerometerSensor,
                    SensorManager.SENSOR_DELAY_FASTEST,handler2);
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        @Override
        public void onDestroy()
        {

            sensorManager.unregisterListener(this);
        }



        public void onSensorChanged(SensorEvent event) {


            if (Globals.getTimeToRecordAccelerometer()==true) {

                Globals.xValues.add((double) event.values[0]);
                System.out.println(" accelerometer " + Globals.xValues.size());
                Globals.yValues.add((double) event.values[1]);
                Globals.zValues.add((double) event.values[2]);
                Globals.setTimeToRecordAccelerometer(false);
                }
            }


        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    };


    public class UpdateThread implements Runnable {

        @Override
        public void run() {
            while(timeThreadIsOn)
            {
                if (_isRecording == true) {
                    setActualChronoTimeForLight();
                    RecordLightSensorValues();
                    setActualChronoTimeForAccelerometer();
                    RecordAccelerometerSensorValues();
                }
            }
        }
    }


}