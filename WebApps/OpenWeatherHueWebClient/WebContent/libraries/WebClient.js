// (Hardware Service) Actuator - SmartPhilipsHUELight ARM RESTful URL
var uriHue = "http://localhost:9050/services/SmartPhilipsHUELight/";
// (Software Service) Sensor - OpenWeather ARM RESTful URL
var uriOpenWeather = 'http://localhost:9050/services/OpenWeather/';

// SmartPhilipsHUELight
function TurnLightOn(resultElementId) {

    CallToARMHardwareSensor(resultElementId, "TurnLightOn", "Turn Light On");
}

function TurnLightOff(resultElementId) {

    CallToARMHardwareSensor(resultElementId, "TurnLightOff", "Turn Light Off");
}

//OpenWeather
function GetWeatherByCityName(resultElementId, CityNameInput) {

    var CityNameInputValue = document.getElementById(CityNameInput).value;

    if (CityNameInputValue == null || CityNameInputValue == "") {
        alert('Please enter the city name.');
        return;
    } else if (!isNaN(CityNameInputValue)) {
        alert('Please enter a name and not a number, e.g., London or Barcelona.');
        return;
    }

    CallToARMSoftwareSensor(resultElementId, "GetWeatherByCityName/" + CityNameInputValue, "GetWeatherByCityName");
}

function GetWeatherByCityNameCountryCode(resultElementId, CityNameInput, CountryCodeInput) {

    var CityNameInputValue = document.getElementById(CityNameInput).value;
    var CountryCodeInputValue = document.getElementById(CountryCodeInput).value;

    if (CityNameInputValue == null || CityNameInputValue == "") {
        alert('Please enter the city name.');
        return;
    } 
    else if (CountryCodeInputValue == null || CountryCodeInputValue == "") {
        alert('Please enter the country code.');
        return;
    }
    else if (!isNaN(CityNameInputValue)) {
        alert('Please enter a name and not a number, e.g., London or Barcelona.');
        return;
    } else if (!isNaN(CountryCodeInputValue)) {
        alert('Please enter a name and not a number, e.g., UK or CY or FR.');
        return;
    }

    CallToARMSoftwareSensor(resultElementId, "GetWeatherByCityNameCountryCode/" + CityNameInputValue + "/" + CountryCodeInputValue, "GetWeatherByCityNameCountryCode");
}

function CallToARMSoftwareSensor(resultElementId, methodName, logicalActionName) {
    var url = uriOpenWeather + methodName;

    var xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.send(null);
    xhr.onreadystatechange = function() {

        if (xhr.readyState == 4) {
            if (xhr.status == 200) {
                var responseTxt = xhr.responseText.replace(/\"/g, "");
                document.getElementById(resultElementId).innerHTML = responseTxt;
                if (responseTxt>25)
                    TurnLightOn('TurnLightOn');
                else
                    TurnLightOff('TurnLightOff');
                    
            } else {
                document.getElementById(resultElementId).innerHTML = 'Error ' + xhr.response;
            }
        }
    };
}

function CallToARMHardwareSensor(resultElementId, methodName, logicalActionName) {
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

            document.getElementById('HueLightResult').innerHTML = resultTxt;
        }
    };
}
