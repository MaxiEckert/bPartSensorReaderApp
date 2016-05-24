package edu.teco.bpartsensorreader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This activity will scan for BLE devices and display them to the user..
 * @author Maximilian Eckert
 *
 */
public class MainActivity extends Activity {

	//The device's Bluetooth Adapter
    private BluetoothAdapter mBluetoothAdapter;

    //stores if device is scanning atm
	private boolean mScanning;
    
    // Constant for identifying intent request
    private static int REQUEST_ENABLE_BT = 0;
    

    private List<BLEDevice> deviceList;
    private CustomBLEListAdapter adapter;
    private ListView listView;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        final BluetoothManager bluetoothManager =
    	        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    	mBluetoothAdapter = bluetoothManager.getAdapter();

        deviceList = new ArrayList<BLEDevice>();
        
        adapter = new CustomBLEListAdapter(this, deviceList);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deviceSelected(position);
            }
        });
    }
	
	@Override
    protected void onResume() {
		super.onResume();
		if (adapter != null && !adapter.isEmpty()) {
			adapter.clear();
			adapter.notifyDataSetChanged();
		}
	}

    /**
     * This method gets invoked when the scan button is clicked. It toggles the device's scanning status.
     * @param v
     */
    public void scanButtonClicked(View v) {
    	Log.i("MyInfo", "on click kam an");
    	
    	if (mScanning) {
    		stopScan();
    	}
    	else {
			//check if BT is enabled
			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			} else {
				startScan();
			}
    	}
    }

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user has returned from the Enable-BT-Intent. Check the result.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
        	startScan();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Suche kann nicht gestarted werden, da Bluetooth nicht aktiviert wurde", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startScan() {   	
        mScanning = true;
        Button scanButton = (Button) findViewById(R.id.button1);
		scanButton.setText("Scanning...");

    	mBluetoothAdapter.startLeScan(mLeScanCallback);
    }
  
    private void stopScan() {	
        mScanning = false;
        Button scanButton = (Button) findViewById(R.id.button1);
		scanButton.setText("Scan nach bParts starten");
        
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
        	runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Device gefunden", Toast.LENGTH_SHORT).show();
                    Log.i("App", "scan callback. Device:  " + device.getName());
                    BLEDevice bleDevice = new BLEDevice(device, rssi);
                    if (!deviceList.contains(bleDevice)) {
                    	adapter.add(new BLEDevice(device, rssi));
                    	adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };
    
    private void deviceSelected(int position) {
    	BluetoothDevice device = deviceList.get(position).getBLEDevice();
    	
    	final Intent intent = new Intent(this, DisplayMeasurementActivity.class);
    	intent.putExtra(DisplayMeasurementActivity.EXTRAS_DEVICE_NAME, device.getName());
    	intent.putExtra(DisplayMeasurementActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
    	
    	stopScan();
    	
    	startActivity(intent);
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
