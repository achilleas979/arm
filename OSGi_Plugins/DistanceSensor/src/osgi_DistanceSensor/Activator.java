package osgi_DistanceSensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

	private static BundleContext context;
	private ServiceRegistration <DistanceSensor> registrationOutOsgi;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;

		
		DistanceSensor service = new DistanceSensor();
		registrationOutOsgi = context.registerService(DistanceSensor.class, service, null);
		System.out.println("Distance Sensor Plugin Started!");
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

		
		Activator.context = null;
		registrationOutOsgi.unregister();
		
		System.out.println("Distance Sensor plugin Stopped!");
	}

}
