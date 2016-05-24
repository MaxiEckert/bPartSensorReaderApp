package edu.teco.bpartsensorreader;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A custom list adapter for showing sensor measurements in a list view.
 * @author Maximilian Eckert
 *
 */
public class CustomMeasurementListAdapter extends ArrayAdapter<Measurement> {
	
	private final Context context;
	private final List<Measurement> values;

	/**
	 * Creates a new measurement list adapter.
	 * @param context
	 * @param values
	 */
	public CustomMeasurementListAdapter(Context context, List<Measurement> values) {
		super(context, R.layout.my_measurement_list_item, values);
		this.context = context;
		this.values = values;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(R.layout.my_measurement_list_item, parent, false);
		
		ImageView tempIcon = (ImageView) rowView.findViewById(R.id.temperature_icon);
		TextView tempValue = (TextView) rowView.findViewById(R.id.temperature);

		ImageView humIcon = (ImageView) rowView.findViewById(R.id.humidity_icon);
		TextView humValue = (TextView) rowView.findViewById(R.id.humidity);
		
		TextView timestamp = (TextView) rowView.findViewById(R.id.timestamp);
		
		Measurement meas = values.get(position);
		
		tempIcon.setImageResource(R.drawable.thermometer);
		humIcon.setImageResource(R.drawable.humidity);
		
		tempValue.setText(String.valueOf(meas.getTemperature()) + "\u00b0 Celsius");
		humValue.setText(String.valueOf(meas.getHumidity()) + "% Luftfeuchtigkeit");
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
		timestamp.setText(simpleDateFormat.format(meas.getTimestamp()));
		
		return rowView;
	}
} 
