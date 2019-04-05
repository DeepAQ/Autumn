package cn.imaq.autumn.restexample.aspect;

import cn.imaq.autumn.aop.annotation.Aspect;
import cn.imaq.autumn.aop.annotation.Hook;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Joinpoint;

@Aspect
@Slf4j
public class TestAspect {
    @Getter
    private static int hook1Executes, hook2Executes;

    @Hook("execution(* cn.imaq.autumn.restexample.controller..*(..))")
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

    @Hook("within(cn.imaq.autumn.restexample.controller..*)")
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
