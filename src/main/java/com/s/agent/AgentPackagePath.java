package com.s.agent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class AgentPackagePath {

    private static File AGENT_PACKAGE_PATH;

    public static File getPath() throws Exception {
        if (AGENT_PACKAGE_PATH == null) {
            AGENT_PACKAGE_PATH = findPath();
        }
        return AGENT_PACKAGE_PATH;
    }

    public static boolean isPathFound() {
        return AGENT_PACKAGE_PATH != null;
    }

    private static File findPath() throws Exception {
        String classResourcePath = AgentPackagePath.class.getName().replaceAll("\\.", "/") + ".class";

        URL resource = AgentPackagePath.class.getClassLoader().getSystemClassLoader().getResource(classResourcePath);
        if (resource != null) {
            String urlString = resource.toString();

            int insidePathIndex = urlString.indexOf('!');
            boolean isInJar = insidePathIndex > -1;

            if (isInJar) {
                urlString = urlString.substring(urlString.indexOf("file:"), insidePathIndex);
                File agentJarFile = null;
                try {
                    agentJarFile = new File(new URL(urlString).getFile());
                } catch (MalformedURLException e) {
                }
                if (agentJarFile.exists()) {
                    return agentJarFile.getParentFile();
                }
            } else {
                String classLocation = urlString.substring(urlString.indexOf("file:"), urlString.length() - classResourcePath.length());
                return new File(classLocation);
            }
        }

        throw new Exception("Can not locate agent jar file.");
    }

}
