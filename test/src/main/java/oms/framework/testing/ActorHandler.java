package oms.framework.testing;

public interface ActorHandler<T> {
  public ActorSet<T> handle();

  public void interestedIn(String lifeCycleName);
}
