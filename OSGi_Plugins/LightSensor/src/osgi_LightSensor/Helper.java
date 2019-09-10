package osgi_LightSensor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Helper {
		/**
	 * Gets the ipaddress property value from
	 * the motion.properties file of the base folder
	 *
	 * @return ipaddress string
	 * @throws IOException
	 */
	public static String getHwBoardIpAddress(){
		String ipaddressString = null;
			Properties prop = new Properties();
	    	InputStream input = null;
	    	
	    	try {
	        
	    		String filename = "light.properties";
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
	 * Gets the sensorname property value from
	 * the motion.properties file of the base folder
	 *
	 * @return sensorname string
	 * @throws IOException
	 */
	public static String getSensorEvent(){

		String sensoreventString = null;
		Properties prop = new Properties();
    	InputStream input = null;
    	
    	try {
        
    		String filename = "light.properties";
    		input = Helper.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + filename);
    		    return "Sorry, unable to find " + filename;
    		}

    		//load a properties file from class path, inside static method
    		prop.load(input);
 
       
 
    	
    //retrieve the property we are interested, the app.version
    		sensoreventString = prop.getProperty("sensorevent");
    System.out.println("sensoreventString " + sensoreventString);
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

    return sensoreventString;
	}
	
}
