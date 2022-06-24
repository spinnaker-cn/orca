package com.netflix.spinnaker.orca.clouddriver.tasks.providers.hecloud

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.orca.clouddriver.OortService
import com.netflix.spinnaker.orca.clouddriver.tasks.image.ImageFinder
import com.netflix.spinnaker.orca.pipeline.model.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.stream.Collectors

@Component
class HeCloudImageFinder implements ImageFinder {

  final String cloudProvider = "hecloud"

  @Autowired
  OortService oortService

  @Autowired
  ObjectMapper objectMapper

  @Override
  String getCloudProvider() {
    return cloudProvider
  }


  @Override
  Collection<ImageDetails> byTags(
    Stage stage, String packageName, Map<String, String> tags) {
    //TO-DO
    return null
  }

  @Override
  Collection<ImageDetails> byName(Stage stage, String packageName, String imageName) {
    StageData stageData = (StageData) stage.mapTo(StageData.class)
    List<HeCloudImage> allMatchedImages =
      oortService.findImage(getCloudProvider(), packageName, null, null, ['imageName':imageName])
        .stream()
        .map({ objectMapper.convertValue(it, HeCloudImage.class)} )
        .collect(Collectors.toList())

    if (allMatchedImages.size() > 0) {
      HeCloudImage latestImage = allMatchedImages[0]
      return Collections.singletonList(latestImage.toHeCloudImageDetails())
    } else {
      return null
    }
  }

  private static class HeCloudImage {
    @JsonProperty
    String imageName

    @JsonProperty
    Map<String, Object> attributes

    ImageDetails toHeCloudImageDetails() {
      JenkinsDetails jenkinsDetails = new JenkinsDetails("", "", "")
      String imageId = attributes.get("imageId").toString()
      return new HeImageDetails(imageName, imageId, jenkinsDetails)
    }

  }

  private static class HeImageDetails extends HashMap<String, Object> implements ImageDetails{

    HeImageDetails(String imageName, String imageId, JenkinsDetails jenkinsDetails) {
      put("imageName", imageName)
      put("imageId", imageId)
      put("ami", imageId)
      put("amiId", imageName)
      put("region", "global")
      put("jenkins", jenkinsDetails)
    }
    @Override
    String getImageId() {
      return (String) super.get("imageId")
    }

    @Override
    String getImageName() {
      return (String) super.get("imageName")
    }

    @Override
    String getRegion() {
      return (String) super.get("region")
    }

    @Override
    JenkinsDetails getJenkins() {
      return (JenkinsDetails) super.get("jenkins")
    }
  }

  static class StageData {
    @JsonProperty
    Collection<String> regions
  }
}
