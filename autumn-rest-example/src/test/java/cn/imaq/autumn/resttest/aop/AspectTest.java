package cn.imaq.autumn.resttest.aop;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;

public class AspectTest {
    @AfterSuite
    public void validateHookExecution() {
        Assert.assertTrue(TestAspect.aroundCount > 0);
        Assert.assertEquals(TestAspect.aroundCount, TestAspect.beforeCount);
        Assert.assertEquals(TestAspect.aroundCount, TestAspect.afterCount);
        Assert.assertEquals(TestAspect.aroundCount, TestAspect.afterReturningCount);
        Assert.assertEquals(TestAspect.afterThrowingCount, 0);
    }
}