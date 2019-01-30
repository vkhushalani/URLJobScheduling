package com.ngahr.UrlJobScheduling.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import static org.quartz.JobKey.jobKey;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ngahr.UrlJobScheduling.model.JobDescriptor;

@Service
@Transactional
public class JobService {
	
Logger logger = LoggerFactory.getLogger(JobService.class);
	private final Scheduler scheduler;
	public JobService(Scheduler scheduler) {
	    this.scheduler = scheduler;
	  }
	public JobDescriptor createJob(String group, JobDescriptor descriptor) {
		descriptor.setGroup(group);
		JobDetail jobDetail = descriptor.buildJobDetail();
		Set<Trigger> triggersForJob = descriptor.buildTriggers();
		try {
			scheduler.scheduleJob(jobDetail, triggersForJob, false);
		} catch (SchedulerException e) {
			logger.error("Could not save job with key - {} due to error - {}", jobDetail.getKey(), e.getLocalizedMessage());
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}
		return descriptor;
	}
	@Transactional(readOnly = true)
	public Optional<JobDescriptor> findJob(String group, String name) {
		// @formatter:off
		try {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey(name, group));
			if(Objects.nonNull(jobDetail))
				return Optional.of(
						JobDescriptor.buildDescriptor(scheduler,jobDetail, 
								scheduler.getTriggersOfJob(jobKey(name, group))));
		} catch (SchedulerException e) {
			logger.error("Could not find job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
		}
		// @formatter:on
		logger.warn("Could not find job with key - {}.{}", group, name);
		return Optional.empty();
	}
	
	@Transactional(readOnly = true)
	public Optional<List<JobDescriptor>> findAllJobInGroup(String group) {
		// @formatter:off
		try {
			List<JobDescriptor> jobDecriptorList = new ArrayList<JobDescriptor>();
		
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			if(Objects.nonNull(jobDetail))
			{jobDecriptorList.add(JobDescriptor.buildDescriptor(scheduler,jobDetail, 
					scheduler.getTriggersOfJob(jobKey)));}
				
				}
			return Optional.of(
					jobDecriptorList
					);
		} catch (SchedulerException e) {
			logger.error("Could not find job with Group - {}.{} due to error - {}", group, e.getLocalizedMessage());
		}
		// @formatter:on
		logger.warn("Could not find job with Group - {}.{}", group);
		return Optional.empty();
	}
	
	public void updateJob(String group, String name, JobDescriptor descriptor) {
		try {
			JobDetail oldJobDetail = scheduler.getJobDetail(jobKey(name, group));
			if(Objects.nonNull(oldJobDetail)) {
				JobDataMap jobDataMap = oldJobDetail.getJobDataMap();
				jobDataMap.put("url", descriptor.getUrl());
				JobBuilder jb = oldJobDetail.getJobBuilder();
				JobDetail newJobDetail = jb.usingJobData(jobDataMap).storeDurably().build();
				scheduler.addJob(newJobDetail, true);
				return;
			}
			logger.warn("Could not find job with key - {}.{} to update", group, name);
		} catch (SchedulerException e) {
			logger.error("Could not find job with key - {}.{} to update due to error - {}", group, name, e.getLocalizedMessage());
		}
	}
	
	public void deleteJob(String group, String name) {
		try {
			scheduler.deleteJob(jobKey(name, group));
			logger.info("Deleted job with key - {}.{}", group, name);
		} catch (SchedulerException e) {
			logger.error("Could not delete job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
		}
	}
	
	public void pauseJob(String group, String name) {
		try {
			scheduler.pauseJob(jobKey(name, group));
		} catch (SchedulerException e) {
			logger.error("Could not pause job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
		}
	}
	
	public void resumeJob(String group, String name) {
		try {
			scheduler.resumeJob(jobKey(name, group));
			
		} catch (SchedulerException e) {
			logger.error("Could not resume job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
		}
	}
	public Optional<List<JobDescriptor>> findAllJob() {
		// @formatter:off
		try {
			List<JobDescriptor> jobDecriptorList = new ArrayList<JobDescriptor>();
			for (String groupName : scheduler.getJobGroupNames()) {
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			if(Objects.nonNull(jobDetail))
			{jobDecriptorList.add(JobDescriptor.buildDescriptor(scheduler,jobDetail, 
					scheduler.getTriggersOfJob(jobKey)));}
				
				}}
			return Optional.of(
					jobDecriptorList
					);
		} catch (SchedulerException e) {
			logger.error("Could not find job - {}.{} due to error - {}", e.getLocalizedMessage());
		}
		// @formatter:on
		logger.warn("Could not find job - {}.{}");
		return Optional.empty();
	}
}
