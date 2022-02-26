package com.s.agent;

import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class ServiceIntercept {

    @Advice.OnMethodEnter()
    public static Long before(@Advice.Origin String methodIns) {
        return System.currentTimeMillis();
    }

    @Advice.OnMethodExit
    public static void after(@Advice.Origin String methodIns, @Advice.Origin("#m") String methodName, @Advice.This Object o, @Advice.Enter Long ms) {
        Logger logger = LoggerFactory.getLogger(o.getClass());
        logger.info("方法级别【类名:"+o.getClass().getName()+"  方法名："+methodName+" 执行时间为:"+(System.currentTimeMillis() - ms+"ms】"));
    }
}
