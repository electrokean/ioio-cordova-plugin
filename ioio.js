var ioio = {
	PIN_OUTPUT_PWM : "pwmOutput",
	PIN_OUTPUT_DIGITAL : "digitalOutput",
	PIN_INPUT_DIGITAL : "digitalInput",
	PIN_INPUT_ANALOG : "analogInput",
	PIN_UART : "uart",
	PIN_TWI : "twi",
	PIN_SPI : "spi",
	pinListeners:[],
	addValuePinListener: function(pin,value,callback){
		if(this.pinListeners[pin]){
			this.pinListeners[pin].push({'prevValue':value,'value':value,'callback':callback});
		}else{
			this.pinListeners[pin] = [{'prevValue':value,'value':value,'callback':callback}];
		}
	},
	addPinListener: function(pin,callback){
		this.addValuePinListener(pin,0,callback);
	},
	removePinListener: function(pin,callback){
		if (this.pinListeners[pin]) {
			for(var j=that.pinListeners[pin].length;j>0;j--){
				if (this.pinListeners[pin][j].callback == callback)
					this.pinListeners[pin].splice(j, 1);
			}
		}
	},
	removeAllPinListeners: function(){
		pinListeners = [];
	},
	open: function(options,succ,fail,allListener) {
		var that = this;
        cordova.exec(
			function(vals){ // DIRECT IOIO LISTENER
				if (succ) {
					succ();
					succ = null;
				}

				if (allListener) {
					try{
						allListener(vals);
					}catch(e){
						console.log('IOIO Callback function error' ,e);
			        }
				}

				for(var i=0;i<vals.length;i++){
					var pin = vals[i];
					switch(pin.class){
						case that.PIN_OUTPUT_DIGITAL:
							break;
						case that.PIN_OUTPUT_PWM:
							break;
						case that.PIN_INPUT_DIGITAL:
							if(that.pinListeners[pin.pin]){
								for(var j=0;j<that.pinListeners[pin.pin].length;j++){								
									that.pinListeners[pin.pin][j].callback(pin.value);
								}
							}
							break;
						case that.PIN_INPUT_ANALOG:
							if(that.pinListeners[pin.pin]){
								for(var j=0;j<that.pinListeners[pin.pin].length;j++){

									if(that.pinListeners[pin.pin][j].value >0){
										if(that.pinListeners[pin.pin][j].value == pin.value){
											continue;
										}
										if(that.pinListeners[pin.pin][j].value > that.pinListeners[pin.pin][j].prevValue
											&&
											that.pinListeners[pin.pin][j].value > pin.value
										){continue;}
									
										if(that.pinListeners[pin.pin][j].value < that.pinListeners[pin.pin][j].prevValue
											&&
											that.pinListeners[pin.pin][j].value < pin.value
										){continue;}
									}

									that.pinListeners[pin.pin][j].prevValue = pin.value;
									that.pinListeners[pin.pin][j].callback(pin.value);
								}
							}
							break;
						case that.PIN_UART: 	// listener should be on rxPin
							if(that.pinListeners[pin.pin]){
								for(var j=0;j<that.pinListeners[pin.pin].length;j++){
									that.pinListeners[pin.pin][j].callback(pin.value);
								}
							}
							break;
						case that.PIN_TWI: 		// listener should be on "virtual pin" bus#+64
							if(that.pinListeners[pin.pin]){
								for(var j=0;j<that.pinListeners[pin.pin].length;j++){
									that.pinListeners[pin.pin][j].callback(pin.value);
								}
							}
							break;
						case that.PIN_SPI: 		// listener should be on "virtual pin" bus#+128
							if(that.pinListeners[pin.pin]){
								for(var j=0;j<that.pinListeners[pin.pin].length;j++){
									that.pinListeners[pin.pin][j].callback(pin.value);
								}
							}
							break;
					}
				}

				setTimeout(function(){
					cordova.exec(function(){},function(){},'IOIOCommunication','repeatMainListener',[]);
				}, options.delay);
			},
			function(params){
				succ = null;
				allListener = null;
				if(fail){
					fail(params);
				}

			},
            'IOIOCommunication',
            'openIOIO',
            [options]
        );
    },
 	close: function(succ, fail) {
    	cordova.exec(
            succ || function(){},
            fail || function(){},
            'IOIOCommunication',
            'stopIOIO',
			[]
        );
	},
 	setPwmOutput: function(pin, freq, succ, fail) {
    	cordova.exec(
            succ || function(){},
            fail || function(){},
            'IOIOCommunication',
            'setPwmOutput',
        	[pin,freq]
        ); 
	},
 	setDigitalOutput: function(pin, output, succ, fail) {
    	cordova.exec(
            succ || function(){},
            fail || function(){},
            'IOIOCommunication',
            'setDigitalOutput',
        	[pin,output]
        );
	},
 	toggleDigitalOutput: function(pin, succ, fail) {
    	cordova.exec(
            succ || function(){},
            fail || function(){},
            'IOIOCommunication',
            'toggleDigitalOutput',
        	[pin]
        ); 
	},
    writeUart: function(bus, data, succ, fail) {
    	cordova.exec(
            succ || function(){},
            fail || function(){},
            'IOIOCommunication',
            'writeUart',
			[bus,data]
        );
	},
    writeReadTwi: function(bus, addr, request, readLen, succ, fail) {
    	cordova.exec(
            succ || function(){},
            fail || function(){},
            'IOIOCommunication',
            'writeReadTwi',
			[bus,addr,request,readLen]
        );
	},
    writeReadSpi: function(bus, slave, request, succ, fail) {
    	cordova.exec(
            succ || function(){},
            fail || function(){},
            'IOIOCommunication',
            'writeReadSpi',
			[bus,slave,request]
        );
	}
};

window.ioio = ioio;
