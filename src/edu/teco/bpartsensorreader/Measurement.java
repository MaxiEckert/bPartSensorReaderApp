package edu.teco.bpartsensorreader;

import java.util.Date;

/** 
 * This class models a sensor measurement pair consisting of a humidity and a temperature value with their timestamp.
 * @author Maximilian Eckert
 *
 */
public class Measurement {
	
	private Date timestamp;
	private float humidity;
	private float temperature;
	
	/**
	 * Creates a new measurement.
	 * @param timestamp the timestamp.
	 * @param humidity the humidity value
	 * @param temperature the temperature value.
	 */
	public Measurement(Date timestamp, float humidity, float temperature) {
		this.timestamp = timestamp;
		this.humidity = humidity;
		this.temperature = temperature;
	}

	/**
	 * Returns the measurements timestamp.
	 * @return the timestamp.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the measurements timestamp.
	 * @param timestamp the timestamp.
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Returns the humidity value.
	 * @return the humidity value.
	 */
	public float getHumidity() {
		return humidity;
	}

	/**
	 * Sets the humidity value.
	 * @param humidity the humidity value.
	 */
	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}

	/**
	 * Returns the temperature value.
	 * @return the temperature value.
	 */
	public float getTemperature() {
		return temperature;
	}

	/**
	 * Sets the temperature value.
	 * @param humidity the temperature value.
	 */
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	
	
}
