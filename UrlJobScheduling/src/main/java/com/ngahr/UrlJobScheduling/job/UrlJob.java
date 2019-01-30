package com.ngahr.UrlJobScheduling.job;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.connectivity.api.authentication.AuthenticationHeader;
import com.sap.core.connectivity.api.authentication.AuthenticationHeaderProvider;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;



public class UrlJob implements Job {
	private static final String ON_PREMISE_PROXY = "OnPremise";

	Logger logger = LoggerFactory.getLogger(UrlJob.class);
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap map = context.getMergedJobDataMap();
		
			try {
				runUrl(map);
			} catch (NamingException | IOException e) {
				e.printStackTrace();
			}
		
		
	}

	private void runUrl(JobDataMap map) throws NamingException, IOException {
	
		String desturl = map.getString("url");
		logger.debug(desturl);
		HttpURLConnection urlConnection = null;
		Context ctx = new InitialContext();
		 ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
		 DestinationConfiguration destConfiguration = configuration.getConfiguration(desturl);
		 String value = destConfiguration.getProperty("URL");
         URL url = new URL(value);
         
         String proxyType = destConfiguration.getProperty("ProxyType");
         Proxy proxy = getProxy(proxyType);
         urlConnection = (HttpURLConnection) url.openConnection(proxy);
         
         // Insert the required header in the request for on-premise destinations
         injectHeader(urlConnection,destConfiguration);
         urlConnection.getResponseCode();
         logger.debug(urlConnection.getResponseCode()+"");
	}

	private void injectHeader(HttpURLConnection urlConnection, DestinationConfiguration destConfiguration) throws NamingException {
		
		if (ON_PREMISE_PROXY.equals(destConfiguration.getProperty("ProxyType"))) {
            // Insert header for on-premise connectivity with the consumer subaccount name
            urlConnection.setRequestProperty("SAP-Connectivity-ConsumerAccount", System.getenv("HC_ACCOUNT"));
        }
		if(destConfiguration.getProperty("Authentication").equalsIgnoreCase("BasicAuthentication"))
		{
			String userCredentials = destConfiguration.getProperty("User")+":"+destConfiguration.getProperty("Password");
			String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userCredentials.getBytes()); 
			urlConnection.setRequestProperty("Authorization", basicAuth);
		}
//		else if(destConfiguration.getProperty("Authentication").equalsIgnoreCase("AppToAppSSO"))
//		{
//			Context ctx = new InitialContext();
//			logger.debug("Inside App to App SSO");
//			logger.debug(destConfiguration.getProperty("URL"));
//			AuthenticationHeaderProvider authHeaderProvider = (AuthenticationHeaderProvider) ctx.lookup("java:comp/env/AuthHeaderProvider");
//			logger.debug("AfterContext");
//			AuthenticationHeader appToAppSSOHeader = authHeaderProvider.getApptoAppSSOHeader(destConfiguration.getProperty("URL"),destConfiguration);
//			logger.debug(appToAppSSOHeader.getName());
//			logger.debug(appToAppSSOHeader.getValue());
//			urlConnection.setRequestProperty(appToAppSSOHeader.getName(), appToAppSSOHeader.getValue());
//		}
		
	}

	private Proxy getProxy(String proxyType) {
		 String proxyHost = null;
	        int proxyPort;
	        
	        if (ON_PREMISE_PROXY.equals(proxyType)) {
	            // Get proxy for on-premise destinations
	            proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
	            proxyPort = Integer.parseInt(System.getenv("HC_OP_HTTP_PROXY_PORT"));
	        } else {
	            // Get proxy for internet destinations
	            proxyHost = System.getProperty("http.proxyHost");
	            proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
	        }
	        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
	}

}
