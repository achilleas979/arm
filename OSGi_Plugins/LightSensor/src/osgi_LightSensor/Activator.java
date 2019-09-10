package osgi_LightSensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

	private static BundleContext context;
	private ServiceRegistration <LightSensor> registrationOutOsgi;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;

		
		LightSensor service = new LightSensor();
		registrationOutOsgi = context.registerService(LightSensor.class, service, null);
		System.out.println("Light Sensor Plugin Started!");
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

		
		Activator.context = null;
		registrationOutOsgi.unregister();
		
		System.out.println("Light Sensor plugin Stopped!");
	}

}
