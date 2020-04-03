package com.singdiary.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Component
@Getter @Setter
public class AwsCloudProperties {

    @NotEmpty
    @Value("${cloud-aws.credentials.accessKey}")
    private String accessKey;

    @NotEmpty
    @Value("${cloud-aws.credentials.secretKey}")
    private String secretKey;

    @NotEmpty
    @Value("${cloud-aws.s3.bucket}")
    private String bucket;

    @NotEmpty
    @Value("${cloud-aws.region.static}")
    private String region;

    @NotEmpty
    @Value("${cloud-aws.url}")
    private String url;
}
