package osgi_openweather;
import java.io.IOException;
import java.net.MalformedURLException;
import osgi_annotations.interfaces.ClassDescription;
import osgi_annotations.interfaces.MethodDescription;
import osgi_annotations.interfaces.ParameterDescription;

@ClassDescription(value = "OpenWeather is an OSGi bundle for communicating with a OpenWeather API. This OSGi bundle implements two basic functionalities for interacting with the OpenWeather API: \t* GetWeatherByCityName\t* GetWeatherByCityNameCountryCode")
public class OpenWeather {
    
    @MethodDescription(value = "GetWeatherByCityName method gets weather temperature "
            + "in Celsius for a city from the OpenWeather API by its name. "
            + "Returns as a String the weather temperature using the city name.")
    public String GetWeatherByCityName(@ParameterDescription(value="Parameter passed is name of the city, e.g., London.") String cityName) {
        String weatherCity = InternalOperationGetWeatherByCityName(cityName);
        return weatherCity;
    }

   
 @MethodDescription(value = "GetWeatherByCityNameCountryCode method gets weather temperature "
            + "in Celsius for a city from the OpenWeather API by its name. "
       + "Returns as a String the weather temperature using the city name and country code.") 
    public String GetWeatherByCityNameCountryCode(@ParameterDescription(value="Parameter passed is name of the city, e.g., London.") String cityName, @ParameterDescription(value="Parameter passed is code of the country, e.g., UK.") String countryCode) {

        String weatherCityCountryCode = InternalOperationGetWeatherByCityNameCountryCode(cityName, countryCode);

        return weatherCityCountryCode;
    }
    
    
    private String InternalOperationGetWeatherByCityName(String cityName) {
        try {

            return Helper.requestToOpenWeatherAPI(cityName);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Error occured";
    }

  private String InternalOperationGetWeatherByCityNameCountryCode(String cityName, String countryCode) {
        try {

            return Helper.requestToOpenWeatherAPI(cityName+","+countryCode);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Error occured";
    }
       

}

