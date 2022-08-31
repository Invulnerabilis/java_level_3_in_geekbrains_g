package homework7.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionTests {

    private static final String BEFORE_SUITE = "BeforeSuite";
    private static final String TEST = "Test";
    private static final String AFTER_SUITE = "AfterSuite";
    private static Map<String, List<Method>> methodMap;

    public static void start(Class testClass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        methodMap = getMethodsByAnnotations(testClass);

        throwExceptionIfMoreThanOne(BEFORE_SUITE);
        throwExceptionIfMoreThanOne(AFTER_SUITE);

        startMethodByAnnotation(BEFORE_SUITE);
        startMethodsWithTestAnnotation();
        startMethodByAnnotation(AFTER_SUITE);
    }

    private static Map<String, List<Method>> getMethodsByAnnotations(Class testClass) {
        Map<String, List<Method>> methodMap = new HashMap<>();
        for (Method method : testClass.getMethods()) {
            Annotation[] annotations = method.getAnnotations();
            if (annotations.length > 0) {
                if (methodMap.get(annotations[0].annotationType().getSimpleName()) == null) {
                    methodMap.put(annotations[0].annotationType().getSimpleName(), new ArrayList<>());
                }
                methodMap.get(annotations[0].annotationType().getSimpleName()).add(method);
            }
        }
        return methodMap;
    }

    private static void throwExceptionIfMoreThanOne(String annotationName) {
        List<Method> methods = methodMap.get(annotationName);
        if (methods != null && methods.size() > 1) {
            throw new RuntimeException(annotationName + " > 1");
        }
    }

    private static void startMethodByAnnotation(String annotation) throws IllegalAccessException, InvocationTargetException {
        List<Method> methods = methodMap.get(annotation);
        ((Method) methods.toArray()[0]).invoke(null);
    }

    private static void startMethodsWithTestAnnotation() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object[] methodsTest = methodMap.get(TEST).toArray();
        List<Method> methods = sortByPriority(methodsTest);
        for (Method method : methods) {
            System.out.print(method.getName() + " :");
            method.invoke(null);
        }
    }

    private static List<Method> sortByPriority(Object[] methodsTest) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<Integer, List<Method>> methods = new HashMap<>();
        for (int i = 0; i < methodsTest.length; i++) {
            Method method = (Method) methodsTest[i];
            int priority = getPriorityInTestAnnotation(method);
            if (methods.get(priority) == null) {
                methods.put(priority, new ArrayList<>());
            }
            methods.get(priority).add(method);
        }

        List<Method> sortMethods = new ArrayList<>();
        methods.values().stream().forEach(sortMethods::addAll);
        return sortMethods;
    }

    private static Integer getPriorityInTestAnnotation(Method method) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (Integer) method.getAnnotations()[0].annotationType().getMethod("priority").invoke(method.getAnnotations()[0]);
    }
}
