package armcore;

public class Config {
	/**
	 * OSGI plugins will need to be added here for the REST plugins to be created
	 */
	public static String osgiFolderFilePath = Activator.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "plugins/";

	/**
	 * File name of the definition of the plugin. This needs to be in the jar file of the bundle.
	 * This file can be only under the jar with out any other folders.
	 */
	public static String definitionXMLFileName = "osgi_plugin_def.xml"; 


	public static String packageName = "armcore";
	
	/**
	 * Gets the current file path
	 */
	public static String currentProjectPath = Activator.class.getProtectionDomain().getCodeSource().getLocation().getPath();

}
