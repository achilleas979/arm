// Philips hue Middleware Address
var uriHue = "http://localhost:9050/services/SmartPhilipsHUELight/";
// Sensors Middleware Address
var uriMotion = 'http://localhost:9050/services/MotionSensor/getServerSentEvents';
var uriDistance = 'http://localhost:9050/services/DistanceSensor/getServerSentEvents';
var uriLight = 'http://localhost:9050/services/LightSensor/getServerSentEvents';

var source1;
var source2;
var source3;

// Initialize sensor values
var temp1 = "none";
var temp2 = "none";
var temp3 = "none";

var i = 0;
var dark = 1; // is day light not dark
// Rules Each rule is named as sse1 , sse2 ect.
function sse1() {
	// optional
	// TurnLightOff("out1");
	source1 = new EventSource(uriMotion);
	source1
			.addEventListener(
					'motion',
					function(e) {
						// console.log('System status is now: ' + e.data);
						// Rule Actions
						console.log('System status is now: ' + e.data);
						if (e.data == "motion") {
							document.getElementById("out1").innerHTML = e.data;
							if (i == 0) {
								TurnLightOn("out1");
								i = 1;
								document.getElementById("out1").innerHTML = e.data;
								// hue is on, ok all done
								source1.close();
							}
						} else if (e.data == "none") {
							document.getElementById("out1").innerHTML = e.data;

						} else if (e.data == "s") {

						} else {
							document.getElementById("out1").innerHTML = "Invalid event inserted. Compromized";
						}
					});
}

function sse2() {
	source2 = new EventSource(uriDistance);
	source2
			.addEventListener(
					'distance',
					function(e) {
						console.log('System status is now: ' + e.data);

						// Rule Actions
						if (e.data == "close") {
							document.getElementById("out2").innerHTML = e.data;
							// there is someone too close, turn light red and
							// dim it.
							DimLight("out2");
							
							
							//lightLevelInput
							document.getElementById("lightLevelInput").value = 245;
							SetLightLevel("out2", "lightLevelInput");
							
							//lightColorInput
							document.getElementById("lightColorInput").value = 65535;
							SetLightColor("out2", "lightColorInput");
							document.getElementById("out2").innerHTML = e.data;
						} else if (e.data == "none") {
							document.getElementById("out2").innerHTML = e.data;
							if (dark == 1) {
								document.getElementById("lightLevelInput").value = 125 ;
								SetLightLevel("out2", "lightLevelInput");
								document.getElementById("lightColorInput").value = 25500;
								SetLightColor("out2", "lightColorInput");
								document.getElementById("out2").innerHTML = e.data;
							}

							else if (dark == 0) {
								document.getElementById("lightLevelInput").value = 245;
								SetLightLevel("out2", "lightLevelInput");
								document.getElementById("lightColorInput").value = 46920;
								SetLightColor("out2", "lightColorInput");
								document.getElementById("out2").innerHTML = e.data;
							} else {
								document.getElementById("out2").innerHTML = "Invalid event inserted. Compromized";
							}

						} else if (e.data == "s") {

						} else {
							document.getElementById("out2").innerHTML = "Invalid event inserted. Compromized";
						}

					});
}

function sse3() {
	source3 = new EventSource(uriLight);
	source3
			.addEventListener(
					'light',
					function(e) {
						console.log('System status is now: ' + e.data);

						// Rule Actions
						if (e.data == "none") {
							document.getElementById("out3").innerHTML = e.data;
							// there is light, turn bulb strong blue
							dark = 0;
							document.getElementById("lightLevelInput").value = 245;
							SetLightLevel("out3", "lightLevelInput");
							document.getElementById("lightColorInput").value = 46920;
							SetLightColor("out3", "lightColorInput");
							document.getElementById("out3").innerHTML = e.data;
						} else if (e.data == "dark") {
							document.getElementById("out3").innerHTML = e.data;
							// there is dark, turn bulb light green
							dark = 1;
							document.getElementById("lightLevelInput").value = 125;
							SetLightLevel("out3", "lightLevelInput");
							document.getElementById("lightColorInput").value = 25500;
							SetLightColor("out3", "lightColorInput");
							document.getElementById("out3").innerHTML = e.data;

						} else if (e.data == "s") {

						} else {
							document.getElementById("out3").innerHTML = "Invalid event inserted. Compromized";
						}
					});
}

// Philips hue

function TurnLightOn(resultId) {

	CallToMiddleware("TurnLightOn", "Turn Light On", resultId);
}

function TurnLightOff(resultId) {

	CallToMiddleware("TurnLightOff", "Turn Light Off", resultId);
}

function DimLight(resultId) {

	CallToMiddleware("DimLight", "Dim Light", resultId);
}

function SetLightLevel(resultId, inputField) {

	var inputValue = document.getElementById(inputField).value;

	if (inputValue == null || inputValue == "") {
		alert('Please enter the light level.');
		return;
	} else if (isNaN(inputValue)) {
		alert('Please enter a positive whole number for light level, between 1 and 254.');
		return;
	}

	CallToMiddleware("SetLightLevel/" + inputValue, "Set Light Level", resultId);
}

function SetLightColor(resultId, inputField) {

	var inputValue = document.getElementById(inputField).value;

	if (inputValue == null || inputValue == "") {
		alert('Please enter the light color.');
		return;
	} else if (isNaN(inputValue)) {
		alert('Please enter a positive whole number for light level, between 0 and 65535.');
		return;
	}

	CallToMiddleware("SetLightColor/" + inputValue, "Set Light Color", resultId);
}

function CallToMiddleware(methodName, logicalActionName, resultId) {
	var url = uriHue + methodName;

	var xhr = new XMLHttpRequest();
	xhr.open('GET', url, true);
	xhr.send(null);
	xhr.onreadystatechange = function() {

		if (xhr.readyState == 4) {
			var resultTxt = "";
			if (xhr.status == 200) {
				if (xhr.response == true || xhr.response == "true") {
					resultTxt = 'Success ' + logicalActionName + '!';
				} else {
					resultTxt = 'Error ' + logicalActionName + ': '
							+ xhr.status + ' - ' + xhr.statusText;
				}

			} else {
				var resultTxt = 'Error ' + logicalActionName + ': '
						+ xhr.status + ' - ' + xhr.statusText;
			}

			document.getElementById(resultId).innerHTML = resultTxt;
		}
	};
}