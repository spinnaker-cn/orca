package com.netflix.spinnaker.orca.clouddriver.tasks.providers.huaweicloud

import com.netflix.spinnaker.orca.clouddriver.OortService
import com.netflix.spinnaker.orca.clouddriver.tasks.image.ImageFinder
import com.netflix.spinnaker.orca.pipeline.model.Stage
import java.util.stream.Collectors
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonProperty


@Component
class HuaweiCloudImageFinder implements ImageFinder {

  final String cloudProvider = "huaweicloud"

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
    List<HuaweiCloudImage> allMatchedImages =
      oortService.findImage(getCloudProvider(), packageName, null, null, ['imageName':imageName])
        .stream()
        .map({ objectMapper.convertValue(it, HuaweiCloudImage.class)} )
        .collect(Collectors.toList())

    if (allMatchedImages.size() > 0) {
      HuaweiCloudImage latestImage = allMatchedImages[0]
      return Collections.singletonList(latestImage.toHuaweiCloudImageDetails())
    } else {
      return null
    }
  }

  private static class HuaweiCloudImage {
    @JsonProperty
    String imageName

    @JsonProperty
    Map<String, Object> attributes

    ImageDetails toHuaweiCloudImageDetails() {
      JenkinsDetails jenkinsDetails = new JenkinsDetails("", "", "")
      String imageId = attributes.get("imageId").toString()
      return new HuaweiImageDetails(imageName, imageId, jenkinsDetails)
    }

  }

  private static class HuaweiImageDetails extends HashMap<String, Object> implements ImageDetails{

    HuaweiImageDetails (String imageName, String imageId,  JenkinsDetails jenkinsDetails) {
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
