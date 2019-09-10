package armcore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private static ServiceRegistration<ConnectionControl> registration;
	private static ConnectionControl CC; 
	private static Dictionary<String, String> props; 

	private static boolean DEBUG = false;

	private static boolean updateManifest = true;
	private static boolean installBundlesFromFolder = true;
	private static boolean createRestFile = true;
	private static boolean compileSourceFileOsgi = true;
	private static boolean registerRestPlugins = true;
	
 

	public static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		//System.out.println("ARM Starting..");
		Activator.context = bundleContext;

		CC = new ConnectionControl();

		props = new Hashtable<String, String>();
		props.put("ecf.jaxrs.jersey.server.alias", "/jersey");

		registration = bundleContext.registerService(ConnectionControl.class, CC, props);

		ArrayList<String> pluginFullNames = new ArrayList<String>();
		 ArrayList<String> pluginFileNames = new ArrayList<String>();
		 ArrayList<String> reqBundleDefinitions = new ArrayList<String>();
		 ArrayList<String> restPluginFullNames = new ArrayList<String>();
		 ArrayList<String> restPluginsJavaCodeList = new ArrayList<String>();

		if (updateManifest) {
			// Get the file path for the OSGI from Config
			final File folder = new File(Config.osgiFolderFilePath);
			//System.out.println("Definition file path: " + Config.osgiFolderFilePath);

			CC.readXMLDefinitionFiles(pluginFullNames, pluginFileNames, reqBundleDefinitions, folder);

			if (DEBUG && reqBundleDefinitions.size() == 0) {
				reqBundleDefinitions.add("Osgi_Equinox;bundle-version=\"1.0.1\"");
				reqBundleDefinitions.add("Osgi_Equinox;bundle-version=\"1.0.2\"");
			}
			CC.updateManifest(reqBundleDefinitions);
		}

		// This will install from plugin directory folder
		if (installBundlesFromFolder) {
			CC.loadPlugins(bundleContext, new ArrayList<Bundle>());
		}

		// If OSGi in not ACTIVE, then the bundle is installed and started
		if (createRestFile) {
			if (DEBUG && pluginFullNames.size() == 0) {
				pluginFullNames.add("osgi_SmartPhilipsHUELight.SmartPhilipsHUELight");
			}
			restPluginFullNames = CC.createRestFiles(Config.packageName, pluginFullNames, pluginFileNames, restPluginsJavaCodeList);
		}

		// Compile the newly created java files
		if (compileSourceFileOsgi) {
			String filePathName = Config.currentProjectPath + "src/" + Config.packageName;

			boolean codeGeneration = CC.runSourceFileOsgi(restPluginFullNames, restPluginsJavaCodeList, filePathName);
		}

		// Register the new compiled class files with the REST
		if (registerRestPlugins) {
			if (DEBUG && restPluginFullNames.size() == 0) {
				restPluginFullNames.add(Config.packageName + "." + 0);
			}
			CC.registerRestPlugins(getContext(), restPluginFullNames);
		}

		//System.out.println("ARM Started!");
	}

	public void stop(BundleContext bundleContext) throws Exception {

		registration.unregister();
		
		//System.out.println("ARM Stopped!");
	}
	
	
	public static void reload(BundleContext bundleContext) throws Exception {
		//System.out.println("ARM Reloading..");
		
		ArrayList<String> pluginFullNames = new ArrayList<String>();
		 ArrayList<String> pluginFileNames = new ArrayList<String>();
		 ArrayList<String> reqBundleDefinitions = new ArrayList<String>();
		 ArrayList<String> restPluginFullNames = new ArrayList<String>();
		 ArrayList<String> restPluginsJavaCodeList = new ArrayList<String>();

		if (updateManifest) {
			// Get the file path for the OSGI from Config
			final File folder = new File(Config.osgiFolderFilePath);
			//System.out.println("Definition file path: " + Config.osgiFolderFilePath);

			CC.readXMLDefinitionFiles(pluginFullNames, pluginFileNames, reqBundleDefinitions, folder);

			if (DEBUG && reqBundleDefinitions.size() == 0) {
				reqBundleDefinitions.add("Osgi_Equinox;bundle-version=\"1.0.1\"");
				reqBundleDefinitions.add("Osgi_Equinox;bundle-version=\"1.0.2\"");
			}
			CC.updateManifest(reqBundleDefinitions);
		}

		// This will install from plugin directory folder
		if (installBundlesFromFolder) {
			CC.loadPlugins(bundleContext, new ArrayList<Bundle>());
		}
		long startTime = System.nanoTime();
		// If OSGi in not ACTIVE, then the bundle is installed and started
		if (createRestFile) {
			if (DEBUG && pluginFullNames.size() == 0) {
				pluginFullNames.add("osgi_SmartPhilipsHUELight.SmartPhilipsHUELight");
			}
			restPluginFullNames = CC.createRestFiles(Config.packageName, pluginFullNames, pluginFileNames, restPluginsJavaCodeList);
		}

		// Compile the newly created java files
		if (compileSourceFileOsgi) {
			String filePathName = Config.currentProjectPath + "src/" + Config.packageName;

			boolean codeGeneration = CC.runSourceFileOsgi(restPluginFullNames, restPluginsJavaCodeList, filePathName);
		}
		

		// Register the new compiled class files with the REST
		if (registerRestPlugins) {
			if (DEBUG && restPluginFullNames.size() == 0) {
				restPluginFullNames.add(Config.packageName + "." + 0);
			}
			/*try {
				Thread.sleep(2600);//this is a delay for code generation to complete
				// then ARM to start considering the new REST/SSE events
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			
			CC.registerRestPlugins(getContext(), restPluginFullNames);
			long elapsedTime = System.nanoTime() - startTime;
			
			//System.out.println("Elapsed time: " + TimeUnit.NANOSECONDS.toMillis(elapsedTime));
			try {
			    Files.write(Paths.get("C:/Users/achilleas/Desktop/Eclipse/arm/results.txt"), (System.lineSeparator() + Long.toString(TimeUnit.NANOSECONDS.toMillis(elapsedTime))).getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
		}

		//System.out.println("ARM Reloaded!");
		
		System.exit(0);
	}
	
}