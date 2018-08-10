## ARM Plugin using the OpenWeather API

In this task we will create a Virtual Sensor (i.e., software service) as an ARM specific Java OSGi plugin for the middleware that allows communicating with the OpenWeather API.  

1: Go to File-> New -> Project-> Plug-in Project, type the Project Name OpenWeather, select in the Target Platform of the Wizard an OSGI framework and Equinox from the drop down. Click Next.

2:  Remove the .qualifier from the version text field and click Finish. Click Open Perspective in the loaded window. The manifest is loaded. Click on the MANIFEST tab.

3:  Copy the following and replace the text in the loaded manifest. This adds the necessary ARM middleware dependencies and other configuration details. Leave a single empty line at the end of the file and click Save. Ignore the error about the exported package as it will go away in the next step. 

```
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: OpenWeather
Bundle-SymbolicName: OpenWeather
Bundle-Version: 1.0.0
Bundle-Activator: osgi_openweather.Activator
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Bundle-ActivationPolicy: lazy
Require-Bundle: com.eclipsesource.jaxrs.jersey-min;bundle-version="2.22.2",
 org.eclipse.equinox.console;bundle-version="1.0.100",
 org.apache.felix.gogo.command;bundle-version="0.10.0",
 org.apache.felix.gogo.runtime;bundle-version="0.10.0",
 org.apache.felix.gogo.shell;bundle-version="0.10.0",
 org.eclipse.osgi;bundle-version="3.9.1",
 com.google.gson;bundle-version="2.5.0"
Import-Package: javax.accessibility,
 javax.ws.rs;version="2.0.0",
 javax.ws.rs.core;version="2.0.1",
 org.osgi.framework;version="1.3.0",
 osgi_annotations.interfaces
Export-Package: osgi_openweather
```
When developing another ARM Java OSGi plugin (in the future) only the following details should be changed: i.e., plugin name, symbolic name, version, the path/package to the Activator class and the source code package for the plugin.

4:  Expand the OpenWeather project in the Project Explorer on the left, right click on the src folder, go to the New-> Package and type the name osgi_openweather for the package. This is the name defined in two places in the above manifest file.

5: Right click on the osgi_openweather package, go to the New-> Class and type the name Activator for the class that controls the plugin lifecycle, e.g., start, stop and click Finish. This is the name of the Bundle-Activator defined in the above manifest file.

6: Copy the following code and replace the loaded Activator class code. The two important parts to notice is the start and the stop methods that basically register the OpenWeather service class when the plugin is started and unregister it when the plugin is stopped. Ignore the errors since the OpenWeather service class will be created in the next step. Spend some minutes to study the code.

```
package osgi_openweather;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

    private static BundleContext context;
    private ServiceRegistration<OpenWeather> registrationOutOsgi;
    
    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        
        OpenWeather service = new OpenWeather();
        registrationOutOsgi = context.registerService(OpenWeather.class, service, null);

        System.out.println("Osgi OpenWeather Started!");
        
    }

    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;

        registrationOutOsgi.unregister();
        
        System.out.println("Osgi OpenWeather Stopped!");
    }

}
```

7: Right click on the osgi_openweather package, go to the New-> Class, type the name OpenWeather for the main service class of the plugin and click Finish. Replace the loaded code by copying the code below. The class offers the GetWeatherByCityName and GetWeatherByCityNameCountryCode methods for retrieving the temperature in Celsius from the OpenWeather API. 

The important parts to notice are the annotations @ClassDescription, @MethodDescription and @ParameterDescription. These are not needed by the Java OSGi specification but they must be defined since they are used by the ARM middleware to automatically generate the RESTful API service interfaces for the two methods and their corresponding documentation as soon as the plugin is installed. Spend some minutes to study the code.  
  
Ignore the errors since they will be resolved in the next two steps.

```
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
```

8: Expand the ARM_Core project from the Project Explorer on the left, copy the Osgi_Annotations_1.0.0.0.jar, right click on the OpenWeather project and paste it. Right click on the OpenWeather project, go to Properties-> Java Build Path-> Libraries-> Add Jars, find the JAR from the OpenWeather project, select it and click Apply and Close. This ARM library enables defining annotations in the service class so that it can be parsed by the middleware. 
 
9: We have to define now the Helper.java class that actually implements the two calls to the OpenWeather API. Right click on the osgi_openweather package and go to New-> Class, type Helper and click Finish. Replace the loaded code with:

```
package osgi_openweather;

import java.io.BufferedReader;
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
    private static String OpenWeatherAPI_appID = "appid=<GET-APP-ID-FROM-OPENWEATHER-API>";
    
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
                            return output;
                        }
                }
                else{
                    br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())));
    
                    String output;
                    System.out.println("Output from Server .... \n");
                    while ((output = br.readLine()) != null) {
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
```

Spend some minutes to study the code. The code simply invokes the OpenWeather API to get the weather by city name or by city name and country code as shown in the example URL calls below:
•	https://api.openweathermap.org/data/2.5/weather?q=London&units=metric&appid=<GET-APP-ID-FROM-OPENWEATHER-API>
•	https://api.openweathermap.org/data/2.5/weather?q=London,uk&units=metric&appid=<GET-APP-ID-FROM-OPENWEATHER-API>

10: The next step involves creating the XML file needed by the ARM middleware to find the main OpenWeather service class that contains the annotations to generate automatically the REST API methods and their corresponding developer documentation. Right click on the src folder and go to New-> Other type File and click Next and then type osgi_plugin_def.xml in the filename. Click Finish, click on the source tab at the bottom of the loaded XML file and add the content below that defines the main service class path and the JAR that will be exported for the OpenWeather plugin.

```
<bundle-definition>
    <fullname>
        osgi_openweather.OpenWeather
    </fullname>
    <filename>
        OpenWeather_1.0.0.jar
    </filename>
</bundle-definition>
```

11: Right click on the OpenWeather project, select Export, type Deployable plug-ins and fragments, select it and click Next, make sure only the OpenWeather plugin is checked at the top of the wizard, browse to Desktop under Directory and click on Options tab to confirm that only the Package plug-ins as individual JAR archives is checked. Click Finish and the OpenWeather_1.0.0.jar should be exported on the Desktop in a created plugins/ folder. 

12: Go to Run-> Run History -> ARM_Core to start the middleware. In the console at the bottom you should see now the osgi> input console running. 

13: Type ss to see the OSGi services that have started including the ARM middleware (ARM_Core_1.0.0.0).  Open, e.g., the PostMan application, type http://localhost:9050/services/ and click Send. You should get No available OSGis since the ARM does not have any plug-ins installed and loaded currently.   

14: Go to Window-> Preferences, type in left top text-field hooks, select Workspace on the left panel and select the check box Refresh using native hooks and polling and click Apply and Close. This configuration is performed to be able to load dynamically in Eclipse the Java service classes automatically generated by the ARM middleware. Copy and paste the OpenWeather_1.0.0.jar in the plugins folder in Eclipse ARM_Core project. The plugin should be started as shown in the console. Go to PostMan again and type http://localhost:9050/services/ and click Send. You should get now:

```
Available OSGI:
1. OSGI Description: OpenWeather is an OSGi bundle for communicating with a OpenWeather API. This OSGi bundle implements two basic functionalities for interacting with the OpenWeather API: 	* GetWeatherByCityName	* GetWeatherByCityNameCountryCode
	Path for available methods: /services/OpenWeather
```

15:  From the text returned in 14 click on the link /services/OpenWeather from PostMan and click Send. It will load the detailed documentation for the plugin. The generated ARM RESTful APIs are:
```
Definition: http://localhost:9050/services/OpenWeather/GetWeatherByCityName/{param0} 
Definition: http://localhost:9050/services/OpenWeather/GetWeatherByCityNameCountryCode/{param0}/{param1} 
```

16:  In PostMan you can try them as follows: 
```
Example use: http://localhost:9050/services/OpenWeather/GetWeatherByCityName/London
Example use: http://localhost:9050/services/OpenWeather/GetWeatherByCityNameCountryCode/London/uk
```

Congratulation you have completed and loaded your first ARM-specific OSGi plugin. 