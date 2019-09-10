package armcore;


import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import osgi_annotations.interfaces.ClassDescription;
import osgi_annotations.interfaces.MethodDescription;
import osgi_annotations.interfaces.ParameterDescription;


@Path("/")
public class ConnectionControl {
	protected Class<?> serviceClass = null;
	private ArrayList<String> OsgiList = new ArrayList<String>();
	private ArrayList<String> registeredServices = new ArrayList<String>();

	public ConnectionControl() {
		//System.out.println("Initializing ConnectorControl");
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String availableOsgi() {

		StringBuilder stringBuilder = new StringBuilder("Available OSGI:\n\n");
		int bundleSize = OsgiList.size();

		if (bundleSize == 0) {
			stringBuilder.append("No available OSGI's.");
		}

		for (int i = 0; i < bundleSize; i++) {
			String curPluginFullName = OsgiList.get(i);
			Class<?> pluginClass;
			try {
				pluginClass = Class.forName(curPluginFullName);

				ClassDescription osgiDescriptor = pluginClass.getAnnotation(ClassDescription.class);
				String osgiDescriptorStr = osgiDescriptor == null ? "OSGI description is empty by the developer."
						: osgiDescriptor.value();
				if (osgiDescriptorStr != null) {
					stringBuilder.append((i + 1) + ". OSGI Description: " + osgiDescriptorStr + "\n");
				}

				Path classPath = pluginClass.getAnnotation(Path.class);
				String classPathStr = classPath == null ? "/" : classPath.value();
				stringBuilder.append("\tPath for available methods: /services" + classPathStr + "\n\n");

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return stringBuilder.toString();
	}

	// Reads the definition xml file from jar file. Plugin names and
	// required bundle names are added only once
	protected void readXMLDefinitionFiles(ArrayList<String> pluginFullNames, ArrayList<String> pluginFileNames, ArrayList<String> reqBundleDefinitions,
			File folder) {

		String definitionFileName = Config.definitionXMLFileName;

		if (folder == null || folder.listFiles() == null) {
			//System.out.println("XML Definitions Folder not found.");
			return;
		}

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {
				try {
					// Read OSGI jar file and the XML file
					JarFile jarFile = new JarFile(fileEntry);
					ZipEntry xmlFileZipEntry = jarFile.getEntry(definitionFileName);
					if (xmlFileZipEntry == null) {
						//System.out.println("Info: Plugin does not contain definition xml file. Plugin: " + fileEntry.getName());
						continue;
					}

					InputStream xmlFileInputStream = jarFile.getInputStream(xmlFileZipEntry);

					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document document = db.parse(xmlFileInputStream);

					if (document == null) {
						continue;
					}

					if (document.getElementsByTagName("fullname").getLength() == 1) {
						Element fullNameElement = (Element) document.getElementsByTagName("fullname").item(0);
						String fullName = fullNameElement.getTextContent().trim();
						if (fullName != null && fullName.isEmpty() == false) {
							String fullNameTrim = fullName.trim();
							// Check that the plugin is not in the list already
							if (pluginFullNames.contains(fullNameTrim) == false) {
								pluginFullNames.add(fullNameTrim);

								if (document.getElementsByTagName("filename").getLength() == 1) {
									Element fileNameElement = (Element) document.getElementsByTagName("filename")
											.item(0);
									String fileName = fileNameElement.getTextContent().trim();
									if (fileName != null && fileName.isEmpty() == false) {
										String fileNameTrim = fileName.trim();
										pluginFileNames.add(fileNameTrim);

									} else {
										pluginFileNames.add("");
									}
								} else {
									pluginFileNames.add("");
								}

							} else {
								// IF osgi is already in the list, then required
								// bundles are included
								continue;
							}
						}

					}

					NodeList requiredBundleList = document.getElementsByTagName("require-bundle");
					if (requiredBundleList.getLength() == 1) {
						Element bundleListElement = (Element) document.getElementsByTagName("require-bundle").item(0);
						NodeList bundleList = bundleListElement != null
								? bundleListElement.getElementsByTagName("bundle") : null;

								if (bundleList != null) {
									String txtContentTrim;
									for (int i = 0; i < bundleList.getLength(); i++) {
										Element bundleElement = (Element) bundleList.item(i);
										String txtContent = bundleElement == null ? null : bundleElement.getTextContent();
										if (txtContent != null && txtContent.isEmpty() == false) {
											// Check that the plugin is not in the list
											// already
											txtContentTrim = txtContent.trim();
											if (reqBundleDefinitions.contains(txtContentTrim) == false) {
												reqBundleDefinitions.add(txtContentTrim);
											}
										}
									}
								}
					}

					// Close the Jar file
					jarFile.close();

				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} // is file
		}
	}

	protected boolean runSourceFileOsgi(ArrayList<String> restPluginFullNames, ArrayList<String> javaCodeList,
			String sourceFilePath) throws Exception {

		if (restPluginFullNames.size() != javaCodeList.size()) {
			throw new Exception(
					"Method: runSourceFileOsgi, Error: The list size of the rest plugins is not equal to the code list.");
		}

		for (int i = 0; i < restPluginFullNames.size(); i++) {
			String curPluginFullName = restPluginFullNames.get(i);
			String curJavaSourceCode = javaCodeList.get(i);

			InlineCompiler.main(Config.currentProjectPath, curJavaSourceCode, curPluginFullName);
		}

		return true;
	}

	protected void loadPlugins(final BundleContext context, List<Bundle> loadedPlugins) {
		File osgiFolder = new File(Config.osgiFolderFilePath);

		if (!osgiFolder.exists() || !osgiFolder.isDirectory()) {
			return;
		}

		try {
			for (final File osgiBundle : osgiFolder.listFiles()) {
				if (osgiBundle.isFile()) {
					try {
						// If Class not found, then try to install and start the
						// service
						Bundle bundle = new ConnectionControl().installAndStartBundle(osgiBundle.getAbsolutePath());

						//System.out.println("Installed NOW: " + osgiBundle.getAbsolutePath() + " (id#"
								//+ bundle.getBundleId() + ")" + " (Symbolic Name#"
								//+ bundle.getSymbolicName() + ")");
						
						addJarToClassPath(osgiBundle.getName());
						
						
					} catch (Exception ex) {
						//System.out.println("Installed Already: " + osgiBundle.getAbsolutePath());
					}

				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates manifest of the bundle with all the required bundles defined in
	 * the XML files for the bundles that will be used for creating REST url's
	 * 
	 * @param reqBundleDefinitions
	 *            List with all the required bundle definitions to be added in
	 *            the Require-Bundle manifest (if not already in the manifest)
	 * @throws IOException
	 */
	protected void updateManifest(ArrayList<String> reqBundleDefinitions) throws IOException {
		String manifestFilePath = Config.currentProjectPath + "META-INF/MANIFEST.MF";
		FileWriter manifestWriter = null;
		File sourceFile = new File(manifestFilePath); // create the source file

		//System.out.println("Definition file path: " + manifestFilePath);
		try {
			Scanner manifestScanner = createFileReaderAndDirs(manifestFilePath, sourceFile);

			// Return from method if scanner, writer or required bundles
			// definitions is empty or none
			if (manifestScanner == null || reqBundleDefinitions == null || reqBundleDefinitions.size() == 0) {
				return;
			}

			StringBuilder newManifestBuilder = new StringBuilder();
			String newRequiredBundleStr = "";

			// Check if manifest contains tag for Require-Bundle
			// Append all require bundles in newRequiredBundleStr, and rest in
			// newManifestBuilder
			boolean reqBundleTagExists = false;
			while (manifestScanner.hasNext()) {
				final String lineFromFile = manifestScanner.nextLine();
				if (lineFromFile.contains("Require-Bundle: ")) {
					reqBundleTagExists = true;
					newRequiredBundleStr = lineFromFile + "\n";

					// Append to newRequiredBundleStr all required bundles
					while (manifestScanner.hasNext()) {
						String newLineFromFile = manifestScanner.nextLine();
						if (newLineFromFile.contains(": ") == false && newLineFromFile.equals("\n") == false) {
							newRequiredBundleStr += (newLineFromFile + "\n");
						} else {
							newManifestBuilder.append(newLineFromFile + "\n");

							// Add all the rest of the line of manifest
							while (manifestScanner.hasNext()) {
								newLineFromFile = manifestScanner.nextLine();
								newManifestBuilder.append(newLineFromFile + "\n");
							}
							break;
						}
					}
					break;
				} else {
					newManifestBuilder.append(lineFromFile + "\n");
				}
			}

			// Add Required Bundle tag in manifest file, if does not exist
			if (reqBundleTagExists == false) {
				newRequiredBundleStr = ("Require-Bundle: ");
			} else {
				// Remove last new line added
				newRequiredBundleStr = newRequiredBundleStr.substring(0, newRequiredBundleStr.length() - 1);
			}

			// Add all required bundles in manifest
			String curBundleReq;
			for (int bundlePos = 0; bundlePos < reqBundleDefinitions.size(); bundlePos++) {
				curBundleReq = reqBundleDefinitions.get(bundlePos);
				// Write the generated class in the file, if not already in
				// manifest
				if (newRequiredBundleStr.indexOf(curBundleReq) == -1) {
					if (reqBundleTagExists == true) {
						newRequiredBundleStr += (",\n " + curBundleReq + ",\n ");
						reqBundleTagExists = false;
					} else {
						newRequiredBundleStr += (curBundleReq + ",\n ");
					}
				}
			}

			// Remove the last new line and empty character
			if (newRequiredBundleStr.endsWith(",\n ")) {
				newRequiredBundleStr = newRequiredBundleStr.substring(0, newRequiredBundleStr.length() - 3);
			}

			// Add new string with required bundles in the list
			newManifestBuilder.append(newRequiredBundleStr + "\n");

			// Get a file writer to write back in the manifest file
			manifestWriter = createFileWriterAndDirs(manifestFilePath, false, sourceFile);
			manifestWriter.write(newManifestBuilder.toString());

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// When writer is not null, close the file
			if (manifestWriter != null) {
				manifestWriter.close();
			}
		}
	}

	/**
	 * Creates a class file with the below methods: a) A REST method is created
	 * that will list all the method calls available in the created class with
	 * pattern <path>/Plugin<pluginId>,
	 * e.g.http://localhost:9050/services/Plugin0 b) A REST method for each
	 * available method of the bundle pattern <path>/Plugin<pluginId>/
	 * <methodName>/<listWithParameters>, i.e.
	 * http://localhost:9050/services/Plugin0/helloWorld/88 c) For each method
	 * (from point b), a second REST method is created with the definition of
	 * the method with pattern <path>/Plugin<pluginId>/<methodName>/def, i.e.
	 * http://localhost:9050/services/Plugin0/helloWorld/def
	 * 
	 * @param packageName
	 *            Package name of the classes that will be generated for calling
	 *            the bundles.
	 * @param pluginFullNames
	 *            List with all the plugin (bundle) full names that will create
	 *            the class files for the rest calls and for retrieving the
	 *            available methods for the each bundle. One class file will be
	 *            created for each item in the list.
	 * @param javaCodeList
	 *            List with the code created for each class.
	 * @return List with the full names of the classes created.
	 * @throws IOException
	 */
	protected ArrayList<String> createRestFiles(String packageName, ArrayList<String> pluginFullNames, ArrayList<String> pluginFileNames,
			ArrayList<String> restPluginsJavaCodeList) throws IOException {

		ArrayList<String> restPluginsFullname = null;
		try{
			restPluginsFullname = new ArrayList<String>();

			for (int pluginPos = 0; pluginPos < pluginFullNames.size(); pluginPos++) {
				FileWriter writer = null;
				try {
					
					ServiceReference<?>[] serv_refs = Activator.getContext().getAllServiceReferences(pluginFullNames.get(pluginPos),null);
					//System.out.println("Service Reference createRestFiles: " + serv_refs[0].getBundle().getSymbolicName());
					
					
					
					if (!registeredServices.contains(serv_refs[0].getBundle().getSymbolicName())){
						
						
						registeredServices.add(serv_refs[0].getBundle().getSymbolicName());	
				
					String curPluginFullName = pluginFullNames.get(pluginPos);
					String curPluginFileName = pluginFileNames.get(pluginPos);
					String[] curPluginSplittedName = curPluginFullName.split("\\.");
					String curPluginName = curPluginSplittedName.length >= 2 ? curPluginSplittedName[1] : curPluginFullName;

					String filePathName = Config.currentProjectPath + "src/" + packageName + "/" + curPluginName + ".java";
					File sourceFile = new File(filePathName); // create the source
					// file

					//System.out.println("Java file path: " + filePathName);

					writer = createFileWriterAndDirs(filePathName, false, sourceFile);

					// Get the class for the specified plugin name (full name)
					Class<?> pluginClass;
					try {
						pluginClass = Activator.getContext().getBundle().loadClass(curPluginFullName);
					} catch (Exception ex) {
						// If Class not found, then try to install and start the
						// service
						//System.out.println("Bundles path: " + Config.osgiFolderFilePath + curPluginFileName);
						Bundle bundle = installAndStartBundle(Config.osgiFolderFilePath
								+ curPluginFileName);
						pluginClass = bundle.loadClass(curPluginFullName);
					}

					StringBuilder classStr = new StringBuilder();
					classStr.append("package " + packageName + ";\n");
					classStr.append("import armcore.ConnectionControl;\n");
					classStr.append("import osgi_annotations.interfaces.*;\n");

					// KG 08/04/2017
					classStr.append("import com.google.gson.Gson;\n");

					//SS 2/1/2018

					classStr.append("import org.glassfish.jersey.media.sse.*;\n");

					classStr.append("import javax.ws.rs.*;\n");
					classStr.append("import javax.ws.rs.core.MediaType;\n\n");

					classStr.append("@Path(\"/" + curPluginName + "\")\n");

					ClassDescription pluginDescriptor = pluginClass.getAnnotation(ClassDescription.class);

					String pluginDescriptorStr = pluginDescriptor == null ? "OSGI description is empty by the developer."
							: pluginDescriptor.value().replaceAll("\n", " ");
					classStr.append("@ClassDescription(value=\"" + pluginDescriptorStr + "\")\n");

					// Class begin
					classStr.append("public class " + curPluginName + " {\n");

					// Add reference to the actual service
					classStr.append("// Get the Service Reference for the OSGi\n");
					classStr.append("private " + curPluginFullName + " service = new " + curPluginFullName + "();\n\n");

					// Retrieve all the plugin methods. Create one rest call for
					// each one
					Method[] methods = pluginClass.getDeclaredMethods();
					ConnectionControl.getClassMethodsAndCreate(classStr, methods, curPluginName, pluginClass, true);

					// Class end
					classStr.append("}\n");

					// Write the generated class in the file
					writer.write(classStr.toString());

					// Add the new plugin full name to the list. We will need to
					// register these services later
					String pluginFullName = packageName + "." + curPluginName;
					restPluginsFullname.add(pluginFullName);

					this.OsgiList.add(pluginFullName);

					restPluginsJavaCodeList.add(classStr.toString());

				}
				else{
					continue;
				}
					
				} catch (Exception ex) {
					ex.printStackTrace();

				} finally {
					// When writer is not null, close the file
					if (writer != null) {
						writer.close();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return restPluginsFullname;
	}

	public static void getClassMethodsAndCreate(StringBuilder classStr, Method[] methods, String className,
			Class<?> pluginClass, boolean createRestMethod) {
		try{
			// If rest Methods will be create, create a method that will print all
			// the methods
			if (createRestMethod) {
				classStr.append("@GET\n");
				classStr.append(
						"@MethodDescription(value=\"This method lists all the available methods for this osgi along with the calls for the definitions of the methods.\")\n");
				classStr.append("public String classAvailableMethods() {\n");
				classStr.append("StringBuilder classStr = new StringBuilder();\n");

				classStr.append("ConnectionControl.getClassMethodsAndCreate(classStr, " + className
						+ ".class.getDeclaredMethods(), \"" + className + "\", " + className + ".class, false);\n");

				classStr.append("return classStr.toString();\n");

				classStr.append("}\n\n");
			} else {
				// Add the descriptor for the class, if exists
				ClassDescription osgiDescriptor = pluginClass.getAnnotation(ClassDescription.class);

				String osgiDescriptorStr = osgiDescriptor == null ? "OSGI description is empty by the developer."
						: osgiDescriptor.value();
				if (osgiDescriptorStr != null) {
					classStr.append("OSGI Description: " + osgiDescriptorStr + "\n\n");
				}
			}



			for (int i = 0; i < methods.length; i++) {
				String methodName = methods[i].getName();

				// Check if method is protected or public. We will give
				// access only to public and protected methods
				int modifier = methods[i].getModifiers();
				boolean isAccessibleMethod = Modifier.isPublic(modifier);
				if (isAccessibleMethod) {
					Method curMethod = methods[i];
					Class<?>[] parameterTypes = curMethod.getParameterTypes();
					Class<?> returnType = curMethod.getReturnType();

					if (createRestMethod) {
						createRestMethod(classStr, methodName, curMethod, parameterTypes, returnType);
					} else {
						getMethodDetails(classStr, methodName, i, pluginClass);
					}
				}

				//System.out.println("Invoke method: " + methodName + ", Accessible Method: " + isAccessibleMethod);
			}

			//IWorkspace workspace = ResourcesPlugin.getWorkspace();
			//URI myUri = URI.create(Activator.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "src/");
			//IFile[] iFile = workspace.getRoot().findFilesForLocationURI(myUri);
			// iFile[0].refreshLocal(IResource.DEPTH_ZERO, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getMethodDetails(StringBuilder classStr, String methodName, int methodPosition,
			Class<?> curClass) {
		try {

			classStr.append("Method Details\n---------------------------------------------\n");

			Method[] curMethods = curClass.getMethods();
			if (curMethods.length > methodPosition) {
				Method curMethod = curMethods[methodPosition];

				Class<?>[] parameterTypes = curMethod.getParameterTypes();
				Class<?> returnType = curMethod.getReturnType();

				Path classPath = curMethod.getAnnotation(Path.class);
				String classPathStr = classPath == null ? "/" : classPath.value();
				classStr.append("Path: /services/" + curClass.getSimpleName() + classPathStr + "\n");

				classStr.append("Name: " + methodName + "\n");

				MethodDescription methodDescriptor = curMethod.getAnnotation(MethodDescription.class);

				String methodDescriptorStr = methodDescriptor == null ? "Description is empty by the developer."
						: methodDescriptor.value();
				classStr.append("Description: " + methodDescriptorStr + "\n");

				// Append parameter types and descriptions (if exist)
				if (parameterTypes.length == 0) {
					classStr.append("No Parameters defined.\n");
				} else {
					classStr.append("Parameter Type(s):\n");

					Annotation[][] parameterAnnotations = curMethod.getParameterAnnotations();

					for (int paramI = 0; paramI < parameterTypes.length; paramI++) {
						classStr.append("\t- " + parameterTypes[paramI].getName() + "\n");

						// Add the descriptor for the parameter, if exists
						ParameterDescription parameterDescriptor = getMethodParameterDescription(parameterAnnotations,
								paramI);
						String parameterDescriptorStr = parameterDescriptor == null ? null
								: parameterDescriptor.value();
						if (parameterDescriptorStr != null) {
							classStr.append("\t\tDescription: " + parameterDescriptorStr + "\n");
						}
					}
				}

				classStr.append("Return Type: " + returnType.getName() + "\n\n\n");
			} else {
				classStr.append("Error in retrieving method details.\n\n\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Bundle installAndStartBundle(String filePathAndBundleName) throws BundleException {
		BundleContext bundleContext = Activator.getContext();
		Bundle bundle = bundleContext.installBundle("file:/" + filePathAndBundleName);
		bundle.start();
		return bundle;
	}

	protected void registerRestPlugins(BundleContext context, ArrayList<String> restPluginFullNames)
			throws MalformedURLException {

		
		// Register services to have the url's accessible
		for (int i = 0; i < restPluginFullNames.size(); i++) {
			try {
				// Load the class from the classloader by name

				// http://keheliya.blogspot.com.cy/2013/02/use-of-dynamicimport-package-in-osgi.html
				// Required 'DynamicImport-Package: *'
				// 'DynamicImport-Package: *' is a trick used by bundles
				// to allow importing client packages which are not
				// known during bundle build time, in addition to its
				// own dependencies, to prevent ClassNotFoundException
				// issues.
				ServiceReference<?>[] serv_refs = Activator.getContext().getAllServiceReferences(restPluginFullNames.get(i),null);
				
				if (serv_refs == null){
				//System.out.println("Service Reference REST PLUGINS: " + restPluginFullNames.get(i));
				Class<?> loadedClass = context.getBundle().loadClass(restPluginFullNames.get(i));
				Object instance = loadedClass.newInstance();
				Activator.getContext().registerService(loadedClass.getName(), instance, null);
				}
				else{
					continue;
				}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvalidSyntaxException e) {
				e.printStackTrace();
			}
		}
		newBundlesListener();
	}
	

	private static FileWriter createFileWriterAndDirs(String locationStr, boolean appendToFile, File sourceFile)
			throws IOException {
		if (sourceFile == null) {
			sourceFile = new File(locationStr);
		}

		File parentDir = sourceFile.getParentFile();
		if (!parentDir.exists())
			parentDir.mkdirs(); // create parent dir and ancestors if
		// necessary

		FileWriter writer = new FileWriter(sourceFile, appendToFile);
		return writer;
	}

	private static Scanner createFileReaderAndDirs(String locationStr, File sourceFile) throws IOException {
		if (sourceFile == null) {
			sourceFile = new File(locationStr);
		}

		File parentDir = sourceFile.getParentFile();
		if (!parentDir.exists()) {
			return null;
		}

		Scanner scanner = new Scanner(sourceFile);
		return scanner;
	}

	private static void createRestMethod(StringBuilder classStr, String methodName, Method curMethod,
			Class<?>[] parameterTypes, Class<?> returnType) {
		// Calling Method
		// --------------------------------------------------------------------------------------------

		if (returnType.isAssignableFrom(org.glassfish.jersey.media.sse.EventOutput.class)){
			createRestMethodInvoke2(classStr, methodName, curMethod, parameterTypes, returnType);
		}
		////need to putan extra else if to make connector sse work as expected
		else{
			createRestMethodInvoke(classStr, methodName, curMethod, parameterTypes, returnType);
		}

		// Definition Method
		// --------------------------------------------------------------------------------------------
		createRestMethodDefinition(classStr, methodName, curMethod, parameterTypes, returnType);
	}

	private static void createRestMethodInvoke2(StringBuilder classStr, String methodName, Method curMethod,
			Class<?>[] parameterTypes, Class<?> returnType) {
		classStr.append("@GET\n");
		// Create path and parameters
		classStr.append("@Path(\"/" + methodName);
		for (int paramI = 0; paramI < parameterTypes.length; paramI++) {
			classStr.append("/{param" + paramI + "}");
		}
		classStr.append("\")\n");

		classStr.append("@Produces(SseFeature.SERVER_SENT_EVENTS)\n");

		// Add Descriptor for the definition method
		MethodDescription methodDescriptor = curMethod.getAnnotation(MethodDescription.class);
		String methodDescriptorStr = methodDescriptor == null ? "Description is empty by the developer."
				: methodDescriptor.value();
		if (methodDescriptorStr != null) {
			classStr.append("@MethodDescription(value=\"Call to the osgi method: " + methodName + ". Details: "
					+ methodDescriptorStr + "\")\n");
		}

		// Create definition of method with parameters
		classStr.append("public EventOutput " + methodName + "(");

		Annotation[][] parameterAnnotations = curMethod.getParameterAnnotations();

		if (parameterTypes.length > 0) {
			for (int paramI = 0; paramI < parameterTypes.length - 1; paramI++) {
				Class<?> curParameter = parameterTypes[paramI];

				// Check if there is an annotation for the current parameter
				ParameterDescription parameterDescriptor = getMethodParameterDescription(parameterAnnotations, paramI);
				String parameterDescriptorStr = parameterDescriptor == null ? null : parameterDescriptor.value();
				if (parameterDescriptorStr != null) {
					classStr.append("@ParameterDescription(value=\"" + parameterDescriptorStr + "\") ");
				}

				classStr.append(
						"@PathParam(\"param" + paramI + "\")" + curParameter.getName() + " param" + paramI + ", ");
			}

			int lastParamI = parameterTypes.length - 1;
			// Check if there is an annotation for the current parameter
			ParameterDescription parameterDescriptor = getMethodParameterDescription(parameterAnnotations, lastParamI);
			String parameterDescriptorStr = parameterDescriptor == null ? null : parameterDescriptor.value();
			if (parameterDescriptorStr != null) {
				classStr.append("@ParameterDescription(value=\"" + parameterDescriptorStr + "\") ");
			}

			classStr.append("@PathParam(\"param" + lastParamI + "\")" + parameterTypes[lastParamI].getName() + " param"
					+ lastParamI);
		}
		classStr.append(") {\n");

		// Append parameter types
		if (parameterTypes.length == 0) {
			classStr.append(returnType.getName() + " result = service." + methodName + "();");
			classStr.append("return result;");
		} else {

			classStr.append(returnType.getName() + " result = service." + methodName + "(");
			for (int paramI = 0; paramI < parameterTypes.length - 1; paramI++) {
				classStr.append("param" + paramI + ", ");
			}
			classStr.append("param" + (parameterTypes.length - 1) + ");\n");

			classStr.append("Gson gson = new Gson(); String json = gson.toJson(result); return json;");
		}

		classStr.append("}\n\n");
	}

	private static void createRestMethodInvoke(StringBuilder classStr, String methodName, Method curMethod,
			Class<?>[] parameterTypes, Class<?> returnType) {
		classStr.append("@GET\n");
		// Create path and parameters
		classStr.append("@Path(\"/" + methodName);
		for (int paramI = 0; paramI < parameterTypes.length; paramI++) {
			classStr.append("/{param" + paramI + "}");
		}
		classStr.append("\")\n");

		classStr.append("@Produces(MediaType.APPLICATION_JSON)\n");

		// Add Descriptor for the definition method
		MethodDescription methodDescriptor = curMethod.getAnnotation(MethodDescription.class);
		String methodDescriptorStr = methodDescriptor == null ? "Description is empty by the developer."
				: methodDescriptor.value();
		if (methodDescriptorStr != null) {
			classStr.append("@MethodDescription(value=\"Call to the osgi method: " + methodName + ". Details: "
					+ methodDescriptorStr + "\")\n");
		}

		// Create definition of method with parameters
		classStr.append("public String " + methodName + "(");

		Annotation[][] parameterAnnotations = curMethod.getParameterAnnotations();

		if (parameterTypes.length > 0) {
			for (int paramI = 0; paramI < parameterTypes.length - 1; paramI++) {
				Class<?> curParameter = parameterTypes[paramI];

				// Check if there is an annotation for the current parameter
				ParameterDescription parameterDescriptor = getMethodParameterDescription(parameterAnnotations, paramI);
				String parameterDescriptorStr = parameterDescriptor == null ? null : parameterDescriptor.value();
				if (parameterDescriptorStr != null) {
					classStr.append("@ParameterDescription(value=\"" + parameterDescriptorStr + "\") ");
				}

				classStr.append(
						"@PathParam(\"param" + paramI + "\")" + curParameter.getName() + " param" + paramI + ", ");
			}

			int lastParamI = parameterTypes.length - 1;
			// Check if there is an annotation for the current parameter
			ParameterDescription parameterDescriptor = getMethodParameterDescription(parameterAnnotations, lastParamI);
			String parameterDescriptorStr = parameterDescriptor == null ? null : parameterDescriptor.value();
			if (parameterDescriptorStr != null) {
				classStr.append("@ParameterDescription(value=\"" + parameterDescriptorStr + "\") ");
			}

			classStr.append("@PathParam(\"param" + lastParamI + "\")" + parameterTypes[lastParamI].getName() + " param"
					+ lastParamI);
		}
		classStr.append(") {\n");

		// Append parameter types
		if (parameterTypes.length == 0) {
			classStr.append(returnType.getName() + " result = service." + methodName + "();");
			classStr.append("Gson gson = new Gson(); String json = gson.toJson(result); return json;");
		} else {

			classStr.append(returnType.getName() + " result = service." + methodName + "(");
			for (int paramI = 0; paramI < parameterTypes.length - 1; paramI++) {
				classStr.append("param" + paramI + ", ");
			}
			classStr.append("param" + (parameterTypes.length - 1) + ");\n");

			classStr.append("Gson gson = new Gson(); String json = gson.toJson(result); return json;");
		}

		classStr.append("}\n\n");
	}

	private static ParameterDescription getMethodParameterDescription(Annotation[][] parameterAnnotations, int paramI) {
		// Check if there is an annotation for the current parameter
		if (parameterAnnotations.length > paramI && parameterAnnotations[paramI].length > 0) {
			// Loop over all annotations defined for this parameter to find the
			// one for ParameterDescription
			for (int i = 0; i < parameterAnnotations[paramI].length; i++) {
				if (parameterAnnotations[paramI][i].annotationType() == ParameterDescription.class) {
					ParameterDescription parameterDescriptor = (ParameterDescription) parameterAnnotations[paramI][i];
					return parameterDescriptor;
				}
			}
		}
		return null;
	}

	private static void createRestMethodDefinition(StringBuilder classStr, String methodName, Method curMethod,
			Class<?>[] parameterTypes, Class<?> returnType) {
		classStr.append("@GET\n");
		// Create path and parameters
		classStr.append("@Path(\"/" + methodName + "/def\")\n");

		// Add Descriptor for the definition method
		classStr.append("@MethodDescription(value=\"Retrieves the definition for osgi method: " + methodName + "\")\n");

		// Create definition of method with parameters
		classStr.append("public String " + methodName + "Def() {\n");

		classStr.append("StringBuilder definitionStrBuilder = new StringBuilder();\n");

		classStr.append("definitionStrBuilder.append(\"Method Name: " + methodName + "\");\n");

		MethodDescription methodDescriptor = curMethod.getAnnotation(MethodDescription.class);

		String methodDescriptorStr = methodDescriptor == null ? "Description is empty by the developer."
				: methodDescriptor.value();
		classStr.append("definitionStrBuilder.append(\"\\nDescription: " + methodDescriptorStr + "\");\n");

		// Append parameter types
		if (parameterTypes.length == 0) {
			classStr.append("definitionStrBuilder.append(\"\\nNo Parameters defined\\n\");\n");
		} else {
			classStr.append("definitionStrBuilder.append(\"\\nParameter Type(s): \\n\");\n");

			Annotation[][] parameterAnnotations = curMethod.getParameterAnnotations();

			for (int paramI = 0; paramI < parameterTypes.length; paramI++) {
				classStr.append("definitionStrBuilder.append(\"\t- " + parameterTypes[paramI].getName() + "\\n\");\n");

				// Add the descriptor for the parameter, if exists
				ParameterDescription parameterDescriptor = getMethodParameterDescription(parameterAnnotations, paramI);
				String parameterDescriptorStr = parameterDescriptor == null ? null : parameterDescriptor.value();
				if (parameterDescriptorStr != null) {
					classStr.append(
							"definitionStrBuilder.append(\"\t\tDescription: " + parameterDescriptorStr + "\\n\");\n");
				}

			}
		}

		classStr.append("definitionStrBuilder.append(\"Return Type: " + returnType.getName() + "\\n\");\n");

		classStr.append("return definitionStrBuilder.toString();\n");

		classStr.append("}\n\n");
	}
	
	
	public void newBundlesListener(){
		
		try {
			final WatchService watcher = FileSystems.getDefault().newWatchService();
			java.nio.file.Path dir = new File(Config.osgiFolderFilePath).toPath();
			WatchKey key = dir.register(watcher, ENTRY_MODIFY);
			

			new Thread() {
				public void run() {

					WatchKey keynew;
					for (;;) {
						
						
						// wait for key to be signaled
						// WatchKey key;
						try {
							keynew = watcher.take();
						} catch (InterruptedException x) {
							return;
						}
						List<WatchEvent<?>> events = keynew.pollEvents();
						
						
						for (WatchEvent<?> event: events) {
				
						WatchEvent.Kind<?> kind = event.kind();
							
							////System.out.println("Events List size: " + events.size());
							////System.out.println("Event details: " + event.count());
							////System.out.println("Event details: " + event.kind().toString());
							////System.out.println("Event details: " + event.context().toString());
							

							// This key is registered only
							// for ENTRY_CREATE events,
							// but an OVERFLOW event can
							// occur regardless if events
							// are lost or discarded.
							if (kind == OVERFLOW) {
								continue;
							}

							// The filename is the
							// context of the event.
							WatchEvent<java.nio.file.Path> ev = (WatchEvent<java.nio.file.Path>)event;
							java.nio.file.Path filename = ev.context();

							// Verify that the new
							//  file is a text file.
							// try {
							// Resolve the filename against the directory.
							// If the filename is "test" and the directory is "foo",
							// the resolved name is "test/foo".
							//java.nio.file.Path child = filename; 
							//if (!java.nio.file.Files.probeContentType(child).equals("text/plain")) {
							//   System.err.format("New file '%s'" +
							//       " is not a plain text file.%n", filename);
							//  continue;
							//}
							//} catch (IOException x) {
							//     System.err.println(x);
							//      continue;
							//  }

							// Email the file to the
							//  specified email alias.
							//System.out.format("New Plugin %s%n", filename);

							try {
								addJarToClassPath(filename.getFileName().toString());

							} catch (Exception e) {
								e.printStackTrace();
							}

							//loadPlugins(Activator.getContext(), new ArrayList<Bundle>());
							try {
								Activator.reload(Activator.getContext());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//Details left to reader....
						
						}

						// Reset the key -- this step is critical if you want to
						// receive further watch events.  If the key is no longer valid,
						// the directory is inaccessible so exit the loop.
						boolean valid = keynew.reset();
						if (!valid) {
							break;
						}
					}
				}
			}.start();

		} catch (IOException x) {
			System.err.println(x);
		}
		
	}
	
	public void addJarToClassPath(String filename){
		boolean jarAlreadyInClasspath = false;
			try {
				File inputFile = new File(Config.currentProjectPath + ".classpath");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(inputFile);
				doc.getDocumentElement().normalize();
				//System.out.print("Root element: ");
				//System.out.println(doc.getDocumentElement().getNodeName());
				   Element rootElement = doc.getDocumentElement();
				NodeList nList = rootElement.getElementsByTagName("classpathentry");
		         ////System.out.println("----------------------------plugins/"+filename);
		         
		         for (int temp = 0; temp < nList.getLength(); temp++) {
			            Node nNode = nList.item(temp);
			           // //System.out.println("Print Element : plugins/"+filename);
			          //  //System.out.println("XML Element : " + nNode.getAttributes().item(1).getTextContent());

			              
			               
			               if (nNode.getAttributes().item(0).getTextContent().equals("lib")&&
			            		   nNode.getAttributes().item(1).getTextContent().equals("plugins/"+filename)){
			            	   //System.out.println("Already added to classpath: " + filename);
			            	   jarAlreadyInClasspath = true;
			               }
			               
			            }
			    
		         if (jarAlreadyInClasspath == false){
		         Element classpathentry = doc.createElement("classpathentry");
				   	
	   				Attr attrType = doc.createAttribute("kind");
	   				attrType.setValue("lib");
	   				classpathentry.setAttributeNode(attrType);
	   	
	   				Attr attrType1 = doc.createAttribute("path");
	   				attrType1.setValue("plugins/"+filename);
	   				classpathentry.setAttributeNode(attrType1);
	   	
	   				rootElement.appendChild(classpathentry);
					// write the DOM object to the file
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
		
					DOMSource domSource = new DOMSource(doc);
		
					StreamResult streamResult = new StreamResult(new File(Config.currentProjectPath + ".classpath"));
		
					transformer.transform(domSource, streamResult);
		         }
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		
	}

}