package com.oronaminc.join.document.dto;

public record DocumentS3UploadResponse(String presignedUrl, String objectKey) { }
