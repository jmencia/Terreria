package oms.framework.exception;

public abstract class AbstractExceptionHandler implements ExceptionHandler {

  private Long priority;
  private Class<?> clazz;

  public AbstractExceptionHandler(Long priority, Class<?> clazz) {
    this.priority = priority;
    this.clazz = clazz;
  }

  public int compareTo(ExceptionHandler comparedHandler) {
    if (this.equals(comparedHandler)) {
      return 0;
    } else {
      return this.getPriority().compareTo(comparedHandler.getPriority());
    }
  }

  public Long getPriority() {
    return priority;
  }

  public void setPriority(Long priority) {
    this.priority = priority;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public void setClazz(Class<?> clazz) {
    this.clazz = clazz;
  }

}
