package net.tijos.gas;

import java.io.IOException;

import net.tijos.gas.base.GPIO;
import net.tijos.gas.base.Module;
import net.tijos.gas.base.Switch;
import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.transducer.led.TiLED;

public class Led extends Module implements Switch {
	private TiLED led;
	protected Led(String name, TiGPIO gpio, GPIO.PIN pin) throws IOException {
		super(0, name);
		
		led = new TiLED(gpio, pin.getPinId());
		led.turnOff();
	}

	@Override
	public void on() throws IOException {
		led.turnOn();
	}

	@Override
	public void off() throws IOException {
		led.turnOff();
	}

	@Override
	public boolean isOff() {
		return !led.isTurnedOn();
	}

}
