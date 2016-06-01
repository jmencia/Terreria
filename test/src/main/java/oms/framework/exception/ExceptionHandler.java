package oms.framework.exception;

/*
 * Interface for Exception handling components.
 * This Interface define the framework for creating Exception handlers.
 * When you create an Exeption handler class you need to implement the following methods
 * Priority - this is used to define the priority of your class. there could be many handlers
 * that want to work with a particular tyoe of Exception. you can specify what priority your handler should
 * be located at.  this is useful for deciding how it will handle your exception. certain handles can manage the exception
 * or if its an unknown exception it can pass it down to lower prioritzations to see if they can handle it.
 *
 * HandleException - this is the class that handles exceptions. it receives the exception and can try to manage the exception
 * or throw a new message that is legible.  some examples of thia could be alerting production support for certain instances
 * of exceptions. or creating a human readable alert for an exception. you can also have it call out to a self healing service
 * to try to resolve the exception for you.
 *
 * interestedInException - this is the Exception thhat the handler is interested in. at the moment its
 * 1 handler 1 exception.  Each handler should be specific to the type of exception that is being thrown
 */

public interface ExceptionHandler extends Comparable<ExceptionHandler> {

  public Long getPriority();

  public void setPriority(Long priority);

  public String HandleException(Throwable exc);

  public <T extends Throwable> Class<T> interestedInException();
}
