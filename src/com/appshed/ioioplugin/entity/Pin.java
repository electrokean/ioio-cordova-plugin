package com.appshed.ioioplugin.entity;

import org.json.JSONObject;

abstract public class Pin{
	public static final String PIN_INPUT_ANALOG = "analogInput";
	public static final String PIN_OUTPUT_PWM = "pwmOutput";
	public static final String PIN_OUTPUT_DIGITAL = "digitalOutput";
	public static final String PIN_INPUT_DIGITAL = "digitalInput";
	public static final String PIN_UART = "uart";
	public static final String PIN_TWI = "twi";
	public static final String PIN_SPI = "spi";
	public int pin;

	// these must be added to pin number to keep unique in the pins map
	// can't use rxpin or txpin for uarts as one or other could be unused, so we have a bus id
	public static final int TYPE_TWI = 64;
	public static final int TYPE_SPI = 128;
	public static final int TYPE_UART = 256;
	
	public abstract JSONObject getJson();
}