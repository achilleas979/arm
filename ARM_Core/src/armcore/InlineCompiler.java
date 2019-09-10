package armcore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;


//http://stackoverflow.com/questions/21544446/how-do-you-dynamically-compile-and-load-external-java-classes

// http://kevinboone.net/classpath.html
// package reload after java files generation - 
// Eclipse- Window - Preferences - General - Workspace - Refresh using native hooks or polling.
public class InlineCompiler {
	
	/**
	public static String readFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}*/


	public static void main(String initPath, String sourceCode, String fullClassName) {
		//System.out.println("InlineCompiler");
		File curJavaFile = new File(initPath + "src/" + fullClassName.replaceAll("\\.", "/") + ".java");
		
			
		    
		    

		    
		   
		    JavaFileObject file = null;
			//try {
				file = new JavaSourceFromString(curJavaFile.getName().replaceAll(".java", ""), sourceCode);
			//} catch (IOException e1) {
				// TODO Auto-generated catch block
			//	e1.printStackTrace();
			//}
				try {
					
					// Create a File object on the root of the directory containing the class file
					// File classFile = new File("file:/" + initPath + "bin/");
					File classFile = new File("file:/" + initPath + "/");
					    // Convert File to a URL
					    URL url = classFile.toURL();          // file:/c:/myclasses/
					    URL[] urls = new URL[]{url};
					    //System.out.println("url = " + url.toString());
					    // Create a new class loader with the directory
					    //ClassLoader cl = new URLClassLoader(urls);
					    URLClassLoader classLoader = URLClassLoader.newInstance(urls , ConnectionControl.class.getClassLoader());

					    // Load in the class; MyClass.class should be located in
					    // the directory file:/c:/myclasses/com/mycompany
					    Class<?> cls = classLoader.loadClass("armcore.ConnectionControl");
					} catch (MalformedURLException e) {
						System.out.print("MalformedURLException");
					} catch (ClassNotFoundException e) {
						System.out.print("ClassNotFoundException");
					}
				
		   
			System.setProperty("user.dir", initPath);
			//System.out.println("Working Directory = " + System.getProperty("user.dir"));
			   
			//System.out.println("initPath = " + initPath);
			//System.out.println("sourceCode = " + sourceCode);
			//System.out.println("fullClassName = " + fullClassName);
			
		    //String classpath = buildClassPath(initPath+"plugins/*", initPath+"lib/*", initPath+"*", initPath+"bin/");
		    String classpath = buildClassPath(initPath+"plugins/*", initPath+"lib/*", initPath+"*", initPath);
		    //System.out.println("Classpath: " + classpath);
		    //Iterable<String> options = Arrays.asList("-cp", classpath, "-d", initPath + "bin/");
		    Iterable<String> options = Arrays.asList("-cp", classpath, "-d", initPath);
		    //List<String> optionList = new ArrayList<String>();
			//optionList.add(classpath);
			//optionList.add("-d C:/Users/achilleas/Desktop/ARM/plugins/");
		    
		    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			//System.out.println("Compiler: " + compiler.toString());
			
		    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		    //System.out.println("diagnostics: " + diagnostics.toString());
		    
		    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		    //System.out.println("fileManager: " + fileManager.toString());

		    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
		    
		    CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

		    boolean success = task.call();
		    for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
		      //System.out.println(diagnostic.getCode());
		      //System.out.println(diagnostic.getKind());
		      //System.out.println(diagnostic.getPosition());
		      //System.out.println(diagnostic.getStartPosition());
		      //System.out.println(diagnostic.getEndPosition());
		      //System.out.println(diagnostic.getSource());
		      //System.out.println(diagnostic.getMessage(null));

		    }
		    //System.out.println("Success: " + success);

		    /*if (success) {
		      try {
		        Class.forName("armcore.SmartPhilipsHUELight").getDeclaredMethod("main", new Class[] { String[].class })
		            .invoke(null, new Object[] { null });
		      } catch (ClassNotFoundException e) {
		        System.err.println("Class not found: " + e);
		      } catch (NoSuchMethodException e) {
		        System.err.println("No such method: " + e);
		      } catch (IllegalAccessException e) {
		        System.err.println("Illegal access: " + e);
		      } catch (InvocationTargetException e) {
		        System.err.println("Invocation target: " + e);
		      }
		    }*/
		    

		//Process pro=Runtime.getRuntime().exec("javac -cp .;C:/Users/achilleas/Desktop/ARM/workspace/ARM_Core/Osgi_Annotations_1.0.0.0.jar;C:/Users/achilleas/Desktop/ARM/workspace/ARM_Core/lib/*;C:/Users/achilleas/Desktop/ARM/workspace/ARM_Core/plugins/SmartPhilipsHUELight_2.0.2.0.jar armcore/SmartPhilipsHUELight.java -d C:/Users/achilleas/Desktop/Eclipse/arm/ARM_Core_1.0.0.0/bin/",
			//		null, 
			//		new File("C:/Users/achilleas/Desktop/ARM/workspace/ARM_Core/src/armcore"));
			
			//javac -cp .;C:/Users/achilleas/Desktop/ARM/workspace/ARM_Core/Osgi_Annotations_1.0.0.0.jar;C:/Users/achilleas/Desktop/ARM/workspace/ARM_Core/lib/*;C:/Users/achilleas/Desktop/ARM/workspace/ARM_Core/plugins/SmartPhilipsHUELight_2.0.2.0.jar armcore/SmartPhilipsHUELight.java -d C:/Users/achilleas/Desktop/Eclipse/arm/ARM_Core_1.0.0.0/bin/
			
			// Compilation Requirements
			/*DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			
			// https://stackoverflow.com/questions/2543439/null-pointer-exception-while-using-java-compiler-api
			// the following line creates the NullPointerException -- see reason at the link above 
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

			//File compiledOutPath = new File(initPath + "/bin");
			
			File compiledOutPath = new File("C:/Users/achilleas/Desktop/Eclipse/arm/ARM_Core_1.0.0.0/bin/armcore");
			
			
			// This sets up the class path that the compiler will use.
			List<String> optionList = new ArrayList<String>();
			//optionList.add("-classpath");
			
			String classpath = buildClassPath("src/armcore/", "/*", "lib/*");
			
			//optionList.add(System.getProperty("java.class.path") + ";" + classpath);
			
			//optionList.addAll("-d", compiledOutPath.toString(),"-classpath", classpath);
			Iterable<String> options = Arrays.asList("-d", compiledOutPath.toString(),"-classpath", classpath);
			//String buildPathLib = buildClassPath(initPath + "lib/*");

			//String buildPathPluginJar = buildClassPath(Config.osgiFolderFilePath + "*");

			//optionList.add(System.getProperty("java.class.path") + ";" + buildPathLib // +
																						// buildPathSrc
			//		+ compiledOutPath.toString() + ";" + buildPathPluginJar);

			// Specify where to put the generated .class files
			//optionList.add("-d");
			//optionList.add(compiledOutPath.toString());

			
			Iterable<? extends JavaFileObject> compilationUnit = fileManager
					.getJavaFileObjectsFromStrings(Arrays.asList("armcore."+ curJavaFile.getName()));
			JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null,
					compilationUnit);
			
			// Compilation Requirements
			if (task.call() == false) { //If call fails, print error
				StringBuffer sb = new StringBuffer();
				for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
					System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(),
							diagnostic.getSource().toUri());

					sb.append("Diagno: ").append(diagnostic.getCode()).append(" ").append(diagnostic.getKind())
							.append(" ").append("Line: ").append(diagnostic.getLineNumber()).append(", col: ")
							.append(diagnostic.getColumnNumber()).append(" msg: ").append(diagnostic.getMessage(null))
							.append("\n");
				}
				System.out.format("Error complete: %s%n", sb.toString());
			}

			fileManager.close();
			
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Error task.call IO(): " + ex.getCause() + " " + ex.getMessage());
		} catch (NullPointerException ex){
			//ex.printStackTrace();
			System.out.println("JRE is used and not JDK");
		} **/
	}

	/**
	 * This function builds a classpath from the passed Strings
	 * 
	 * @param paths
	 *            classpath elements
	 * @return returns the complete classpath with wildcards expanded
	 */
	// http://stackoverflow.com/questions/22965975/how-to-set-up-classpath-of-javacompiler-to-multiple-jar-files-using-wildcard
	private static String buildClassPath(String... paths) {
		StringBuilder sb = new StringBuilder();
		for (String path : paths) {
			if (path.endsWith("*")) {
				path = path.substring(0, path.length() - 1);
				File pathFile = new File(path);
				if (pathFile != null && pathFile.listFiles() != null) {
					for (File file : pathFile.listFiles()) {
						if (file.isFile() && file.getName().endsWith(".jar")) {
							sb.append(path);
							sb.append(file.getName());
							sb.append(System.getProperty("path.separator"));
						} else if (file.isFile() && file.getName().endsWith(".java")
								|| file.isFile() && file.getName().endsWith(".class")) {
							sb.append(path);
							sb.append(file.getName());
							sb.append(System.getProperty("path.separator"));
						}
					}
				}
			} else {
				sb.append(path);
				sb.append(System.getProperty("path.separator"));
			}
		}
		return sb.toString();
	}

}
