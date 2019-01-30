package com.ngahr.UrlJobScheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;



@SpringBootApplication
public class Application extends SpringBootServletInitializer  {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
 
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
	public SchedulerFactoryBean schedulerFactory(ApplicationContext applicationContext) {
		SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
		AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		
		factoryBean.setJobFactory(jobFactory);
		factoryBean.setApplicationContextSchedulerContextKey("applicationContext");
		return factoryBean;
	}
}
