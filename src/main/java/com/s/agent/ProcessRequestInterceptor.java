package com.s.agent;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;

public class ProcessRequestInterceptor {

    @RuntimeType
    public static Object intercept(@AllArguments Object[] allArguments, @Morph OverrideCallable zuper) throws IOException {
        long agentStart = System.currentTimeMillis();
        Logger logger = LoggerFactory.getLogger(zuper.getClass());
        final StringBuilder in = new StringBuilder();
        String out = null;
        HttpServletResponse response = (HttpServletResponse) allArguments[1];
        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        allArguments[1] = responseWrapper;
        HttpServletRequest request = (HttpServletRequest) allArguments[0];
        RequestWrapper requestWrapper = new RequestWrapper(request);
        allArguments[0] = requestWrapper;
        try {
            if (request.getParameterMap() != null && request.getParameterMap().size() > 0) {
                request.getParameterMap().keySet().forEach(key -> in.append("key=" + key + " value=" + request.getParameter(key) + ","));
            }
            String queryString = getPostData(requestWrapper);
            if(!StringUtil.isEmpty(queryString)) {
                in.append(" queryString:" + getPostData(requestWrapper));
            }

            MDC.put("tid",UUID.randomUUID().toString());
            return zuper.call(allArguments);
        } catch (Exception e) {
            logger.error("Exception :{}" ,e);
            return null;
        } finally {
            byte[] content = responseWrapper.getBody();
            out = new String(content);
            logger.info("接口级别【url path:" + request.getRequestURI() + " 入参:" + in + "出参:"+out+" 接口整体耗时:" + (System.currentTimeMillis() - agentStart)+"ms】");
            MDC.clear();
        }
    }
    private static String getPostData(HttpServletRequest request) {
        StringBuffer data = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            while (null != (line = reader.readLine())) {
                data.append(line);
            }
        } catch (IOException e) {
        } finally {
        }
        return data.toString();
    }
}
