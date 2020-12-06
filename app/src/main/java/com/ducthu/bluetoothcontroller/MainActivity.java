package com.ducthu.bluetoothcontroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1; //Khai báo số int yêu cầu mở Bluetooth

    Toolbar toolbar;                     //Khai báo toolbar
    TextView txtBTName;                  //Khai báo TextView hiển thị tên của thiết bị Bluetooth
    TextView txtBTAdd;                   //Khai báo TextView hiển thị địa chỉ MAC tb Bluetooth
    ImageButton btnLight1;               //Khai báo ImageButton điều khiển đèn số 1
    ImageButton btnLight2;               //Khai báo ImageButton điều khiển đèn số 2
    ImageButton btnFan;                  //Khai báo ImageButton điều khiển quạt
    ImageButton btnSocket;               //Khai báo ImageButton điều khiển  ổ cắm

    BluetoothAdapter btAdapter;          //Khai báo BluetoothAdapter
    BluetoothDevice btDevice;            //Khai báo thiết bị Bluetooth được kết nối tới
    Set<BluetoothDevice> pairedDevice;   //Khai báo danh sách các thiết bị Bluetooth đã được
                                         //điện thoại kết nối
    List<String> btNames;                //Khai báo List tên các thiết bị Bluetooth
    List<String> btAddress;              //Khai báo List địa chỉ MAC các thiết bị Bluetooth

    BluetoothSocket btSocket;            //Khai báo socket Bluetooth

    InputStream input;                   //Khai báo InputStream
    OutputStream output;                 //Khai báo OutputStream

    boolean isLight1 = false;            //Khai báo biến lưu trạng thái của đèn 1
    boolean isLight2 = false;            //Khai báo biến lưu trạng thái của đèn 2
    boolean isFan = false;               //Khai báo biến lưu trạng thái của quạt
    boolean isSocket = false;            //Khai báo biến lưu tráng thái của ổ cắm

    //Khai báo UUID để kết nối tới thiết bị Bluetooth.
    public static UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setBluetooth();
    }

    private void setComponents() {
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        txtBTName = (TextView) findViewById(R.id.txt_bt_name);
        txtBTAdd = (TextView) findViewById(R.id.txt_bt_add);

        btnLight1 = (ImageButton) findViewById(R.id.btn_light1);
        btnLight2 = (ImageButton) findViewById(R.id.btn_light2);
        btnFan = (ImageButton) findViewById(R.id.btn_fan);
        btnSocket = (ImageButton) findViewById(R.id.btn_socket);

        btnLight1.setEnabled(false);
        btnLight2.setEnabled(false);
        btnFan.setEnabled(false);
        btnSocket.setEnabled(false);

        btNames = new ArrayList<>();
        btAddress = new ArrayList<>();
    }

    private void setBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            Toast.makeText(MainActivity.this,
                    "Bluetooth is not available",
                    Toast.LENGTH_SHORT).show();
        }

        if(!btAdapter.isEnabled()) {
            Intent intentEnableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentEnableBT, REQUEST_ENABLE_BT);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        pairedDevice = btAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevice) {
            btNames.add(bt.getName());
            btAddress.add(bt.getAddress());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this,
                        "App need Bluetooth to work!",
                        Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reload_bt_device, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_reload_bt:
                showDialogBTDevice();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialogBTDevice() {
        CharSequence[] listDevice = btNames.toArray(new CharSequence[btNames.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.dialog_pick_a_bt)
                .setItems(listDevice,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                connectBTDevice(which);
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void connectBTDevice (int id) {
        for(BluetoothDevice bt : pairedDevice) {
            if(btNames.get(id).equals(bt.getName())) {
                btDevice = bt;
                txtBTName.setText(bt.getName());
                txtBTAdd.setText(bt.getAddress());
            }
        }

        boolean isSuccess = false;

        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(myUUID);
            btSocket.connect();
            isSuccess = true;
        } catch(IOException e) {
            isSuccess = false;
            e.printStackTrace();
        }

//        try{
//            input = btSocket.getInputStream();
//        } catch (IOException e) {
//            isSuccess = false;
//            e.printStackTrace();
//            Toast.makeText(MainActivity.this, "Something wrong! You should restart the app!",
//                    Toast.LENGTH_SHORT).show();
//        }

        try{
            output = btSocket.getOutputStream();
        } catch (IOException e) {
            isSuccess = false;
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Something wrong! You should restart the app!",
                    Toast.LENGTH_SHORT).show();
        }

        if(isSuccess) {
            Toast.makeText(MainActivity.this, "Connect Success!", Toast.LENGTH_SHORT).show();
            enableButton(true);
        } else {
            Toast.makeText(MainActivity.this, "Something wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    public void enableButton(boolean isOn) {
        btnLight1.setEnabled(isOn);
        btnLight2.setEnabled(isOn);
        btnFan.setEnabled(isOn);
        btnSocket.setEnabled(isOn);
    }

    public void onClickSwitchLight1(View view) {
        byte data = '1';
        if(isLight1){
            data = 'a';
            btnLight1.setImageResource(R.drawable.light_off);
        }
        else {
            data = '1';
            btnLight1.setImageResource(R.drawable.light_on);
        }
        try{
            output.write(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
        isLight1 = !isLight1;
    }

    public void onClickSwitchLight2(View view) {
        byte data = '2';
        if(isLight2) {
            data = 'b';
            btnLight2.setImageResource(R.drawable.light_off);
        }
        else {
            data = '2';
            btnLight2.setImageResource(R.drawable.light_on);
        }
        try{
            output.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isLight2 = !isLight2;
    }

    public void onClickSwitchFan(View view) {
        byte data = '3';
        if(isFan) {
            data = 'c';
            btnFan.setImageResource(R.drawable.fan_off);
        }
        else {
            data = '3';
            btnFan.setImageResource(R.drawable.fan_on);
        }
        try{
            output.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isFan = !isFan;
    }

    public void onClickSwitchSocket(View view) {
        byte data = '4';
        if(isSocket) {
            data = 'd';
            btnSocket.setImageResource(R.drawable.socket_off);
        }
        else {
            data = '4';
            btnSocket.setImageResource(R.drawable.socket_on);
        }
        try{
            output.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isSocket = !isSocket;
    }
}