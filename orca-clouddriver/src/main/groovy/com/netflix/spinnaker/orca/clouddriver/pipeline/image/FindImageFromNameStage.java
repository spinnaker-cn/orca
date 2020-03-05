package com.netflix.spinnaker.orca.clouddriver.pipeline.image;

import com.netflix.spinnaker.orca.clouddriver.tasks.image.FindImageFromNameTask;
import com.netflix.spinnaker.orca.pipeline.StageDefinitionBuilder;
import com.netflix.spinnaker.orca.pipeline.TaskNode;
import com.netflix.spinnaker.orca.pipeline.model.Stage;
import org.springframework.stereotype.Component;

@Component
public class FindImageFromNameStage implements StageDefinitionBuilder {
  @Override
  public void taskGraph(Stage stage, TaskNode.Builder builder) {
    builder.withTask("findImage", FindImageFromNameTask.class);
  }
}
