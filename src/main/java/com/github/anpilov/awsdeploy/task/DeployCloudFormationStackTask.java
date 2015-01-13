package com.github.anpilov.awsdeploy.task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.github.anpilov.awsdeploy.aws.AmazonApiFactory;
import com.github.anpilov.awsdeploy.aws.S3Uploader;
import com.github.anpilov.awsdeploy.extension.AwsDeployExtension;
import com.github.anpilov.awsdeploy.extension.CloudFormationExtension;
import com.github.anpilov.awsdeploy.extension.S3LocationExtension;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployCloudFormationStackTask extends DefaultTask {

    private static final Logger logger = LoggerFactory.getLogger(DeployCloudFormationStackTask.class);

    @TaskAction
    public void deployStack() {
        Project project = getProject();

        CloudFormationExtension cloudFormationExtension = AwsDeployExtension.get(project).getCloudFormation();
        String stackName = cloudFormationExtension.getStackName();
        File templateFile = cloudFormationExtension.getTemplateFile();

        logger.debug("Deploying CloudFormation stack {} from file {}", stackName, templateFile.getAbsolutePath());

        String templateUrl = null;
        S3LocationExtension s3Location = cloudFormationExtension.getS3();
        if (s3Location != null) {
            S3Uploader s3Uploader = new S3Uploader(project);
            templateUrl = s3Uploader.upload(s3Location, templateFile);
        }

        AmazonCloudFormation cloudFormation = AmazonApiFactory.createCloudFormation(project);
        Stack existingStack = getExistingStack(cloudFormation, stackName);
        if (existingStack == null) {
            logger.debug("Stack {} doesn't exist, creating", stackName);
            createStack(cloudFormation, stackName, templateUrl, templateFile);
        } else {
            logger.debug("Stack {} exists, updating", stackName);
            updateStack(cloudFormation, stackName, templateUrl, templateFile);
        }

        logger.debug("CloudFormation stack {} deployed");
    }

    private void createStack(AmazonCloudFormation cloudFormation, String stackName, String templateUrl, File templateFile) {
        CreateStackRequest request = new CreateStackRequest().withStackName(stackName);
        if (templateUrl != null) {
            request.withTemplateURL(templateUrl);
        } else {
            request.withTemplateBody(readTemplateBody(templateFile));
        }
        cloudFormation.createStack(request);
    }

    private void updateStack(AmazonCloudFormation cloudFormation, String stackName, String templateUrl, File templateFile) {
        UpdateStackRequest request = new UpdateStackRequest().withStackName(stackName);
        if (templateUrl != null) {
            request.withTemplateURL(templateUrl);
        } else {
            request.withTemplateBody(readTemplateBody(templateFile));
        }
        cloudFormation.updateStack(request);
    }

    private String readTemplateBody(File templateFile) {
        try {
            return Files.toString(templateFile, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stack getExistingStack(AmazonCloudFormation cloudFormation, String stackName) {
        List<Stack> existingStacks = cloudFormation.describeStacks(new DescribeStacksRequest().withStackName(stackName)).getStacks();
        Preconditions.checkArgument(existingStacks.size() <= 1, "Multiple stacks (%s) found by stackName=%s", existingStacks.size(), stackName);
        return existingStacks.isEmpty() ? null : existingStacks.get(0);
    }
}
