package edu.teco.bpartsensorreader;

import android.bluetooth.BluetoothDevice;

/**
 * This class models a Bluetooth low energy device.
 * 
 * @author Maximilian Eckert
 *
 */
public class BLEDevice {

	private BluetoothDevice device;
	private int rssi;
	
	/**
	 * Creates a new BLE device.
	 * @param device Bluetooth device.
	 * @param rssi signal strength.
	 */
	public BLEDevice(BluetoothDevice device, int rssi) {
		this.device = device;
		this.rssi = rssi;
	}
	
	/**
	 * Returns the BLE device.
	 * @return the BLE device.
	 */
	public BluetoothDevice getBLEDevice() {
		return device;
	}
	
	/**
	 * Returns the signal strength.
	 * @return the RSSI value.
	 */
	public int getRssi() {
		return rssi;
	}
	
	/**
	 * Sets the signal strength of this BLE device.
	 * @param rssi the new RSSI value.
	 */
	public void updateRssi(int rssi) {
		this.rssi = rssi;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BLEDevice)) {
			return false;
		}
		else {
			BLEDevice otherDevice = (BLEDevice) other;
			return device.getAddress().equals(otherDevice.getBLEDevice().getAddress());
		}
	}
}
