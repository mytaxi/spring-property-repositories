package com.mytaxi;

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.MavenTask;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.builders.trigger.BitbucketServerTrigger;
import com.atlassian.bamboo.specs.model.task.ScriptTaskProperties;
import com.atlassian.bamboo.specs.util.BambooServer;

@BambooSpec
public class PlanSpec
{

    private Project project()
    {
        return new Project()
            .key(new BambooKey("LIB"))
            .name("libraries");
    }


    public Plan createPlan()
    {
        return new Plan(project(), "PropertyRepositories", "PROP")
            .stages(new Stage("Default Stage")
                .jobs(new Job("Default Job", new BambooKey("JOB1"))
                    .tasks(
                        new VcsCheckoutTask()
                            .description("Checkout Default Repository")
                            .checkoutItems(new CheckoutItem().defaultRepository())
                            .cleanCheckout(true),
                        new MavenTask()
                            .description("verify")
                            .goal("clean verify -U")
                            .jdk("JDK 1.8")
                            .executableLabel("Maven 3")
                            .hasTests(true)
                            .useMavenReturnCode(true)
                        ,
                        new ScriptTask()
                            .description("Trigger successful build")
                            .interpreter(ScriptTaskProperties.Interpreter.BINSH_OR_CMDEXE)
                            .inlineBody(
                                "export BAMBOO_USER=mytaxi_deploy\nexport BAMBOO_PASS=myt\\!deploy1\n\nexport PROJECT_NAME=`echo ${bamboo.buildKey} | sed -e 's/-.*//g'`\nexport PLAN_NAME=CD\nexport STAGE_NAME=JOB1\n\nif [ \"${bamboo.repository.branch.name}\" = \"develop\" ]\nthen\n   echo \"start maven deploy\"\n   mvn deploy -U -Dprotoc.path=/usr/local/bin/protoc-2.5.0  -DskipTests\nfi\n\nif [ \"${bamboo.repository.branch.name}\" = \"master\" ]\nthen\n   echo \"start maven release on master\"\n   mvn release:prepare -B -Darguments=\"-Dprotoc.path=/usr/local/bin/protoc-2.5.0 -Dmaven.test.skip=true -Dmaven.javadoc.skip=true\"\n   mvn release:perform -Darguments=\"-Dprotoc.path=/usr/local/bin/protoc-2.5.0 -Dmaven.test.skip=true -Dmaven.javadoc.skip=true\"\nfi")
                    )))
            .linkedRepositories("property-repositories")
            .triggers(new BitbucketServerTrigger())
            .planBranchManagement(new PlanBranchManagement()
                .delete(new BranchCleanup())
                .notificationForCommitters());
    }


    public PlanPermissions planPermission(PlanIdentifier planIdentifier)
    {
        return new PlanPermissions(planIdentifier.getProjectKey(), planIdentifier.getPlanKey()).permissions(new Permissions()
            .userPermissions("j.schumacher", PermissionType.EDIT, PermissionType.BUILD, PermissionType.CLONE, PermissionType.VIEW, PermissionType.ADMIN)
            .groupPermissions("bamboo-admin", PermissionType.ADMIN)
            .loggedInUserPermissions(PermissionType.VIEW, PermissionType.BUILD, PermissionType.CLONE)
            .anonymousUserPermissionView());
    }


    public static void main(final String[] args) throws Exception
    {
        //By default credentials are read from the '.credentials' file.
        BambooServer bambooServer = new BambooServer("https://bamboo.intapps.it");

        Plan plan = new PlanSpec().createPlan();

        bambooServer.publish(plan);

        PlanPermissions planPermission = new PlanSpec().planPermission(plan.getIdentifier());

        bambooServer.publish(planPermission);
    }
}