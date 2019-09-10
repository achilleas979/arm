package osgi_MotionSensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

	private static BundleContext context;
	private ServiceRegistration <MotionSensor> registrationOutOsgi;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;

		
		MotionSensor service = new MotionSensor();
		registrationOutOsgi = context.registerService(MotionSensor.class, service, null);
		System.out.println("Motion Sensor Plugin Started!");
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

		
		Activator.context = null;
		registrationOutOsgi.unregister();
		
		System.out.println("Motion Sensor plugin Stopped!");
	}

}
