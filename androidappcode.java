package com.example.angelomathai.smartlife;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    Button btnPaired;
    ListView devicelist;
    TextView t1;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPaired = findViewById(R.id.button);
        devicelist = findViewById(R.id.listView);
        t1=findViewById(R.id.textView);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if ( myBluetooth==null ) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_LONG).show();
            finish();
        } else if ( !myBluetooth.isEnabled() ) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        btnPaired.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList () {  //shows the list of paired devices
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if ( pairedDevices.size() > 0 ) {
            for ( BluetoothDevice bt : pairedDevices ) {
                list.add(bt.getName().toString() + "\n" + bt.getAddress().toString());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  //starts second activity based on the selected Bluetooth device
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length()-17);

            Intent i = new Intent(MainActivity.this, ledControl.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }
    };
}




//2nd Activity

package com.example.angelomathai.smartlife;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class ledControl extends AppCompatActivity
{

    Button btn1, btn2, btn3, btn4,  btnDis;
    Button Speak;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {  //second activity UI and sends signal to ‘sendsignal’ function based on the clicked button
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);

        btn1 = (Button) findViewById(R.id.button1);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn4 = (Button) findViewById(R.id.button4);
        btnDis = (Button) findViewById(R.id.button5);
        Speak = (Button) findViewById(R.id.button6);

        new ConnectBT().execute();  

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                sendSignal("1");
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {

                sendSignal("2");
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {

                sendSignal("3");
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {

                sendSignal("4");
            }
        });
        Speak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                promptSpeechInput();

            }
        });


        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {

                Disconnect();
            }
        });
    }
    private void sendSignal ( String number ) { //sends signal to arduino
        if ( btSocket != null ) {
            try {
                btSocket.getOutputStream().write(number.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void promptSpeechInput() { //to get the speech input via google voice
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) { // sends signals based on the voice commands
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data)
                {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String input = result.get(0).toString();

                    if (input.equals("turn on switch one")||input.equals("turn on switch 1")) {
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("1".toString().getBytes());
                            } catch (IOException e) {
                                msg("error");
                            }
                        }
                    }

                    if (input.equals("turn off switch one")||input.equals("turn off switch 1")) {
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("1".toString().getBytes());
                            } catch (IOException e) {
                                msg("error");
                            }
                        }

                    }

                    if (input.equals("turn on switch two")||input.equals("turn on switch to")) {
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("2".toString().getBytes());
                            } catch (IOException e) {
                                msg("error");
                            }
                        }

                    }

                    if (input.equals("turn off switch two")||input.equals("turn off switch to")) {
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("2".toString().getBytes());
                            } catch (IOException e) {
                                msg("error");
                            }
                        }

                    }

                    if (input.equals("turn on switch three")||input.equals("turn on switch 3")) {
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("3".toString().getBytes());
                            } catch (IOException e) {
                                msg("error");
                            }
                        }

                    }
                    if (input.equals("turn off switch three")||input.equals("turn off switch 3")) {
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("3".toString().getBytes());
                            } catch (IOException e) {
                                msg("error");
                            }
                        }

                    }

                    if (input.equals("turn on switch four")||input.equals("turn on switch for")||input.equals("turn on switch 4"))
                    {
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("4".toString().getBytes());
                            } catch (IOException e) {
                                msg("error");
                            }
                        }

                    }

                    if (input.equals("turn off switch four")||input.equals("turn off switch for")||input.equals("turn off switch 4")) {
                        if (btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("4".toString().getBytes());
                            } catch (IOException e) {
                                msg("error");
                            }
                        }

                    }


                }

                break;
            }

        }
    }
    private void Disconnect () //disconnects Bluetooth connnection
    {
        if ( btSocket!=null ) {
            try {
                btSocket.close();
            } catch(IOException e) {
                msg("Error");
            }
        }

        finish();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override //background bluetooth connection process
        protected  void onPreExecute () {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isBtConnected ) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }

            progress.dismiss();
        }

    }
    private void msg (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

}
