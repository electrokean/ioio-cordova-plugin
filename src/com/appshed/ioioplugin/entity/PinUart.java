package com.appshed.ioioplugin.entity;

import org.json.JSONException;
import org.json.JSONObject;

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;

public class PinUart extends Pin{
	public int bus = 0;
	public int rxPin = IOIO.INVALID_PIN;
	public int txPin = IOIO.INVALID_PIN;
	public int baud = 9600;
	public Uart.Parity parity = Uart.Parity.NONE; //0
	public Uart.StopBits stopBits = Uart.StopBits.ONE; //0
	public String input = "";
	public String output = null;
	public Uart uart = null;
	
	public PinUart(int bus, int rxPin, int txPin, int baud, int parity, int stopBits){
		this.pin = bus + Pin.TYPE_UART;
		this.bus = bus;
		this.rxPin = rxPin;
		this.txPin = txPin;
		this.baud = baud;
		this.parity = Uart.Parity.values()[parity];
		this.stopBits = Uart.StopBits.values()[stopBits];
	}

	@Override
	public JSONObject getJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("pin",rxPin);
			json.put("value",input);
			json.put("class",PIN_UART);
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