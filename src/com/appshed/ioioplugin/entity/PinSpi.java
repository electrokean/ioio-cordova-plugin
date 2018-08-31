package com.appshed.ioioplugin.entity;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.appshed.ioioplugin.service.IOIOCOmmunicationService;

import ioio.lib.api.IOIO;
import ioio.lib.api.SpiMaster;
import ioio.lib.api.SpiMaster.Rate;

public class PinSpi extends Pin{
	public int bus = 0;
	public int misoPin = IOIO.INVALID_PIN;
	public int mosiPin = IOIO.INVALID_PIN;
	public int clkPin = IOIO.INVALID_PIN;
	public int[] ssPins = {};

	public int slave = 0;
	public byte[] request = {};
	public byte[] response = {};
	public boolean start = false;

	public Rate rate = Rate.RATE_1M;

	public SpiMaster spi = null;

	public PinSpi(int bus, int misoPin, int mosiPin, int clkPin, int[] ssPins, int rate){
		this.pin = bus + Pin.TYPE_SPI;
		this.bus = bus;
		this.misoPin = misoPin;
		this.mosiPin = mosiPin;
		this.clkPin = clkPin;
		this.ssPins = ssPins;
		if (rate==250) this.rate = Rate.RATE_250K;
		else if (rate==500) this.rate = Rate.RATE_500K;
		else if (rate==1000) this.rate = Rate.RATE_1M;
		else if (rate==2000) this.rate = Rate.RATE_2M;
	}

	@Override
	public JSONObject getJson() {
		JSONObject json = new JSONObject();
		JSONArray data = new JSONArray();
		for (int i=0; i<response.length; i++) {
			data.put(response[i] & 0xff);
		}
		try {
			json.put("pin",pin);
			json.put("value",data);
			json.put("slave",slave);
			json.put("class",PIN_SPI);
			json.put("info",toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String toString() {
		return "PinSpi [bus=" + bus + ", slave=" + slave + "]";
	}


}