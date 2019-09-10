package osgi_openweather;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Helper {
 private static String OpenWeatherAPI_mainURL = "api.openweathermap.org/data/2.5/weather?q=";
    private static String OpenWeatherAPI_units = "units=metric";
    private static String OpenWeatherAPI_appID = "appid=cfeeecc403ae599eed49172110830bc5";
    
    
    
    public static String requestToOpenWeatherAPI(String queryParams)
            throws MalformedURLException, IOException, ProtocolException {
        URL url = new URL("https://" + OpenWeatherAPI_mainURL + "" + queryParams + "&" + OpenWeatherAPI_units + "&" + OpenWeatherAPI_appID);
        
        System.out.println("https://" + OpenWeatherAPI_mainURL + "" + queryParams + "&" + OpenWeatherAPI_units + "&" + OpenWeatherAPI_appID);
        
         try {

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                BufferedReader br = null;
                if (conn.getResponseCode() != 200) {
                    
                	
                		br = new BufferedReader(new InputStreamReader(
        	                    (conn.getErrorStream())));
                	
                		
                		 String output;
                		System.out.println("Output from Server .... \n");
                		while ((output = br.readLine()) != null) {
    	                    //System.out.println(output);
    	                    //System.out.println(parse(output));
    	                    return output;
    	                }
                }
                else{
                	br = new BufferedReader(new InputStreamReader(
    	                    (conn.getInputStream())));
	
	                String output;
	                System.out.println("Output from Server .... \n");
	                while ((output = br.readLine()) != null) {
	                    //System.out.println(output);
	                    //System.out.println(parse(output));
	                    return parse(output);
	                }
                }

                conn.disconnect();

              } catch (MalformedURLException e) {

                e.printStackTrace();

              } catch (IOException e) {

                e.printStackTrace();

              }

        return "Error occured";
    }
    
    public static String parse(String jsonLine) {
        JsonElement jelement = new JsonParser().parse(jsonLine);
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("main");
        String result = jobject.get("temp").getAsString();
        return result;
    }
}
