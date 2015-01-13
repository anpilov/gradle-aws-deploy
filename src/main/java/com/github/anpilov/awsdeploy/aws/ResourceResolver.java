package com.github.anpilov.awsdeploy.aws;

import java.text.MessageFormat;
import java.util.List;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.github.anpilov.awsdeploy.extension.AwsDeployExtension;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceResolver {

    private static final Logger logger = LoggerFactory.getLogger(ResourceResolver.class);

    private AmazonCloudFormation cloudFormation;
    private String stack;

    public ResourceResolver(Project project) {
        this.cloudFormation = AmazonApiFactory.createCloudFormation(project);
        this.stack = AwsDeployExtension.get(project).getCloudFormation().getStackName();
    }

    public String resolveResourceId(String logicalId) {
        DescribeStackResourcesResult describeStackResourcesResult = cloudFormation.describeStackResources(new DescribeStackResourcesRequest()
                .withStackName(stack)
                .withLogicalResourceId(logicalId)
        );

        List<StackResource> resources = describeStackResourcesResult.getStackResources();
        Preconditions.checkState(resources.size() == 1, "CloudFormation resource not found. Stack=%s LogicalId=%s Results=%s", stack, logicalId, resources.size());
        String physicalResourceId = resources.get(0).getPhysicalResourceId();

        logger.debug("Resolved logicalId {} to physicalId {}", logicalId, physicalResourceId);
        return physicalResourceId;
    }

    public String resolveIdOrName(String id, String name, String descriptor) {
        if (!Strings.isNullOrEmpty(id)) {
            return resolveResourceId(id);
        }
        if (Strings.isNullOrEmpty(name)) {
            throw new RuntimeException(MessageFormat.format("Either {0}Id or {1}Name have to be specified", descriptor, descriptor));
        }
        return name;
    }
}
