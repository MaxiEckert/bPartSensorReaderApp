package edu.teco.bpartsensorreader;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A custom list adapter for showing BLE devices in a list view.
 * @author Maximilian Eckert
 *
 */
public class CustomBLEListAdapter extends ArrayAdapter<BLEDevice> {
	
	private final Context context;
	private final List<BLEDevice> values;

	/** 
	 * Creates a new BLE device list adapter.
	 * @param context app context.
	 * @param values initial values.
	 */
	public CustomBLEListAdapter(Context context, List<BLEDevice> values) {
		super(context, R.layout.my_list_item, values);
		this.context = context;
		this.values = values;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(R.layout.my_list_item, parent, false);
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
		TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
		
		BLEDevice device = values.get(position);
		
		imageView.setImageResource(R.drawable.bluetooth_icon);
		firstLine.setText("Device Name: " + device.getBLEDevice().getName());
		secondLine.setText("Address: " + device.getBLEDevice().getAddress()
							+ "  RSSI : " + device.getRssi() + " dBm");
		return rowView;
	}
} 

