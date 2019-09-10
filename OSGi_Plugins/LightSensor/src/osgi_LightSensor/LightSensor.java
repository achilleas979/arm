package osgi_LightSensor;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;

import osgi_LightSensor.Helper;
import osgi_annotations.interfaces.ClassDescription;
import osgi_annotations.interfaces.MethodDescription;

@ClassDescription(value = "LightSensor is an osgi bundle that represent a light sensor. This plugin provide SSE that feeded from the sensor and then serve it to client.")
public class LightSensor extends ResourceConfig {
	private static String sensorC = Helper.getSensorEvent(); //"slight";
	private String LocalNetworkIP = Helper.getHwBoardIpAddress(); //"192.168.1.135";
	
	@MethodDescription(value = "Return the Sensor type. 1 : Motion sensor, 2: Distance sensor 3: Light Sensor")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public int getStype() {
		int stype = 3; /// motion sensor
		return stype;
	}

	@MethodDescription(value = "Return the Server Sent Event from the light sensor. The event name is 'light'. When is dark then a 'dark' data pass in else, it pass a 'none' data as message.")
	@GET
	@Consumes(SseFeature.SERVER_SENT_EVENTS)
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public EventOutput getServerSentEvents() {
		
		final EventOutput eventOutput = new EventOutput();
		Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
		WebTarget target = client.target("http://" + LocalNetworkIP + "/" + sensorC);
		final EventInput eventInput = target.request().get(EventInput.class);

		new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				try {
					while (!eventInput.isClosed()) {
						// If it is not sychronise well use:
						// Thread.sleep(500);
						InboundEvent inboundEvent = eventInput.read();

						if (inboundEvent == null) {
							// connection has been closed
							break; 
						}
						try {
							//Handle event and retransmit it. You can add event id if required.
							//For Debug
							//System.out.println(inboundEvent.readData(String.class));
							OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
							eventBuilder.name(inboundEvent.getName());
							eventBuilder.data(inboundEvent.readData(String.class));
							OutboundEvent event = eventBuilder.build();
							eventOutput.write(event);
						} catch (IOException e) {
							try {
								eventOutput.close();
								eventInput.close();
							} catch (IOException ioClose) {
								throw new RuntimeException("Error when closing the event output internal.", ioClose);
							}
							throw new RuntimeException("Error when writing or reading the event.", e);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (!eventOutput.isClosed()) {
							eventOutput.close();
						}
						if (!eventInput.isClosed()) {
							eventInput.close();
						}
					} catch (IOException ioClose) {
						throw new RuntimeException("Error when closing the event output.", ioClose);
					}
				}
			}
		}).start();
		return eventOutput;
	}

}
