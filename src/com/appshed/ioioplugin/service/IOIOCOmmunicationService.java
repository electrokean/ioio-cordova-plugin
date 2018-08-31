package com.appshed.ioioplugin.service;

import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import com.appshed.ioioplugin.entity.Pin;
import com.appshed.ioioplugin.entity.PinAnalogInput;
import com.appshed.ioioplugin.entity.PinDigitalInput;
import com.appshed.ioioplugin.entity.PinDigitalOutput;
import com.appshed.ioioplugin.entity.PinPwmOutput;
import com.appshed.ioioplugin.entity.PinUart;
import com.appshed.ioioplugin.entity.PinTwi;
import com.appshed.ioioplugin.entity.PinSpi;

import android.content.Intent;
import android.os.IBinder;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.Uart;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.SpiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

/**
 * An example IOIO service. While this service is alive, it will attempt to
 * connect to a IOIO and blink the LED. A notification will appear on the
 * notification bar, enabling the user to stop the service.
 */
public class IOIOCOmmunicationService extends IOIOService {
	public static final String TAG = IOIOCOmmunicationService.class.getSimpleName();

	public static final int PWM_MAX_FREQ = 10000;
	public static Map<Integer,Pin> pins;
	public static CallbackContext eventListener;

	public static final long delay = 100;
	public static boolean activeLed = true;
	public static long lastCallbackFromJS = 0;
	public static long lastCallbackToJS = 0;

	public static void initPins(){
		System.out.print("initPins()");
		pins = new HashMap<Integer,Pin>();
	}

	public static void addAnalogInput(int pinPort){
		pins.put(pinPort,new PinAnalogInput(pinPort));
	}

	public static void addPwmOutput(int pinPort,int freq){
		pins.put(pinPort,new PinPwmOutput(pinPort,freq));
	}

	public static void addDigitalOutput(int pinPort){
		pins.put(pinPort,new PinDigitalOutput(pinPort));
	}

	public static void addDigitalInput(int pinPort){
		pins.put(pinPort,new PinDigitalInput(pinPort));
	}

	public static void addUart(int bus, int rxPin, int txPin, int baud, int parity, int stopBits){
		pins.put(bus+Pin.TYPE_UART,new PinUart(bus, rxPin, txPin, baud, parity, stopBits));
	}

	public static void addTwi(int bus, int rate){
		pins.put(bus+Pin.TYPE_TWI, new PinTwi(bus, rate));
	}

	public static void addSpi(int bus, int misoPin, int mosiPin, int clkPin, int[] ssPins, int rate){
		pins.put(bus+Pin.TYPE_SPI, new PinSpi(bus, misoPin, mosiPin, clkPin, ssPins, rate));
	}

	public static void setPwmOutput(int pinPort,int freq){
		if(pins.containsKey(pinPort) && pins.get(pinPort)instanceof PinPwmOutput){
			((PinPwmOutput)pins.get(pinPort)).freq = freq;
		}
	}

	public static void setDigitalOutput(int pinPort,boolean output){
		if(pins.containsKey(pinPort) && pins.get(pinPort)instanceof PinDigitalOutput){
			((PinDigitalOutput)pins.get(pinPort)).output = output;
		}
	}

	public static void toggleDigitalOutput(int pinPort){
		if(pins.containsKey(pinPort) && pins.get(pinPort)instanceof PinDigitalOutput){
			PinDigitalOutput pinDigitalOutput = (PinDigitalOutput)pins.get(pinPort);
			pinDigitalOutput.output = !pinDigitalOutput.output;
		}
	}

	public static void writeUart(int bus, String data){
		if(pins.containsKey(bus+Pin.TYPE_UART) && pins.get(bus+Pin.TYPE_UART)instanceof PinUart){
			PinUart pinUart = (PinUart)pins.get(bus+Pin.TYPE_UART);
			pinUart.output = data;
		}
	}

	public static void writeReadTwi(int bus, int addr, byte[] request, int readLen){
		if(pins.containsKey(bus+Pin.TYPE_TWI) && pins.get(bus+Pin.TYPE_TWI)instanceof PinTwi){
			PinTwi pinTwi = (PinTwi)pins.get(bus+Pin.TYPE_TWI);
			pinTwi.addr = addr;
			pinTwi.request = request;
			pinTwi.readLen = readLen;
			pinTwi.response = new byte[pinTwi.readLen];
			pinTwi.start = true;
		}
	}

	public static void writeReadSpi(int bus, int slave, byte[] request){
		if(pins.containsKey(bus+Pin.TYPE_SPI) && pins.get(bus+Pin.TYPE_SPI)instanceof PinSpi){
			PinSpi pinSpi = (PinSpi)pins.get(bus+Pin.TYPE_SPI);
			pinSpi.slave = slave;
			pinSpi.request = request;
			pinSpi.response = new byte[request.length];
			pinSpi.start = true;
		}
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private DigitalOutput led_;

			/**
			 * here we need to open all ports before we would use them
			 */

			@Override
			public void incompatible() {
				JSONObject r = new JSONObject();
				try{
					r.put("IOIOLIB_VER", ioio_.getImplVersion(VersionType.IOIOLIB_VER));
					r.put("APP_FIRMWARE_VER", ioio_.getImplVersion(VersionType.APP_FIRMWARE_VER));
					r.put("BOOTLOADER_VER", ioio_.getImplVersion(VersionType.BOOTLOADER_VER));
					r.put("HARDWARE_VER", ioio_.getImplVersion(VersionType.HARDWARE_VER));
				}catch(Exception e){}

				PluginResult result = new PluginResult(PluginResult.Status.ERROR,r);
                result.setKeepCallback(true);
				eventListener.sendPluginResult(result);
			}

			@Override
			protected void setup() throws ConnectionLostException,InterruptedException {


				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);
				for(Integer pinPort:pins.keySet()){
					Pin pin = pins.get(pinPort);

					if(pin instanceof PinPwmOutput){ // PinPwmOutput
						PinPwmOutput pinPwmOutput = (PinPwmOutput)pin;
						pinPwmOutput.pwmOutput = ioio_.openPwmOutput(pinPort, PWM_MAX_FREQ);
					}else if(pin instanceof PinAnalogInput){ // PinAnalogInput
						PinAnalogInput pinAnalogInput = (PinAnalogInput)pin;
						pinAnalogInput.analogInput = ioio_.openAnalogInput(pinPort);
					}else if(pin instanceof PinDigitalOutput){ // PinDigitalOutput
						PinDigitalOutput pinDigitalOutput = (PinDigitalOutput)pin;
						pinDigitalOutput.digitalOutput = ioio_.openDigitalOutput(pinPort);
					}else if(pin instanceof PinDigitalInput){ // PinDigitalInput
						PinDigitalInput pinDigitalInput = (PinDigitalInput)pin;
						pinDigitalInput.digitalInput = ioio_.openDigitalInput(pinPort);
					}else if(pin instanceof PinUart){ // PinUart
						PinUart pinUart = (PinUart)pin;
						//System.out.println(TAG + " opening uart" + pinUart.bus + " rxPin=" + pinUart.rxPin + " baud=" + pinUart.baud);
						if (pinUart.uart==null)
							pinUart.uart = ioio_.openUart(pinUart.rxPin, pinUart.txPin, pinUart.baud, 
								pinUart.parity, pinUart.stopBits);
					}else if(pin instanceof PinTwi){ // PinTwi
						PinTwi pinTwi = (PinTwi)pin;
						if (pinTwi.twi==null)
							pinTwi.twi = ioio_.openTwiMaster(pinTwi.bus, pinTwi.rate, pinTwi.smbus);
					}else if(pin instanceof PinSpi){ // PinSpi
						PinSpi pinSpi = (PinSpi)pin;
						if (pinSpi.spi==null)
							pinSpi.spi = ioio_.openSpiMaster(pinSpi.misoPin, pinSpi.mosiPin, pinSpi.clkPin,
								pinSpi.ssPins, pinSpi.rate);
					}
				}
			}

			/**
			 * here we read/write from/to board with minimum delay of 100 milliseconds, on JS side we would increase this value
			 */
			@Override
			public void loop() throws ConnectionLostException,InterruptedException {

				if(lastCallbackFromJS == 0 || (IOIOCOmmunicationService.lastCallbackToJS+delay) > System.currentTimeMillis()){
					return;
				}

				led_.write(activeLed);
				activeLed = !activeLed;
				IOIOCOmmunicationService.lastCallbackToJS = System.currentTimeMillis();
				lastCallbackFromJS = 0;

				JSONArray parameters = new JSONArray();
				for(Integer pinPort:pins.keySet()){
					Pin pin = pins.get(pinPort);
					if(pins.get(pinPort) instanceof PinPwmOutput){ // PinPwmOutput
						PinPwmOutput pinPwmOutput = (PinPwmOutput)pins.get(pinPort);
						pinPwmOutput.pwmOutput.setPulseWidth(pinPwmOutput.freq);
						try {
							parameters.put(pinPwmOutput.getJson());
			            } catch (Exception e) {}

					}else if(pins.get(pinPort) instanceof PinAnalogInput){ // PinAnalogInput
						PinAnalogInput pinAnalogInput = (PinAnalogInput)pins.get(pinPort);
						pinAnalogInput.output = pinAnalogInput.analogInput.read();
						try {
							parameters.put(pinAnalogInput.getJson());
			            } catch (Exception e) {}
					}else if(pin instanceof PinDigitalOutput){
						PinDigitalOutput pinDigitalOutput = (PinDigitalOutput)pin;
						pinDigitalOutput.digitalOutput.write(pinDigitalOutput.output);
						try {
							parameters.put(pinDigitalOutput.getJson());
			            } catch (Exception e) {}
					}else if(pin instanceof PinDigitalInput){ // PinDigitalInput
						PinDigitalInput pinDigitalInput = (PinDigitalInput)pin;
						pinDigitalInput.input = pinDigitalInput.digitalInput.read();
						try {
							parameters.put(pinDigitalInput.getJson());
			            } catch (Exception e) {}
					}else if(pin instanceof PinUart){ // PinUart
						PinUart pinUart = (PinUart)pin;
						if (pinUart.uart!=null) {
							try {
								InputStream in = pinUart.uart.getInputStream();
								int len = in.available();
								if (len>0) {
									System.out.println(TAG + " uart" + pinUart.bus + " avail=" + len);
									byte[] buffer = new byte[len];
									in.read(buffer);
									System.out.println(TAG + " uart" + pinUart.bus + " buffer=" + bytesToHex(buffer));
									pinUart.input = new String(buffer, "ASCII");
									parameters.put(pinUart.getJson());
								}
				            } catch (Exception e) {}
							if (pinUart.output!=null) {
								try {
									OutputStream out = pinUart.uart.getOutputStream();
									byte[] buffer = pinUart.output.getBytes();
									out.write(buffer);
					            } catch (Exception e) {}
						        pinUart.output = null;
					        }
				        }
					}else if(pin instanceof PinTwi){ // PinTwi
						PinTwi pinTwi = (PinTwi)pin;
						if (pinTwi.twi!=null && pinTwi.start) {
							try {
								pinTwi.start = false;
								System.out.println(TAG + " twi" + pinTwi.bus + " request=" + bytesToHex(pinTwi.request));
								pinTwi.twi.writeRead(pinTwi.addr, false, pinTwi.request, pinTwi.request.length,
									pinTwi.response, pinTwi.readLen);
								System.out.println(TAG + " twi" + pinTwi.bus + " response=" + bytesToHex(pinTwi.response));
								parameters.put(pinTwi.getJson());
				            } catch (Exception e) {}
				        }
					}else if(pin instanceof PinSpi){ // PinSpi
						PinSpi pinSpi = (PinSpi)pin;
						if (pinSpi.spi!=null && pinSpi.start) {
							try {
								pinSpi.start = false;
								System.out.println(TAG + " spi" + pinSpi.bus + " request=" + bytesToHex(pinSpi.request));
								pinSpi.spi.writeRead(pinSpi.slave, pinSpi.request, pinSpi.request.length,
									pinSpi.request.length, pinSpi.response, pinSpi.response.length);
								System.out.println(TAG + " spi" + pinSpi.bus + " response=" + bytesToHex(pinSpi.response));
								parameters.put(pinSpi.getJson());
				            } catch (Exception e) {}
				        }
					}
				}

				PluginResult result = new PluginResult(PluginResult.Status.OK, parameters);
                result.setKeepCallback(true);
				eventListener.sendPluginResult(result);
			}
		};
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		initPins();

		/*
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			// User clicked the notification. Need to stop the service.
			nm.cancel(0);
			stopSelf();
		} else {

			Notification notification = new Notification(
					R.drawable.ic_launcher, "IOIO service running",
					System.currentTimeMillis());
			notification
					.setLatestEventInfo(this, "IOIO Service", "Click to stop",
							PendingIntent.getService(this, 0, new Intent(
									"stop", null, this, this.getClass()), 0));
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			nm.notify(0, notification);
		}
		//*/
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
