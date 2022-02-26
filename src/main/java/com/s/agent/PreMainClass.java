package com.s.agent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class PreMainClass {

    public static void premain(String agentparam, Instrumentation inst){

        final ByteBuddy byteBuddy = new ByteBuddy();
        AgentBuilder agentBuilder = new AgentBuilder.Default(byteBuddy);
        InputStreamReader configFileStream;

        try {
            File configFile =   new File(AgentPackagePath.getPath(),"/config/agent.config");
            configFileStream = new InputStreamReader(new FileInputStream(configFile), "UTF-8");
            Properties properties = new Properties();
            properties.load(configFileStream);
            ConfigInitializer.initialize(properties,Config.class);
        } catch (Exception e) {
        }
        ElementMatcher.Junction<NamedElement> e = nameStartsWith("net.bytebuddy.");
        String ignores = Config.Agent.ignore;
        if(!StringUtil.isEmpty(ignores)) {
            String[] ignore = ignores.split(",");
            for (int i = 0; i < ignore.length; i++) {
                System.out.println(ignore[i]);
                e = e.or(nameStartsWith(ignore[i]));
            }
        }
        // type指定了agent拦截的包名 以 com.agent作为前缀
        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, module) ->
             builder.visit(Advice.to(ServiceIntercept.class).on(ElementMatchers.any()));
        ;
        agentBuilder.disableClassFormatChanges().ignore(e)
                .type(ElementMatchers.nameStartsWith(Config.Agent.packname))
                .transform(transformer).installOn(inst);

        agentBuilder.ignore(ElementMatchers.named("javax.servlet.http"))
                .type(ElementMatchers.named("org.springframework.web.servlet.FrameworkServlet"))
                .transform((builder, type, classLoader, module) ->
                        builder.method(ElementMatchers.named("processRequest"))
                                .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideCallable.class)).to(ProcessRequestInterceptor.class)))
                .installOn(inst);
    }
}