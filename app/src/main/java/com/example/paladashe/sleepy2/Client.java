package com.example.paladashe.sleepy2;


import android.os.AsyncTask;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
import android.content.Context;
import android.app.ProgressDialog;
import android.widget.Toast;



public class  Client  extends AsyncTask<Void,String,Void >{

    private Socket socket = null;
    private boolean isConnected = false;
    Vector< Integer> myInputLight= null;
    Vector< Integer> myInputAudio=null;
    Vector< Double> xInputValues= null;
    Vector< Double> yInputValues=null;
    Vector< Double> zInputValues= null;
    String myInputEmail;
    String myPassword;
    String myServerIpAddress;
    Context myContext;
    ProgressDialog progress;
    float myInputFrequency;





    public Client(Context context,Vector <Integer> Light,Vector <Integer> Audio,Float Frequency,Vector <Double> x,Vector <Double> y,Vector <Double> z,String emailAddress,String password,String ipAddress)
    {   myContext=context;
        myInputLight= Light;
        myInputAudio=Audio;
        myInputFrequency=Frequency;
        xInputValues=x;
        yInputValues=y;
        zInputValues=z;
        myInputEmail=emailAddress;
        myPassword=password;
        myServerIpAddress = ipAddress;
        this.progress = new ProgressDialog(myContext);

    }


    @Override
    protected Void doInBackground(Void... params) {

        while (!isConnected) {
            try {
                Integer portNumber = 35220;
                Boolean connectionMade = false;
                publishProgress("Trying to connect");
                while(connectionMade==false)
                {

                    try {
                        socket = new Socket(myServerIpAddress, portNumber);
                        isConnected = true;
                        connectionMade= true;
                        System.out.println("Connected");

                    }
                    catch(Exception e) {
                        try
                        {
                            Thread.sleep(1000);//2 seconds
                        }
                        catch(InterruptedException ie){
                            ie.printStackTrace();
                        }
                    }
                }

                publishProgress("Connected");
                DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream objectOutput=new ObjectOutputStream(os);


                objectOutput.writeObject(myInputLight);
                objectOutput.flush();
                System.out.println("Light values SENT");

                objectOutput.writeObject(myInputAudio);
                objectOutput.flush();
                System.out.println("Audio values SENT");

                os.writeFloat(myInputFrequency);
                os.flush();
                System.out.println("Audio frequency SENT");

                objectOutput.writeObject(xInputValues);
                objectOutput.flush();
                objectOutput.writeObject(yInputValues);
                objectOutput.flush();
                objectOutput.writeObject(zInputValues);
                objectOutput.flush();
                System.out.println("Accelerometer values SENT");

                objectOutput.writeObject(myInputEmail);
                objectOutput.flush();
                System.out.println("Email address SENT");

                objectOutput.writeObject(myPassword);
                objectOutput.flush();
                System.out.println("Email password SENT");

                System.out.println("Message sent to the server");
                publishProgress("Data sent");

                myInputLight.clear();
                myInputAudio.clear();
                myInputFrequency=0;
                xInputValues.clear();
                yInputValues.clear();
                zInputValues.clear();
                myInputEmail="";
                myPassword="";



                socket.close();

            } catch (SocketException se) {
                se.printStackTrace();
// System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();

            }

        }

        return null;
    }

    @Override
    protected void onCancelled() {
        progress.dismiss();
    }

    protected void onProgressUpdate(String... message)
            
    {

        
       if(message[0] == "Trying to connect")
       {
        progress.setTitle("Connection Manager");
        progress.setMessage("Trying to connect..");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
       }
       else if (message[0]=="Connected")
       {
           progress.dismiss();
           Toast.makeText(myContext, "Connected!", Toast.LENGTH_SHORT).show();
       }
        else if (message[0]=="Data sent")
       {
           progress.dismiss();
           Toast.makeText(myContext, "Data sent for analysis", Toast.LENGTH_SHORT).show();
       }

    }

}




