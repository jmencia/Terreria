package oms.framework.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/*
 * This class can be used to run Methods based on Order of implementation. in case
 * you want to run junit tests in a linear fashion
 *
 */
public class OrderedTestRunner extends BlockJUnit4ClassRunner {

  // public OrderedTestRunner(Class<?> clazz) throws InitializationError
  // {
  // super(clazz);
  // }

  public OrderedTestRunner(Class<?> clazz) throws InitializationError {

    super(getFromTestClassloader(clazz));
  }

  private static Class<?> getFromTestClassloader(Class<?> clazz) throws InitializationError {
    try {
      return Class.forName(clazz.getName(), true, oracle.jdbc.OracleDriver.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      throw new InitializationError(e);
    }
  }

  @Override
  protected List<FrameworkMethod> computeTestMethods() {
    List<FrameworkMethod> toSort = super.computeTestMethods();

    if (toSort.isEmpty()) {
      return toSort;
    }

    final Map<Integer, FrameworkMethod> testMethods = new TreeMap<Integer, FrameworkMethod>();

    // check that all methods here are declared in the same class, we don't
    // deal with test methods from superclasses that haven't been overridden
    Class<?> clazz = getDeclaringClass(toSort);
    if (clazz == null) {
      // fail explicitly
      System.err.println(
          "OrderedTestRunner can only run test classes that" + " don't have test methods inherited from superclasses");
      return Collections.emptyList();
    }

    // use Javassist to figure out line numbers for methods
    ClassPool pool = ClassPool.getDefault();
    try {
      CtClass cc = pool.get(clazz.getName());
      // all methods in toSort are declared in the same class, we checked
      for (FrameworkMethod m : toSort) {
        String methodName = m.getName();
        CtMethod method = cc.getDeclaredMethod(methodName);
        testMethods.put(method.getMethodInfo().getLineNumber(0), m);
      }
    } catch (NotFoundException e) {
      e.printStackTrace();
    }

    return new ArrayList<FrameworkMethod>(testMethods.values());
  }

  private Class<?> getDeclaringClass(List<FrameworkMethod> methods) {
    // methods can't be empty, it's been checked
    Class<?> clazz = methods.get(0).getMethod().getDeclaringClass();

    for (int i = 1; i < methods.size(); i++) {
      if (!methods.get(i).getMethod().getDeclaringClass().equals(clazz)) {
        // they must be all in the same class
        return null;
      }
    }

    return clazz;
  }

}
