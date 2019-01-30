package com.ngahr.UrlJobScheduling.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ngahr.UrlJobScheduling.model.JobDescriptor;
import com.ngahr.UrlJobScheduling.service.JobService;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
@RestController
@RequestMapping("/scheduling")
public class JobController {
	
	private final JobService jobService;

	  public JobController(JobService jobService) {
	    this.jobService = jobService;
	  }
	  @PostMapping(path = "/groups/{group}/jobs")
		public ResponseEntity<JobDescriptor> createJob(@PathVariable String group, @RequestBody JobDescriptor descriptor) {
			return new ResponseEntity<>(jobService.createJob(group, descriptor), CREATED);
		}
	  @GetMapping(path = "/groups/{group}/jobs/{name}")
		public ResponseEntity<JobDescriptor> findJob(@PathVariable String group, @PathVariable String name) {
			return jobService.findJob(group, name)
					.map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		}
	  
	  @GetMapping(path = "/groups/{group}/jobs")
		public ResponseEntity<List<JobDescriptor>> findAllJobsInGroup(@PathVariable String group) {
			return jobService.findAllJobInGroup(group)
					.map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		}
	  @GetMapping(path = "/jobs")
		public ResponseEntity<List<JobDescriptor>> findAllJobs() {
			return jobService.findAllJob()
					.map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		}

		@PutMapping(path = "/groups/{group}/jobs/{name}")
		public ResponseEntity<Void> updateJob(@PathVariable String group, @PathVariable String name, @RequestBody JobDescriptor descriptor) {
			jobService.updateJob(group, name, descriptor);
			return ResponseEntity.noContent().build();
		}

		@DeleteMapping(path = "/groups/{group}/jobs/{name}")
		public ResponseEntity<Void> deleteJob(@PathVariable String group, @PathVariable String name) {
			jobService.deleteJob(group, name);
			return ResponseEntity.noContent().build();
		}

		@PatchMapping(path = "/groups/{group}/jobs/{name}/pause")
		public ResponseEntity<Void> pauseJob(@PathVariable String group, @PathVariable String name) {
			jobService.pauseJob(group, name);
			return ResponseEntity.noContent().build();
		}

		@PatchMapping(path = "/groups/{group}/jobs/{name}/resume")
		public ResponseEntity<Void> resumeJob(@PathVariable String group, @PathVariable String name) {
			jobService.resumeJob(group, name);
			return ResponseEntity.noContent().build();
		}

}
