package osgi_SmartPhilipsHUELight;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private ServiceRegistration<SmartPhilipsHUELight> registrationOutOsgi;
	
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		//Service should be a class of the Interface, and instantiate it with the implemented class
    	//Interface is the package that will be exported - package of Math Service
		SmartPhilipsHUELight service = new SmartPhilipsHUELight();
		registrationOutOsgi = context.registerService(SmartPhilipsHUELight.class, service, null);

		System.out.println("Osgi SmartPhilipsHUELight Started!");
		
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;

		registrationOutOsgi.unregister();
		
		System.out.println("Osgi SmartPhilipsHUELight Stopped!");
	}

}
