package homework7.tests;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class Testing {

    @BeforeSuite
    public static void beforeSuite() {
        System.out.println("Started!");
    }

    @AfterSuite
    public static void afterSuite() {
        System.out.println("Finished!");
    }

    @Test(priority = 1)
    public static void test1() {
        System.out.println("1");
    }

    @Test(priority = 1)
    public static void test4() {
        System.out.println("1");
    }

    @Test(priority = 3)
    public static void test3() {
        System.out.println("3");
    }

    @Test(priority = 2)
    public static void test2() {
        System.out.println("2");
    }
}
