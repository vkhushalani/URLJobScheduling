package com.ngahr.UrlJobScheduling.controller;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

@RestController
@RequestMapping("/scheduling")
public class FrontEndAppController {
	Logger logger = LoggerFactory.getLogger(FrontEndAppController.class);
	@GetMapping(path = "/destinations")
	public ResponseEntity <?> getAllDestinations() throws NamingException {
		String account = System.getenv("HC_ACCOUNT");
//		List<String> destinations = new ArrayList<String>();
		Context ctx = new InitialContext();
		ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
		Map<String, DestinationConfiguration> destConfigurations = configuration.getConfigurations(account);
		
		List<SimpleEntry<String, String>> destinations = new ArrayList<SimpleEntry<String, String>>();
		for (Map.Entry<String, DestinationConfiguration> entry : destConfigurations.entrySet()) {
			DestinationConfiguration destConfig = entry.getValue();
			if(destConfig.getProperty("Type").equalsIgnoreCase("HTTP") &&
					destConfig.getProperty("ProxyType").equalsIgnoreCase("Internet")
					&& (destConfig.getProperty("Authentication").equalsIgnoreCase("NoAuthentication")|| 
							destConfig.getProperty("Authentication").equalsIgnoreCase("BasicAuthentication")||
							destConfig.getProperty("Authentication").equalsIgnoreCase("AppToAppSSO"))
					&& (destConfig.getProperty("javaSchedule") != null && destConfig.getProperty("javaSchedule").equalsIgnoreCase("true")))
			{destinations.add(new SimpleEntry<String,String>("destName",entry.getKey()));}
//			for(Map.Entry<String, String> destProp : destConfig.getAllProperties().entrySet())
//			{
//				logger.debug(destProp.getKey());
//				logger.debug(destProp.getValue());
//			}
			 
			
		}	
		return ResponseEntity.ok(destinations);
	}

}
