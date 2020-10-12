package com.example.paladashe.sleepy2;


    import android.os.AsyncTask;
    import java.io.DataOutputStream;
    import java.io.IOException;
    import java.io.ObjectInputStream;
    import java.io.ObjectOutputStream;
    import java.net.Socket;
    import java.net.SocketException;

    import android.content.Context;
    import android.app.ProgressDialog;
    import android.widget.Toast;


    public class  ShutDownServer extends AsyncTask<Void,String,Void >{

        private Socket socket = null;



        private DataOutputStream dOut=null;
        private boolean isConnected = false;
        Context myContext;
        ProgressDialog progress;
        int serverIsOn;
        String myServerIpAddress;



        public ShutDownServer(Context context,Integer serverIsOn,String IpAddress)
        {   myContext=context;
            this.serverIsOn=serverIsOn;
            this.progress = new ProgressDialog(myContext);
            this.myServerIpAddress=IpAddress;

        }

        @Override
        protected Void doInBackground(Void... params) {

            while (!isConnected) {
                try {
                    Integer portNumber = 35221;
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


                    os.writeInt(serverIsOn);
                    os.flush();
                    System.out.println("Server settings SENT");

                    System.out.println("Message sent to the server");
                    if(serverIsOn==0)
                    {
                        publishProgress("Data sent for shutting down");
                    }

                   serverIsOn=0;



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
                progress.setMessage("Trying to connect to the server");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
            }
            else if (message[0]=="Connected")
            {
                progress.dismiss();
                Toast.makeText(myContext, "Connected to server !", Toast.LENGTH_SHORT).show();
            }
            else if (message[0]=="Data sent for shutting down")
            {
                progress.dismiss();
                Toast.makeText(myContext, "Server down, please restart the app and turn the server on if you want to send data again", Toast.LENGTH_LONG).show();
            }


        }

    }







