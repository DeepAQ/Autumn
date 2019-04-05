package cn.imaq.autumn.resttest;

import cn.imaq.autumn.restexample.aspect.TestAspect;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;

public class AspectTest {
    @AfterSuite
    public void validateHookExecution() {
        Assert.assertTrue(TestAspect.getHook1Executes() > 0);
        Assert.assertTrue(TestAspect.getHook2Executes() > 0);
        Assert.assertEquals(TestAspect.getHook1Executes(), TestAspect.getHook2Executes());
    }
}