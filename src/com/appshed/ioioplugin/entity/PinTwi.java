package com.appshed.ioioplugin.entity;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.appshed.ioioplugin.service.IOIOCOmmunicationService;

import ioio.lib.api.IOIO;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.TwiMaster.Rate;

public class PinTwi extends Pin{
	public int bus = 0;

	public int addr = 0;
	public byte[] request = {};
	public int readLen = 0;
	public byte[] response = {};
	public boolean start = false;

	public Rate rate = Rate.RATE_100KHz;
	// currently this is not exposed, just a default
	public boolean smbus = false;

	public TwiMaster twi = null;

	public PinTwi(int bus, int rate){
		this.pin = bus + Pin.TYPE_TWI;
		this.bus = bus;
		if (rate==400) this.rate = Rate.RATE_400KHz;
		else if (rate==1000) this.rate = Rate.RATE_1MHz;
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
			json.put("addr",addr);
			json.put("class",PIN_TWI);
			json.put("info",toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String toString() {
		return "PinTwi [bus=" + bus + ", addr=" + addr + "]";
	}


}