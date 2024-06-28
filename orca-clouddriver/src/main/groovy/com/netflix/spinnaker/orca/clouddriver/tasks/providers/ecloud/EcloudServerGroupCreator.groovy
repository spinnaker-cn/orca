package com.netflix.spinnaker.orca.clouddriver.tasks.providers.ecloud

import com.netflix.spinnaker.orca.clouddriver.tasks.servergroup.ServerGroupCreator
import com.netflix.spinnaker.orca.kato.tasks.DeploymentDetailsAware
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Slf4j
@Component
class EcloudServerGroupCreator implements ServerGroupCreator, DeploymentDetailsAware {

  boolean katoResultExpected = true

  String cloudProvider = "ecloud"

  @Override
  List<Map> getOperations(Stage stage) {
    def ops = []
    def createServerGroupOp = createServerGroupOperation(stage)
    ops.add([(ServerGroupCreator.OPERATION): createServerGroupOp])
    return ops
  }

  @Override
  Optional<String> getHealthProviderName() {
    return Optional.of("Ecloud")
  }

  def createServerGroupOperation(Stage stage) {
    def operation = [:]
    def context = stage.context

    if (context.containsKey("cluster")) {
      operation.putAll(context.cluster as Map)
    } else {
      operation.putAll(context)
    }

    def targetRegion = operation.region ?: (operation.availabilityZones as Map<String, Object>).keySet()[0]
    withImageFromPrecedingStage(stage, targetRegion, cloudProvider) {
      operation.amiName = operation.amiName ?: it.amiName
      operation.imageId = operation.imageId ?: it.imageId
    }

    withImageFromDeploymentDetails(stage, targetRegion, cloudProvider) {
      operation.amiName = operation.amiName ?: it.amiName
      operation.imageId = operation.imageId ?: it.imageId
    }

    if (!operation.imageId) {
      def deploymentDetails = (context.deploymentDetails ?: []) as List<Map>
      if (deploymentDetails) {
        // Because docker image ids are not region or cloud provider specific
        operation.imageId = deploymentDetails[0]?.imageId
      }
    }

    if (!operation.containsKey("application")) {
      throw new IllegalStateException("No application could be found in ${context}.")
    }

    if (!operation.containsKey("stack")) {
      throw new IllegalStateException("No stack could be found in ${context}.")
    }

    if (!operation.containsKey("accountName")) {
      throw new IllegalStateException("No accountName could be found in ${context}.")
    }

//    if (!operation.containsKey("credentials")) {
//      throw new IllegalStateException("No credentials could be found in ${context}.")
//    }

    if (!operation.containsKey("region")) {
      throw new IllegalStateException("No region could be found in ${context}.")
    }

    if (!operation.containsKey("imageId")) {
      throw new IllegalStateException("No imageId could be found in ${context}.")
    }

    if (!operation.containsKey("instanceType")) {
      throw new IllegalStateException("No instanceType could be found in ${context}.")
    }

    if (!operation.containsKey("maxSize")) {
      throw new IllegalStateException("No maxSize could be found in ${context}.")
    }

    if (!operation.containsKey("minSize")) {
      throw new IllegalStateException("No minSize could be found in ${context}.")
    }

    if (!operation.containsKey("desiredCapacity")) {
      throw new IllegalStateException("No desiredCapacity could be found in ${context}.")
    }

    log.info("Deploying Ecloud ${operation.imageId} to ${operation.region}")

    return operation
  }

}
