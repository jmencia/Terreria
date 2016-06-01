package oms.framework.testing;

import java.util.Set;

import oms.framework.utils.ResourceReader;

public class ActorsForJNDIHandler implements ActorHandler<String> {
  @SuppressWarnings("unused")
  private String lifeCycleName;
  public ResourceReader reader;

  public ActorsForJNDIHandler(ResourceReader reader) {
    this.reader = reader;
  }

  public ActorSet<String> handle() {
    @SuppressWarnings("unchecked")
    Set<String> actors = (Set<String>) reader.read();
    ActorSet<String> actorSet = new ActorSet<String>();
    actorSet.addAll(actors);
    return actorSet;
  }

  public void interestedIn(String lifeCycle) {
    lifeCycleName = lifeCycle;
  }

}
