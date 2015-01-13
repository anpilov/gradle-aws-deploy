package com.github.anpilov.awsdeploy.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.github.anpilov.awsdeploy.extension.AwsDeployExtension;
import org.gradle.api.Project;

public class AmazonApiFactory {

    public static AmazonCloudFormation createCloudFormation(Project project) {
        AwsDeployExtension awsDeployExtension = AwsDeployExtension.get(project);
        AmazonCloudFormation cloudFormation = new AmazonCloudFormationClient(getCredentialsProviderChain(awsDeployExtension));
        cloudFormation.setRegion(awsDeployExtension.getRegion());
        return cloudFormation;
    }

    public static AmazonS3 createS3(Project project) {
        AwsDeployExtension awsDeployExtension = AwsDeployExtension.get(project);
        AmazonS3 s3 = new AmazonS3Client(getCredentialsProviderChain(awsDeployExtension));
        s3.setRegion(awsDeployExtension.getRegion());
        return s3;
    }

    public static AWSElasticBeanstalk createElasticBeanstalk(Project project) {
        AwsDeployExtension awsDeployExtension = AwsDeployExtension.get(project);
        AWSElasticBeanstalk elasticBeanstalk = new AWSElasticBeanstalkClient(getCredentialsProviderChain(awsDeployExtension));
        elasticBeanstalk.setRegion(awsDeployExtension.getRegion());
        return elasticBeanstalk;
    }

    private static AWSCredentialsProviderChain getCredentialsProviderChain(AwsDeployExtension awsDeployExtension) {
        AWSCredentials credentials = awsDeployExtension.getCredentials();
        if (credentials == null) {
            return new DefaultAWSCredentialsProviderChain();
        }
        return new AWSCredentialsProviderChain(new StaticCredentialsProvider(credentials));
    }

}
