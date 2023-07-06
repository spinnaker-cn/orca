package com.netflix.spinnaker.orca.clouddriver.pipeline.providers.ctyun


import com.netflix.spinnaker.orca.clouddriver.tasks.MonitorKatoTask
import com.netflix.spinnaker.orca.clouddriver.tasks.servergroup.ServerGroupCacheForceRefreshTask
import com.netflix.spinnaker.orca.pipeline.StageDefinitionBuilder
import com.netflix.spinnaker.orca.pipeline.TaskNode
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class UpsertCtyunScheduledActionsStage implements StageDefinitionBuilder {
  @Override
  void taskGraph(Stage stage, TaskNode.Builder builder) {
    builder
      .withTask("upsertCtyunScheduledActions", UpsertCtyunScheduledActionsTask)
      .withTask("monitorUpsert", MonitorKatoTask)
      .withTask("forceCacheRefresh", ServerGroupCacheForceRefreshTask)
  }
}
