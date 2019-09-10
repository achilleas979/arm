package osgi_SmartPhilipsHUELight;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class Helper {
	private static String LocalNetworkIP = Helper.getHueBridgeIpAddress();//"localhost:8000"// "192.168.31.75"
	private static String UserId = Helper.getHueBridgeUserId();//"newdeveloper" 
	private static String LightNumber = Helper.getHueBridgeLight();//"2"//"1"
	
	//private static String LocalNetworkIP = "192.168.31.170";//"localhost:8000"// "192.168.31.75"
	//private static String UserId = "lqGLRJJWGRtrYe5s-tyDPzl330GCYdBcz6Nmuxg0";//"newdeveloper"
	//private static String LightNumber = "4";//"2"//"1"

	
	/**
	 * Gets the huebrige.ipaddress property value from
	 * the ./hue.properties file of the base folder
	 *
	 * @return huebrige.ipaddress string
	 * @throws IOException
	 */
	public static String getHueBridgeIpAddress(){
		String ipaddressString = null;
			Properties prop = new Properties();
	    	InputStream input = null;
	    	
	    	try {
	        
	    		String filename = "hue.properties";
	    		input = Helper.class.getClassLoader().getResourceAsStream(filename);
	    		if(input==null){
	    	            System.out.println("Sorry, unable to find " + filename);
	    		    return "Sorry, unable to find " + filename;
	    		}

	    		//load a properties file from class path, inside static method
	    		prop.load(input);
	 
	       
	 
	    	
	    //retrieve the property we are interested, the app.version
	    ipaddressString = prop.getProperty("ipaddress");
	    System.out.println("ipaddressString " + ipaddressString);
	    	} catch (IOException ex) {
	    		ex.printStackTrace();
	        } finally{
	        	if(input!=null){
	        		try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        	}
	        }

	    return ipaddressString;
	}
	
	
	/**
	 * Gets the huebrige.userid property value from
	 * the ./hue.properties file of the base folder
	 *
	 * @return huebrige.userid string
	 * @throws IOException
	 */
	public static String getHueBridgeUserId(){

		String useridString = null;
		Properties prop = new Properties();
    	InputStream input = null;
    	
    	try {
        
    		String filename = "hue.properties";
    		input = Helper.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + filename);
    		    return "Sorry, unable to find " + filename;
    		}

    		//load a properties file from class path, inside static method
    		prop.load(input);
 
       
 
    	
    //retrieve the property we are interested, the app.version
    		useridString = prop.getProperty("userid");
    System.out.println("useridString " + useridString);
    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally{
        	if(input!=null){
        		try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	}
        }

    return useridString;
	}
	
	/**
	 * Gets the huebrige.light property value from
	 * the ./hue.properties file of the base folder
	 *
	 * @return huebrige.light string
	 * @throws IOException
	 */
	public static String getHueBridgeLight(){

		String lightString = null;
		Properties prop = new Properties();
    	InputStream input = null;
    	
    	try {
        
    		String filename = "hue.properties";
    		input = Helper.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + filename);
    		    return "Sorry, unable to find " + filename;
    		}

    		//load a properties file from class path, inside static method
    		prop.load(input);
 
       
 
    	
    //retrieve the property we are interested, the app.version
    		lightString = prop.getProperty("light");
    System.out.println("lightString " + lightString);
    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally{
        	if(input!=null){
        		try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	}
        }

    return lightString;
	}
	
	public static boolean requestToSmartLight(String dataTurnLightOn)
			throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL("http://" + LocalNetworkIP + "/api/" + UserId + "/lights/" + LightNumber + "/state");
		
		System.out.println("info: " + LocalNetworkIP + "/" + UserId); 
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("PUT");
		conn.setRequestProperty("Accept", "application/json");

		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(dataTurnLightOn);
		out.close();

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		String output;
		StringBuilder outputSB = new StringBuilder();
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			System.out.println(output);
			outputSB.append(output);
		}
		System.out.println("\n");

		if (outputSB.length() != 0) {
			Gson gson = new Gson();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			ArrayList<LinkedTreeMap> resultList = gson.fromJson(outputSB.toString(), ArrayList.class);

			if (resultList != null && resultList.size() > 0) {
				if (resultList.get(0).containsKey("error")) {
					return false;
				}
			}
		}

		conn.disconnect();

		return true;
	}
}
