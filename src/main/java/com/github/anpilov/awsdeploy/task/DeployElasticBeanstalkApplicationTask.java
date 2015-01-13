package com.github.anpilov.awsdeploy.task;

import java.io.File;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;
import com.github.anpilov.awsdeploy.aws.AmazonApiFactory;
import com.github.anpilov.awsdeploy.aws.ResourceResolver;
import com.github.anpilov.awsdeploy.aws.S3Uploader;
import com.github.anpilov.awsdeploy.extension.AwsDeployExtension;
import com.github.anpilov.awsdeploy.extension.ElasticBeanstalkExtension;
import com.google.common.base.Preconditions;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployElasticBeanstalkApplicationTask extends DefaultTask {

    private static final Logger logger = LoggerFactory.getLogger(DeployElasticBeanstalkApplicationTask.class);

    @TaskAction
    public void deployApp() {
        Project project = getProject();

        ElasticBeanstalkExtension ebExtension = AwsDeployExtension.get(project).getElasticBeanstalk();
        AWSElasticBeanstalk elasticBeanstalk = AmazonApiFactory.createElasticBeanstalk(project);

        Preconditions.checkNotNull(ebExtension, "elasticBeanstalk config not specified");
        Preconditions.checkNotNull(ebExtension.getVersionLabel(), "elasticBeanstalk.versionLabel not specified");
        Preconditions.checkNotNull(ebExtension.getS3(), "elasticBeanstalk.s3 config not specified");

        ResourceResolver resourceResolver = new ResourceResolver(project);
        String applicationName = resourceResolver.resolveIdOrName(ebExtension.getApplicationId(), ebExtension.getApplicationName(), "elasticBeanstalk.application");
        String environmentName = resourceResolver.resolveIdOrName(ebExtension.getEnvironmentId(), ebExtension.getEnvironmentName(), "elasticBeanstalk.environment");

        S3Uploader s3Uploader = new S3Uploader(project);
        File versionArtifact = ebExtension.getVersionArtifact();
        S3Location s3Location = s3Uploader.toS3Location(ebExtension.getS3(), versionArtifact);

        logger.info("Uploading artifact {} to s3 location {}/{}", versionArtifact.getAbsolutePath(), s3Location.getS3Bucket(), s3Location.getS3Key());
        s3Uploader.upload(s3Location, versionArtifact);

        logger.info("Creating application version {}", ebExtension.getVersionLabel());
        elasticBeanstalk.createApplicationVersion(new CreateApplicationVersionRequest()
                .withApplicationName(applicationName)
                .withVersionLabel(ebExtension.getVersionLabel())
                .withSourceBundle(s3Location)
        );

        // TODO cleanup old versions?

        logger.info("Updating environment {}", environmentName);
        elasticBeanstalk.updateEnvironment(new UpdateEnvironmentRequest()
                .withEnvironmentName(environmentName)
                .withVersionLabel(ebExtension.getVersionLabel())
        );
    }
}
