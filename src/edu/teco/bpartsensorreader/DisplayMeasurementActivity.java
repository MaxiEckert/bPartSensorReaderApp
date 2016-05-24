package edu.teco.bpartsensorreader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This activity will display the measurements from the bPart sensor 
 * and will allow the user to update the sensor's system time.
 * @author Maximilian Eckert
 *
 */
public class DisplayMeasurementActivity extends Activity {

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	
	protected static final String BPART_TIME_UUID = "4b822f51-3941-4a4b-a3cc-b2602ffe0d00";
	protected static final String TIME_NOTIFICATION_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
	protected static final String BPART_MEAS_UUID = "4b822f41-3941-4a4b-a3cc-b2602ffe0d00";
	
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice device;
	private BluetoothGatt mBluetoothGatt;
	private TextView textView;
	private BluetoothGattCharacteristic timeChar;
	private BluetoothGattCharacteristic measChar;
	private List<Measurement> measurementList;
	private CustomMeasurementListAdapter adapter;
	private ListView listView;
	private TextView measCountTextView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_measurement);

		textView = (TextView) findViewById(R.id.textView2);
		measCountTextView = (TextView) findViewById(R.id.textView3);

		measurementList = new ArrayList<Measurement>();

		adapter = new CustomMeasurementListAdapter(this, measurementList);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(adapter);

		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		getActionBar().setTitle("Connected to " + mDeviceName);

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
		connect();
	}


	@Override
	protected void onPause() {
		super.onPause();
		Log.i("App", "Activity paused");
		mBluetoothGatt.close();
	}

	private void connect() {
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);	
		mBluetoothGatt.connect();
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i("App", "connected!");
				mBluetoothGatt.discoverServices();
			}
			else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.i("App", "disconnected :( try to reconnect");
				mBluetoothGatt.close();
				connect();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.i("App", "onServicesDiscovered() called.");

			List<BluetoothGattService> mServices = mBluetoothGatt.getServices();
			for (BluetoothGattService service : mServices) {
				List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

				for (BluetoothGattCharacteristic characteristic : characteristics) {
					if(characteristic.getUuid().toString().equals(BPART_TIME_UUID)) {
						Log.i("App", "time char gefunden!!");
						timeChar = characteristic;
						mBluetoothGatt.readCharacteristic(timeChar);
					} else if(characteristic.getUuid().toString().equals(BPART_MEAS_UUID)) {
						Log.i("App", "meas char gefunden!!");
						measChar = characteristic;
					}
				}
			}
		}

		@Override
		public void onCharacteristicRead (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (characteristic.equals(timeChar)) {
				int time = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
				final Date timestamp = new Date((long) time * 1000);
				Log.i("App", "time char gelesen: " + time);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
						textView.setText(simpleDateFormat.format(timestamp) + " Uhr");
					}
				});

				// Enable Notification.
				mBluetoothGatt.setCharacteristicNotification(characteristic, true);

				// Globally enable notifications on bPart.
				BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(TIME_NOTIFICATION_DESCRIPTOR_UUID));
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				mBluetoothGatt.writeDescriptor(descriptor);

			} else if (characteristic.equals(measChar)) {
				byte[] raw = characteristic.getValue();
				Log.i("App", "meas char gelesen: got " + raw.length + " bytes!!!!!!");
				if (raw.length == 0) {
					Log.i("App", "meas char gelesen: Kein Messwert mehr da");
				} else {   		
					int timestamp1 = byteArrayToTimestamp(Arrays.copyOfRange(raw, 0, 4));
					int timestamp2 = byteArrayToTimestamp(Arrays.copyOfRange(raw, 8, 12));

					float hum1 = byteArrayToMeasValue(Arrays.copyOfRange(raw, 4, 6));
					float temp1 = byteArrayToMeasValue(Arrays.copyOfRange(raw, 6, 8)) / 1000.f;
					float hum2 = byteArrayToMeasValue(Arrays.copyOfRange(raw, 12, 14));
					float temp2 = byteArrayToMeasValue(Arrays.copyOfRange(raw, 14, 16)) / 1000.f;

					final Measurement meas1 = new Measurement(new Date((long) timestamp1 * 1000), hum1, temp1);
					final Measurement meas2 = new Measurement(new Date((long) timestamp2 * 1000), hum2, temp2);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.insert(meas1, 0);
							adapter.insert(meas2, 0);
							adapter.notifyDataSetChanged();
							measCountTextView.setText("Anzahl Messwerte: " + adapter.getCount());
						}
					});

					Log.i("App", "meas char gelesen: timestamp1: " + timestamp1);
					Log.i("App", "meas char gelesen: timestamp2: " + timestamp2);
					Log.i("App", "meas char gelesen: hum1: " + hum1);
					Log.i("App", "meas char gelesen: temp1: " + temp1);
					Log.i("App", "meas char gelesen: hum2: " + hum2);
					Log.i("App", "meas char gelesen: temp2: " + temp2);

					mBluetoothGatt.readCharacteristic(measChar);
				}
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,	BluetoothGattCharacteristic characteristic) {
			Log.i("App", "Notification");
			
			int time = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
			final Date timestamp = new Date((long) time * 1000);
			Log.i("App", "time char gelesen: " + time);

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
					textView.setText(simpleDateFormat.format(timestamp) + " Uhr");
				}
			});
		}

		@Override
		public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == 0) {
				Log.i("App", "Write erfolgreich");
				mBluetoothGatt.readCharacteristic(timeChar);
			}
			else {
				Log.i("App", "Write failed");
			}
		}
	};

	/**
	 * Updates the bParts system time.
	 * @param view
	 */
	public void updateTime(View view) {
		Log.i("App", "try to write " + (int) (new Date().getTime()/1000));
		timeChar.setValue((int) (new Date().getTime()/1000), BluetoothGattCharacteristic.FORMAT_UINT32, 0);
		mBluetoothGatt.writeCharacteristic(timeChar);
	}

	/**
	 * Initiates the reading of the stored measurements.
	 * @param view
	 */
	public void readMeasurements(View view) {
		mBluetoothGatt.readCharacteristic(measChar);
	}

	private int byteArrayToTimestamp(byte[] raw) {
		ByteBuffer bb = ByteBuffer.wrap(raw);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}

	private int byteArrayToMeasValue(byte[] raw) {
		ByteBuffer bb = ByteBuffer.wrap(raw);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getShort();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_measurement, menu);
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
