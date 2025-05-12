package com.example.techbridge.domain.member.service;

import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public URL generateUploadUrl(String key, String contentType, Duration duration) {
        return s3Presigner.presignPutObject(p -> p
            .signatureDuration(duration)
            .putObjectRequest(o -> o
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
            )
        ).url();
    }

    public URL generateViewUrl(String key, Duration duration) {
        return s3Presigner.presignGetObject(p -> p
                .signatureDuration(duration)
                .getObjectRequest(o -> o
                    .bucket(bucket)
                    .key(key)
                ))
            .url();
    }
}
