package cn.imaq.autumn.resttest.aop;

import cn.imaq.autumn.aop.annotation.Aspect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.annotation.*;
import org.testng.Assert;

@Aspect
@Slf4j
public class TestAspect {
    @Getter
    static int aroundCount, beforeCount, afterCount, afterReturningCount, afterThrowingCount;

    @Pointcut("execution(* cn.imaq.autumn.restexample.controller..*(..))")
    private void controllerMethods() {
    }

    @Around("controllerMethods()")
    private Object around(Joinpoint jp) throws Throwable {
        Assert.assertNotNull(jp);

        aroundCount++;
        log.info("> Before {}", jp.getStaticPart());
        Object ret;
        try {
            ret = jp.proceed();
        } catch (Throwable t) {
            log.error("> Catch {}", jp.getStaticPart());
            t.printStackTrace();
            throw t;
        }
        log.info("> After {}", jp.getStaticPart());
        return ret;
    }

    @Before("controllerMethods()")
    private void before(Joinpoint jp) {
        Assert.assertNotNull(jp);
        Assert.expectThrows(UnsupportedOperationException.class, jp::proceed);
        beforeCount++;
        log.info("< Before {}", jp.getStaticPart());
    }

    @After("controllerMethods()")
    private void after(Joinpoint jp) {
        Assert.assertNotNull(jp);
        Assert.expectThrows(UnsupportedOperationException.class, jp::proceed);
        afterCount++;
        log.info("< After {}", jp.getStaticPart());
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "2", argNames = "1,2")
    private void afterReturning(Joinpoint jp, Object ret) {
        Assert.assertNotNull(jp);
        Assert.assertNotNull(ret);
        Assert.expectThrows(UnsupportedOperationException.class, jp::proceed);
        afterReturningCount++;
        log.info("< AfterReturning {}", jp.getStaticPart());
    }

    @AfterThrowing(pointcut = "controllerMethods()", throwing = "2", argNames = "1,2")
    private void afterThrowing(Joinpoint jp, Exception ex) {
        Assert.assertNotNull(jp);
        Assert.assertNotNull(ex);
        Assert.expectThrows(UnsupportedOperationException.class, jp::proceed);
        afterThrowingCount++;
        log.info("< AfterThrowing {}", jp.getStaticPart());
    }
}
