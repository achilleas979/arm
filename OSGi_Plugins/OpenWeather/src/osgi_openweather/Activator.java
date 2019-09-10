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

