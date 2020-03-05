package com.netflix.spinnaker.orca.clouddriver.tasks.image;

import com.netflix.spinnaker.orca.ExecutionStatus;
import com.netflix.spinnaker.orca.RetryableTask;
import com.netflix.spinnaker.orca.TaskResult;
import com.netflix.spinnaker.orca.clouddriver.tasks.AbstractCloudProviderAwareTask;
import com.netflix.spinnaker.orca.pipeline.model.Stage;
import com.netflix.spinnaker.kork.artifacts.model.Artifact;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.kork.artifacts.model.Artifact;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FindImageFromNameTask extends AbstractCloudProviderAwareTask implements RetryableTask {

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  List<ImageFinder> imageFinders;

  @Value("${tasks.findImageFromNameTimeoutMillis:10000}")
  private Long findImageFromNameTimeoutMillis;

  @Override
  public TaskResult execute(Stage stage) {
    String cloudProvider = getCloudProvider(stage);
    ImageFinder imageFinder = imageFinders.stream()
      .filter(it -> it.getCloudProvider().equals(cloudProvider))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("ImageFinder not found for cloudProvider " + cloudProvider));
    FindImageFromNameTask.StageData stageData = (FindImageFromNameTask.StageData) stage.mapTo(FindImageFromNameTask.StageData.class);
    Collection<ImageFinder.ImageDetails> imageDetails = imageFinder.byName(stage, stageData.packageName, stageData.imageName);

    if (imageDetails == null || imageDetails.isEmpty()) {
      throw new IllegalStateException("Could not find named image for package: " + stageData.packageName + " and name: " + stageData.imageName);
    }

    List<Artifact> artifacts = new ArrayList<>();
    imageDetails.forEach(imageDetail -> artifacts.add(generateArtifactFrom(imageDetail, cloudProvider)));

    Map<String, Object> stageOutputs = new HashMap<>();
    stageOutputs.put("amiDetails", imageDetails);
    stageOutputs.put("artifacts", artifacts);

    return new TaskResult(
      ExecutionStatus.SUCCEEDED,
      stageOutputs,
      Collections.singletonMap("deploymentDetails", imageDetails)
    );
  }

  private Artifact generateArtifactFrom(ImageFinder.ImageDetails imageDetails, String cloudProvider) {
    Map<String, Object> metadata = new HashMap<>();
    try {
      ImageFinder.JenkinsDetails jenkinsDetails = imageDetails.getJenkins();
      metadata.put("build_info_url", jenkinsDetails.get("host"));
      metadata.put("build_number", jenkinsDetails.get("number"));
    } catch (Exception e) {
      // This is either all or nothing
    }

    Artifact artifact = new Artifact();
    artifact.setName(imageDetails.getImageName());
    artifact.setReference(imageDetails.getImageId());
    artifact.setLocation(imageDetails.getRegion());
    artifact.setType(cloudProvider + "/image");
    artifact.setMetadata(metadata);
    artifact.setUuid(UUID.randomUUID().toString());

    return artifact;
  }

  @Override
  public long getBackoffPeriod() {
    return 10000;
  }

  @Override
  public long getTimeout() {
    return this.findImageFromNameTimeoutMillis;
  }

  static class StageData {
    @JsonProperty
    String packageName;

    @JsonProperty
    String imageName;
  }

}
