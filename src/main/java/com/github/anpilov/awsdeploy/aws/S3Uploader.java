package com.github.anpilov.awsdeploy.aws;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.github.anpilov.awsdeploy.extension.S3LocationExtension;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.gradle.api.Project;

public class S3Uploader {

    private static final long PART_SIZE_MB = 5 * 1024 * 1024;

    private ResourceResolver resourceResolver;
    private AmazonS3 s3;

    public S3Uploader(Project project) {
        this.resourceResolver = new ResourceResolver(project);
        this.s3 = AmazonApiFactory.createS3(project);
    }

    public String upload(S3LocationExtension extension, File file) {
        S3Location location = toS3Location(extension, file);
        return upload(location, file);
    }

    public S3Location toS3Location(S3LocationExtension extension, File file) {
        String bucketName = getBucketName(extension);
        String keyPrefix = getKeyPrefix(extension);
        String keyName = keyPrefix + file.getName();
        return new S3Location(bucketName, keyName);
    }

    private String getBucketName(S3LocationExtension s3Location) {
        Preconditions.checkState(!Strings.isNullOrEmpty(s3Location.getBucketId()) || !Strings.isNullOrEmpty(s3Location.getBucketName()),
                "need to specify either s3BucketName or s3BucketId for s3 upload");

        String bucketId = s3Location.getBucketId();
        return !Strings.isNullOrEmpty(bucketId)
                ? resourceResolver.resolveResourceId(bucketId)
                : s3Location.getBucketName();
    }

    private String getKeyPrefix(S3LocationExtension s3Location) {
        String keyPrefix = s3Location.getKeyPrefix();
        if (!Strings.isNullOrEmpty(keyPrefix)) {
            if (!keyPrefix.endsWith("/")) {
                keyPrefix += "/";
            }
        } else {
            keyPrefix = "";
        }
        return keyPrefix;
    }

    public String upload(S3Location location, File file) {
        String uploadId = s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(location.getS3Bucket(), location.getS3Key())).getUploadId();

        try {
            List<PartETag> partETags = uploadFile(uploadId, location.getS3Bucket(), location.getS3Key(), file);
            CompleteMultipartUploadResult result = s3.completeMultipartUpload(new CompleteMultipartUploadRequest(location.getS3Bucket(), location.getS3Key(), uploadId, partETags));
            return result.getLocation();
        } catch (Exception e) {
            s3.abortMultipartUpload(new AbortMultipartUploadRequest(location.getS3Bucket(), location.getS3Key(), uploadId));
            throw e;
        }
    }

    private List<PartETag> uploadFile(String uploadId, String bucketName, String keyName, File file) {
        long contentLength = file.length();
        long filePosition = 0;
        List<PartETag> partETags = new ArrayList<>();
        for (int i = 1; filePosition < contentLength; i++) {
            long partSize = Math.min(PART_SIZE_MB, (contentLength - filePosition));

            UploadPartResult uploadResult = s3.uploadPart(new UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(keyName)
                    .withUploadId(uploadId)
                    .withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(file)
                    .withPartSize(partSize)
            );

            partETags.add(uploadResult.getPartETag());
            filePosition += partSize;
        }
        return partETags;
    }
}
