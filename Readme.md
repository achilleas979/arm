# Adaptive Runtime Middleware (ARM)

ARM is a Dynamic, Reconfigurable and Regenerative Middleware for IoT. This software system will have the ability upon installation, registration and deployment of smart plugins/modules, which allow communicating with sensors, actuators and cloud services, to discover their capabilities via method annotations (i.e., class metadata). This allows adapting its configuration at runtime, re-generating and re-deploying the middleware’s Web APIs, i.e., RESTful services.

The middleware can detect the functionalities of the deployed Java OSGi bundles (i.e., smart plugins/modules) in the network and expose them through REST web methods and SSE. This is done automatically by the middleware, which re-configures and regenerates itself, as well as generates the developer documentation for the generated Web APIs. 

With this approach the Application Developers (eg. Android, iOS, Web Developer) will have all required information and documentation so as to be able to develop client applications using the connected actuators, sensors and cloud services for practically any platform (e.g., Android, iOS, web app). Hence, they can use Heterogeneous  IoT Devices without introducing additional technologies and platforms that potentially may increase complexity and require from developers to learn new technologies.

## Getting Started

1. Fork the ARM middleware project to your computer.

### Prerequisites

1. Check that you have a Java JDK 8 installed: e.g., C:\Program Files\Java\jdk1.8.0_181 (64-bit) or e.g., C:\Program Files (x86)\Java\jdk1.8.0_181 (32-bit). If not please download and install it based on your computer architecture (e.g., 64-bit) and OS (e.g., Windows).

2. Download and install the Eclipse IDE for Java EE developers.  

### Installing

1: Open Eclipse IDE for Java EE. Go to File-> Import-> Expand General-> Existing Projects into Workspace click Next, click Browse and select from the Desktop the ARM_Core project, check Copy Projects into Workspace at the bottom and click Finish. Ignore errors until the end of Task 1 since configuration details are still defined in the next steps. 

2: To confirm Java SDK 8 and not Java JRE 8 is used by Eclipse, go to Window-> Preferences and on the left panel expand Java. Then click on Installed JREs, click Add, choose Standard VM and click Next, click on Directory, locate the path to the installed Java JDK based on the version, e.g., C:\Program Files\Java\jdk1.8.0_181\, click Finish. Then click the check box to select this JDK, click Apply and Close to set it as the Eclipse default.    

3: Then go to Window-> Preferences, type in left top text-field Target Platform, select Target Platform on the left panel and click Add. Click Next, change the name to ARM_Core, click Add, Select Directory, click Next, click Browse and locate/type the lib ARM_Core Eclipse project path, e.g., C:\Users\<user>\Desktop\ARM-Evaluation\eclipse\workspace\ARM_Core\lib. Finally, click Next, you should see a list of plugins loaded and click Finish & Finish. Select the ARM_Core check box and click Apply and Close. 

4: Then go again to Window-> Preferences, type in left top text-field API Baselines on the left panel and click Add Baseline. Then choose A Target Platform, click Next, type name ARM_Core, click check box for ARM_Core, click Refresh, click Finish, click Apply and Close and click Build to finalise the ARM installation. 

5: Go to File-> Import, type Launch Configurations, select it, click Next, browse to the downloaded ARM_Core folder and click Select folder. Then click on the ARM_Core folder on the left panel to select it, click the check box on the right panel to select the ARM_Core launch configuration and click Finish. Go to Run-> Run Configurations and on the left panel locate ARM_Core under OSGi framework, select it and click Run to start the middleware. In the console at the bottom you should see now the osgi> input console running. 

6: Type ss to see the OSGi services/plugins that have started including the ARM middleware (ARM_Core_1.0.0.0). E.g., using the PostMan application type http://localhost:9050/services/ and click Send. You should get No available OSGis since the ARM does not have any plug-ins installed and loaded currently. Type exit and then y to stop the middleware for now.

## Developing an example ARM plug-in using the OpenWeather API

[ARM specific Java OSGi plugin](docs/creating-arm-plugin.md)



## Developing a simple Web Application using ARM plugins

[Developing a Web Application using ARM specific Java OSGi plugins](docs/creating-a-webapp.md)

## Built With

* [Eclipse IDE](https://www.osgi.org/) - Eclipse IDE for Java EE.
* [OSGi](https://www.osgi.org/) - The Dynamic Module System for Java.
* [Equinox](http://www.eclipse.org/equinox/) -  Equinox as the OSGi implementation.
* [JAX-RS Specification](http://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html) - 	
JSR-000339 JavaTM API for RESTful Web Services 2.0.
* [Jersey](https://jersey.github.io/) - Jersey as the JAX-RS implementation.
* [OSGi-JAX-RS Connector](https://github.com/hstaudacher/osgi-jax-rs-connector) - The glue that connects OSGi and Jersey.
* [Tutorial: JAX-RS and OSGi together](https://eclipsesource.com/blogs/2014/02/04/step-by-step-how-to-bring-jax-rs-and-osgi-together/) - Step by Step: How to bring JAX-RS and OSGi together.

## Contributing

TBD.

## Versioning

TBD.

## Authors

* **Achilleas Achilleos** - *Initial work* - [achilleas979](https://github.com/echilleas979)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details

## Publications
```1. Achilleas Achilleos, Kyriaki Georgiou, Christos Markides, Andreas Constantinides and George Papadopoulos, "Adaptive Runtime Middleware: Everything as a Service",	9th International Conference on Computational Collective Intelligence, (ICCI 2017), Springer.

## Acknowledgments

TBD.
