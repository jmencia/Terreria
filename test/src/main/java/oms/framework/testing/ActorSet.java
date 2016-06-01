package oms.framework.testing;

import java.util.Set;
import java.util.TreeSet;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

@SuppressWarnings("serial")
public class ActorSet<T> extends TreeSet<T> implements MethodRule {
  protected Set<ActorHandler<T>> handlers = new TreeSet<ActorHandler<T>>();

  public ActorSet(ActorHandler<T> handler) {
    handlers.add(handler);
  }

  public ActorSet() {
  }

  public void setHandler(ActorHandler<T> handler) {
    handlers.add(handler);
  }

  public Integer getHandlersSetSize() {
    return handlers.size();
  }

  protected void before(String lifeCycleName) {
    for (ActorHandler<T> handler : handlers) {
      handler.interestedIn(lifeCycleName);
      ActorSet<T> actors = handler.handle();
      this.addAll(actors);
    }
  }

  protected void after() {
    this.clear();
  }

  public final Statement apply(final Statement base, final FrameworkMethod method, Object target) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {

        before(method.getName());
        try {
          base.evaluate();
        } finally {
          after();
        }
      }
    };
  }
}
