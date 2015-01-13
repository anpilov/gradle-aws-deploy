package com.github.anpilov.awsdeploy;

import com.github.anpilov.awsdeploy.extension.AwsDeployExtension;
import com.github.anpilov.awsdeploy.task.DeployCloudFormationStackTask;
import com.github.anpilov.awsdeploy.task.DeployElasticBeanstalkApplicationTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GradleAwsDeployPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().add("awsDeploy", new AwsDeployExtension(project));
        project.getTasks().create("deployStack", DeployCloudFormationStackTask.class);
        project.getTasks().create("deployApp", DeployElasticBeanstalkApplicationTask.class);
    }
}
