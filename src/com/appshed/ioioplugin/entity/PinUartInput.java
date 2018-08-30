package com.appshed.ioioplugin.entity;

import org.json.JSONException;
import org.json.JSONObject;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;

public class PinUartInput extends Pin{
	public String input = "";
	public int txPin = IOIO.INVALID_PIN;
	public int baud = 9600;
	public Uart.Parity parity = Uart.Parity.NONE; //0
	public Uart.StopBits stopBits = Uart.StopBits.ONE; //0
	
	public PinUartInput(int pin, int txPin, int baud, int parity, int stopBits){
		this.pin = pin;
		this.txPin = txPin;
		this.baud = baud;
		this.parity = Uart.Parity.values()[parity];
		this.stopBits = Uart.StopBits.values()[stopBits];
	}

	@Override
	public JSONObject getJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("pin",pin);
			json.put("value",input);
			json.put("class",PIN_INPUT_UART);
			json.put("info",toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String toString() {
		return "PinUartInput [input=" + input + "]";
	}
	
	

}