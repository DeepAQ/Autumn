package cn.imaq.autumn.restexample.aspect;

import cn.imaq.autumn.aop.annotation.Aspect;
import cn.imaq.autumn.aop.annotation.Hook;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Slf4j
public class TestAspect {
    @Getter
    private static int hook1Executes, hook2Executes;

    @Pointcut("execution(* cn.imaq.autumn.restexample.controller..*(..))")
    private void controllerMethods() {
    }

    @Hook("controllerMethods()")
    public Object hook1(Joinpoint jp) throws Throwable {
        hook1Executes++;
        log.info("1 Before {}", jp.getStaticPart());
        Object ret;
        try {
            ret = jp.proceed();
        } catch (Throwable t) {
            log.error("1 Catch {}", jp.getStaticPart());
            t.printStackTrace();
            throw t;
        }
        log.info("1 After {}", jp.getStaticPart());
        return ret;
    }

    @Hook("controllerMethods()")
    public Object hook2(Joinpoint jp) throws Throwable {
        hook2Executes++;
        log.info("2 Before {}", jp.getStaticPart());
        Object ret;
        try {
            ret = jp.proceed();
        } catch (Throwable t) {
            log.error("2 Catch {}", jp.getStaticPart());
            t.printStackTrace();
            throw t;
        }
        log.info("2 After {}", jp.getStaticPart());
        return ret;
    }
}
