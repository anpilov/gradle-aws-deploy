package com.github.anpilov.awsdeploy.extension;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import org.gradle.api.Project;

public class AwsDeployExtension {

    private Project project;

    private String region;
    private CredentialsExtension credentials;
    private CloudFormationExtension cloudFormation;
    private ElasticBeanstalkExtension elasticBeanstalk;

    public static AwsDeployExtension get(Project project) {
        return project.getExtensions().getByType(AwsDeployExtension.class);
    }

    public AwsDeployExtension(Project project) {
        this.project = project;
    }

    public Region getRegion() {
        Preconditions.checkNotNull(region, "Region not specified, use aws.region property");
        return Region.getRegion(Regions.fromName(region));
    }

    public AWSCredentials getCredentials() {
        if (credentials != null) {
            return new BasicAWSCredentials(credentials.getAccessKey(), credentials.getSecretKey());
        }
        return null;
    }

    public CloudFormationExtension getCloudFormation() {
        return cloudFormation;
    }

    public ElasticBeanstalkExtension getElasticBeanstalk() {
        return elasticBeanstalk;
    }

    public void credentials(Closure closure) {
        CredentialsExtension credentials = new CredentialsExtension();
        project.configure(credentials, closure);
        this.credentials = credentials;
    }

    public void cloudFormation(Closure closure) {
        CloudFormationExtension cloudFormation = new CloudFormationExtension(project);
        project.configure(cloudFormation, closure);
        this.cloudFormation = cloudFormation;
    }

    public void elasticBeanstalk(Closure closure) {
        ElasticBeanstalkExtension elasticBeanstalk = new ElasticBeanstalkExtension(project);
        project.configure(elasticBeanstalk, closure);
        this.elasticBeanstalk = elasticBeanstalk;
    }
}
