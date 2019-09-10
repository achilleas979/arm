package osgi_SmartPhilipsHUELight;

import java.io.IOException;
import java.net.MalformedURLException;

import osgi_annotations.interfaces.*;

@ClassDescription(value = "SmartPhilipsHUELight is an OSGi bundle for communicating with a Philips HUE Light. "
		+ "This OSGi bundle implements four basic functionalities for interacting with the smart light: "
		+ "\t* Turn Light On\t* Turn Light Off\t* Dim Light\t* Set Light Level")
public class SmartPhilipsHUELight {

	@MethodDescription(value = "TurnLightOn method turns the light On. Returns true if the light is turned On, otherwise false.")
	public boolean TurnLightOn() {
		
		boolean lightIsOn = InternalOperationTurnLightOn();

		return lightIsOn;
	}

	@MethodDescription(value = "TurnLightOff method turns the light Off. Returns true if the light is turned Off, otherwise false.")
	public boolean TurnLightOff() {

		boolean lightIsOff = InternalOperationTurnLightOff();

		return lightIsOff;
	}

	@MethodDescription(value = "DimLight method will dim the light. Returns true if the light is Dimmed, otherwise false.")
	public boolean DimLight() {

		boolean lightIsDimmed = InternalOperationDimLight();

		return lightIsDimmed;
	}

	@MethodDescription(value = "SetLightLevel method sets the light level, based on the whole number passed as paremeter. "
			+ "Returns true if the light level is set, otherwise false.")
	public boolean SetLightLevel(@ParameterDescription(value="Parameter passed is the level of the light between 1 and 265.") int level) {

		boolean lightLevelIsSet = InternalOperationSetLightLevel(level);

		return lightLevelIsSet;
	}

	@MethodDescription(value = "SetLightColor method sets the light level, based on the whole number passed as paremeter. "
			+ "Returns true if the light level is set, otherwise false. "
			+ "This is a wrapping value between 0 and 65535. Both 0 and 65535 are red, 25500 is green and 46920 is blue.")
	public boolean SetLightColor(@ParameterDescription(value="Parameter passed is the hue color between 0 and 65535.") int hueColor) {
		System.out.println("From SetLightColor: Light should have color hue: " + hueColor + ".");

		boolean lightColorIsSet = InternalOperationSetColor(hueColor);

		return lightColorIsSet;
	}

	private boolean InternalOperationTurnLightOn() {
		try {
			String dataTurnLightOn = "{\"on\":true}";

			return Helper.requestToSmartLight(dataTurnLightOn);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean InternalOperationTurnLightOff() {
		try {
			String dataTurnLightOff = "{\"on\":false}";

			return Helper.requestToSmartLight(dataTurnLightOff);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean InternalOperationDimLight() {
		try {
			String dataDimLight = "{\"alert\":\"select\"}";

			return Helper.requestToSmartLight(dataDimLight);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean InternalOperationSetLightLevel(int level) {
		try {
			String dataSetLightLevel = "{\"bri\":" + level + "}";

			return Helper.requestToSmartLight(dataSetLightLevel);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	// This is a wrapping value between 0 and 65535. Both 0 and 65535 are red,
	// 25500 is green and 46920 is blue.
	private boolean InternalOperationSetColor(int hueColor) {
		try {
			String dataSetColor = "{\"hue\":" + hueColor + "}";

			return Helper.requestToSmartLight(dataSetColor);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

}