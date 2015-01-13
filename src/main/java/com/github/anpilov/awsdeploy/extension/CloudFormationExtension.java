package com.github.anpilov.awsdeploy.extension;

import java.io.File;

import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import org.gradle.api.Project;

public class CloudFormationExtension {

    private Project project;

    private String stackName;
    private File templateFile;
    private S3LocationExtension s3;

    public CloudFormationExtension(Project project) {
        this.project = project;
    }

    public String getStackName() {
        Preconditions.checkNotNull(stackName, "cloudFormation.stackName config missing");
        return stackName;
    }

    public File getTemplateFile() {
        Preconditions.checkNotNull(templateFile, "cloudFormation.templateFile config missing");
        Preconditions.checkState(templateFile.exists(), "cloudFormation.templateFile file not found");
        Preconditions.checkState(templateFile.canRead(), "cloudFormation.templateFile could not be read");
        return templateFile;
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
