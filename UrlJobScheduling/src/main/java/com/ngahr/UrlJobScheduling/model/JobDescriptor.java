package com.ngahr.UrlJobScheduling.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngahr.UrlJobScheduling.job.UrlJob;

import static org.quartz.JobBuilder.*;


public class JobDescriptor {
	
	private String name;
	private String group;
	private String url;
	private String state;
	@JsonProperty("triggers")
	private List<TriggerDescriptor> triggerDescriptors = new ArrayList<>();
	public JobDescriptor setName(final String name) {
		this.name = name;
		return this;
	}
	public JobDescriptor setState(final String state) {
		this.state = state;
		return this;
	}
	public JobDescriptor setGroup(final String group) {
		this.group = group;
		return this;
	}
	public JobDescriptor setUrl(final String url) {
		this.url = url;
		return this;
	}
	public JobDescriptor setTriggerDescriptors(final List<TriggerDescriptor> triggerDescriptors) {
		this.triggerDescriptors = triggerDescriptors;
		return this;
	}
	public String getGroup(){
		return group;
	}
	
	public String getName(){
		return name;
	}
	
	public String getUrl(){
		return url;
	}
	public String getState(){
		return state;
	}
	public List<TriggerDescriptor> getTriggerDescriptors(){
		return triggerDescriptors;
	}
	@JsonIgnore
	public Set<Trigger> buildTriggers() {
		Set<Trigger> triggers = new LinkedHashSet<>();
		for (TriggerDescriptor triggerDescriptor : triggerDescriptors) {
			triggers.add(triggerDescriptor.buildTrigger());
		}

		return triggers;
	}
	public JobDetail buildJobDetail() {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("url", url);
		return newJob(UrlJob.class)
                .withIdentity(name, group)
                .usingJobData(jobDataMap)
                .build();
	}
	
	public static JobDescriptor buildDescriptor(Scheduler scheduler ,JobDetail jobDetail, List<? extends Trigger> triggersOfJob) throws SchedulerException {
		// @formatter:off
		List<TriggerDescriptor> triggerDescriptors = new ArrayList<>();
		Boolean pause= false;
		for (Trigger trigger : triggersOfJob) {
		    triggerDescriptors.add(TriggerDescriptor.buildDescriptor(trigger));
		    TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
		    if (TriggerState.PAUSED.equals(triggerState)) {
		    	pause =  true;
	        }
		}
		
		return new JobDescriptor()
				.setName(jobDetail.getKey().getName())
				.setGroup(jobDetail.getKey().getGroup())
				.setUrl(jobDetail.getJobDataMap().getString("url"))
				.setState( pause ? "paused" : "active")
				.setTriggerDescriptors(triggerDescriptors);
		// @formatter:on
	}
}
