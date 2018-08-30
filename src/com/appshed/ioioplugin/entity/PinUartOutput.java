package com.appshed.ioioplugin.entity;

import org.json.JSONException;
import org.json.JSONObject;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;

public class PinUartOutput extends Pin{
	public String output = null;
	public int rxPin = IOIO.INVALID_PIN;
	public int baud = 9600;
	public Uart.Parity parity = Uart.Parity.NONE; //0
	public Uart.StopBits stopBits = Uart.StopBits.ONE; //0
	
	public PinUartOutput(int pin, int rxPin, int baud, int parity, int stopBits){
		this.pin = pin;
		this.rxPin = rxPin;
		this.baud = baud;
		this.parity = Uart.Parity.values()[parity];
		this.stopBits = Uart.StopBits.values()[stopBits];
	}

	@Override
	public JSONObject getJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("pin",pin);
			json.put("value",output);
			json.put("class",PIN_OUTPUT_UART);
			json.put("info",toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String toString() {
		return "PinUartOutput [output=" + output + "]";
	}
	
	

}