package com.netflix.spinnaker.orca.clouddriver.tasks.providers.ecloud;

import com.netflix.spinnaker.orca.clouddriver.tasks.servergroup.InterestingHealthProviderNamesSupplier;
import com.netflix.spinnaker.orca.pipeline.model.Stage;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EcloudInterestingHealthProviderNamesSupplier
  implements InterestingHealthProviderNamesSupplier {

  private static final String ECLOUD = "ecloud";

  @Override
  public boolean supports(String cloudProvider, Stage stage) {
    if (cloudProvider.equals(ECLOUD)) {
      return true;
    }
    return false;
  }

  @Override
  public List<String> process(String cloudProvider, Stage stage) {
    return Arrays.asList("Ecloud");
  }
}
