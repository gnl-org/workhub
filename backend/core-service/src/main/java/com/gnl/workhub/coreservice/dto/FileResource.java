package com.gnl.workhub.coreservice.dto;

import org.springframework.core.io.Resource;

public record FileResource(Resource resource, String contentType, String filename) {}
