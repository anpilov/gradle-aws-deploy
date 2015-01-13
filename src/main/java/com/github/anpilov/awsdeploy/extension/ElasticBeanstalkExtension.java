package com.github.anpilov.awsdeploy.extension;

import java.io.File;

import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import org.gradle.api.Project;

public class ElasticBeanstalkExtension {

    private Project project;

    private String versionLabel;
    private File versionArtifact;

    private String applicationId;
    private String applicationName;

    private String environmentId;
    private String environmentName;

    private S3LocationExtension s3;

    public ElasticBeanstalkExtension(Project project) {
        this.project = project;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public File getVersionArtifact() {
        Preconditions.checkNotNull(versionArtifact, "elasticBeanstalk.versionArtifact config missing");
        Preconditions.checkState(versionArtifact.exists(), "elasticBeanstalk.versionArtifact file not found");
        Preconditions.checkState(versionArtifact.canRead(), "elasticBeanstalk.versionArtifact could not be read");
        return versionArtifact;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public S3LocationExtension getS3() {
        return s3;
    }

    public void s3(Closure closure) {
        S3LocationExtension location = new S3LocationExtension();
        project.configure(location, closure);
        this.s3 = location;
    }
}
