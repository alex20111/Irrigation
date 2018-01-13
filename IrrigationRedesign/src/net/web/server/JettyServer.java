package net.web.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import home.exception.LoggerException;
 
public class JettyServer {
	
	private static Log log = LogFactory.getLog(JettyServer.class);
	
	public static final String cfgFile 	= "/server/WebConfig.ini"; 
	
	public static String HTTP_PORT 		= "HTTP-PORT";
	public static String HTTPS_PORT 	= "HTTPS-PORT";
	public static String KEY_STORE_PATH = "KEY-STORE-PATH";
	public static String KEY_STORE_PASS = "KEY-STORE-PASS";
	public static String WEB_APP_TEMP 	= "WEB-APP-TEMP";
	public static String WEB_CONTEXT 	= "WEB-CONTEXT";
	public static String LOGGER_LEVEL 	= "LOG-LEVEL";
	public static String SERVER_USER 	= "SERVER-USER";
	public static String SERVER_PASS 	= "SERVER-PASS";
	public static String LOGIN			= "FORCE-LOGIN";
	public static String EMAIL_KEY	 	= "EMAILKEY";
	
	
	
	private static Server server;
	private static HttpConfiguration http_config;	
	
	public static final String warFileName = "/server/WebServer.war";	
	
	/**
	 * @param args
	 * @throws LoggerException 
	 */

	public static void main(String[] args) throws LoggerException 
	{	
		try{			

			System.out.println("Starting server.");
			System.out.println("All log outputs from the screen will go into log file");
			//init logging 
		//	logging();

			//get server base dir
			System.setProperty("base.dir", new File("").getAbsolutePath());

			File warFile = new File(warFileName);

			//load config file
			File confFile = new File(cfgFile);

			if (!confFile.exists()){
				System.err.printf("Configuration " + cfgFile + " not found.\nThe " + cfgFile + " must be under the same directory that you are executing the jar.");
				System.exit(0);
			}		

			PropertiesConfiguration  confProp = new PropertiesConfiguration(confFile);

			ServerConst.sslUser = confProp.getString(SERVER_USER);
			ServerConst.sslPass  = confProp.getString(SERVER_PASS);

			// Setup Thread pool
			QueuedThreadPool threadPool = new QueuedThreadPool();
			threadPool.setMaxThreads(200);		

			// Create a basic jetty server object .
			server = new Server(threadPool);

			server.addBean(new ScheduledExecutorScheduler());
			// Extra options
			server.setDumpAfterStart(false);
			server.setDumpBeforeStop(false);
			server.setStopAtShutdown(true);
			server.setStopTimeout(10000);	

			if (confProp.getString(HTTP_PORT) == null || confProp.getString(HTTP_PORT).length() == 0 ){
				System.out.println("Please enter a port number into the configuration file");
				System.exit(0);
			}
			try{
				addHttp(server, Integer.valueOf(confProp.getString(HTTP_PORT)));
			}catch(NumberFormatException nfx){
				System.out.println("Please enter a numeric port number");
				System.exit(0);				
			}

			if (confProp.getString(KEY_STORE_PATH) != null && confProp.getString(HTTPS_PORT) != null){
				if(confProp.getString(HTTPS_PORT).length() == 0){
					System.out.println("Please enter a port number for the HTTPS connection");
					System.exit(0);
				}
				if(confProp.getString(KEY_STORE_PATH).length() == 0){
					System.out.println("Please enter a Keystore name or name and path");
					System.exit(0);
				}
				if(confProp.getString(KEY_STORE_PASS) == null || confProp.getString(KEY_STORE_PASS).length() == 0){
					System.out.println("Please enter a Keystore password");
					System.exit(0);
				}
				
				ServerConst.sslKeyFile = confProp.getString(KEY_STORE_PATH);
				ServerConst.sslKeyPass = confProp.getString(KEY_STORE_PASS);
				
				try{
					addSSL(server, Integer.valueOf(confProp.getString(HTTPS_PORT)), confProp.getString(KEY_STORE_PATH), confProp.getString(KEY_STORE_PASS));
				}catch(NumberFormatException nfx)
				{
					System.out.println("Please enter a valid HTTPS PORT.");
					System.exit(0);
				}
			}

			// Setup JMX
			MBeanContainer mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
			server.addBean(mbContainer);

			// The WebAppContext is the entity that controls the environment in which a web application lives and
			// breathes. In this example the context path is being set to "/" so it is suitable for serving root context
			// requests and then we see it setting the location of the war. A whole host of other configurations are
			// available, ranging from configuring to support annotation scanning in the webapp
			WebAppContext webapp = new WebAppContext();

			if (confProp.getString(WEB_CONTEXT) == null || confProp.getString(WEB_CONTEXT).length() == 0){
				System.out.println("Please enter a web context");
				System.exit(0);
			}
			webapp.setContextPath(confProp.getString(WEB_CONTEXT) );
			webapp.setPersistTempDirectory(true);
			if (!warFile.exists())
			{
				throw new RuntimeException( "Unable to find WAR File: "	+ warFile.getAbsolutePath() );
			}

			webapp.setWar( warFile.getAbsolutePath() );	

			if (confProp.getProperty(WEB_APP_TEMP) != null && confProp.getString(WEB_APP_TEMP).length() > 0){
				webapp.setTempDirectory(new File(confProp.getString(WEB_APP_TEMP)));
			}

			// This webapp will use jsps and jstl. We need to enable the
			// AnnotationConfiguration in order to correctly set up the jsp container
			Configuration.ClassList classlist = Configuration.ClassList.setServerDefault( server );
			classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
					"org.eclipse.jetty.annotations.AnnotationConfiguration" );

			// Set the ContainerIncludeJarPattern so that jetty examines these
			// container-path jars for tlds, web-fragments etc.
			// If you omit the jar that contains the jstl .tlds, the jsp engine will scan for them instead.
			webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
					".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$" );

			// A WebAppContext is a ContextHandler as well so it needs to be set to the server so it is aware of where to
			// send the appropriate requests.

			ServletContextHandler context0 = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context0.setContextPath("/server");
			context0.addServlet(new ServletHolder(new ServerServlet(server, webapp)),"/options");

			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[] { context0, webapp });

			server.setHandler(contexts);

			LowResourceMonitor lowResourcesMonitor=new LowResourceMonitor(server);
			lowResourcesMonitor.setPeriod(1000);
			lowResourcesMonitor.setLowResourcesIdleTimeout(200);
			lowResourcesMonitor.setMonitorThreads(true);
			lowResourcesMonitor.setMaxConnections(0);
			lowResourcesMonitor.setMaxMemory(0);
			lowResourcesMonitor.setMaxLowResourcesTime(5000);
			server.addBean(lowResourcesMonitor);		

			if (confProp.getString(LOGGER_LEVEL) != null && confProp.getString(LOGGER_LEVEL).length() > 0){
				System.setProperty("root.logger.level", confProp.getString(LOGGER_LEVEL).trim());
			}else{
				System.setProperty("root.logger.level", "DEBUG");
			}
			
			if (confProp.getString(LOGIN) != null && confProp.getString(LOGIN).length() > 0)
			{
				String login = confProp.getString(LOGIN).trim();
				if ("true".equalsIgnoreCase(login)){
					System.setProperty("web.login.options", "mustLogin");
				}else if ("false".equalsIgnoreCase(login)){
					System.setProperty("web.login.options", "loginAfter");
				}else{
					System.setProperty("web.login.options", "mustLogin");
				}
			}
			
			if (confProp.getString(EMAIL_KEY) != null && confProp.getString(EMAIL_KEY).length() > 0){
				System.setProperty("email_key", confProp.getString(EMAIL_KEY));
			}
			

			confProp.clear();
			
			// Start things up! By using the server.join() the server thread will join with the current thread.
			server.start();
			server.join();

		}catch(Exception ex){
			log.error("Error in JettyServer  " , ex);
		}
	}

	private static void addHttp(Server server, int port){
		
		http_config = new HttpConfiguration();
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
		
		ServerConnector http = new ServerConnector(server,
                new HttpConnectionFactory(http_config));
        http.setPort(port);
        http.setIdleTimeout(30000);
                        
        server.addConnector(http);
	}

	private static void addSSL( Server server, int port, String keyStorePath, String keyPass) throws FileNotFoundException{
		
		String keystorePath = System.getProperty("example.keystore", keyStorePath);
		File keystoreFile = new File(keystorePath);
		if (!keystoreFile.exists())
		{
			throw new FileNotFoundException("Keystore does not exist: " + keystoreFile.getAbsolutePath());
		}
		// SSL Context Factory for HTTPS
		// SSL requires a certificate so we configure a factory for ssl contents
		// with information pointing to what keystore the ssl connection needs
		// to know about. Much more configuration is available the ssl context,
		// including things like choosing the particular certificate out of a
		// keystore to be used.
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
		sslContextFactory.setKeyStorePassword(keyPass);
		sslContextFactory.setKeyManagerPassword(keyPass);
		sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
		sslContextFactory.setExcludeProtocols("SSL",
                "SSLv2", "SSLv3", "SSLv2Hello");

		// HTTPS Configuration
		// On this HttpConfiguration object we add a
		// SecureRequestCustomizer which is how a new connector is able to
		// resolve the https connection before handing control over to the Jetty
		// Server.
		HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

		// HTTPS connector
		// We create a second ServerConnector, passing in the http configuration
		// we just made along with the previously created ssl context factory.
		// Next we set the port and a longer idle timeout.
		ServerConnector https = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
				new HttpConnectionFactory(https_config));
		https.setPort(port);
		https.setIdleTimeout(500000);

		// Set the connector
		server.addConnector( https);	     
	}
	
	private static void logging() throws IOException{
		RolloverFileOutputStream os = new RolloverFileOutputStream("/server/yyyy_mm_dd_Server.log", false);
		

		//We are creating a print stream based on our RolloverFileOutputStream 
		PrintStream logStream = new PrintStream(os); 
		//We are redirecting system out and system error to our print stream. 
		System.setOut(logStream); 
		System.setErr(logStream);    
		
	}
}
