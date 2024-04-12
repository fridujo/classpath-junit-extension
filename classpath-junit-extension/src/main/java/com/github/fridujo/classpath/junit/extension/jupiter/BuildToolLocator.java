package com.github.fridujo.classpath.junit.extension.jupiter;

import com.github.fridujo.classpath.junit.extension.buildtool.BuildTool;
import org.junit.jupiter.api.extension.ExtensionContext;

class BuildToolLocator {
    private static final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(BuildToolLocator.class);

    static BuildTool locate(ExtensionContext extensionContext) {
        return getStore(extensionContext).get(BuildTool.class, BuildTool.class);
    }

    static BuildTool store(ExtensionContext extensionContext, BuildTool buildTool) {
        getStore(extensionContext).put(BuildTool.class, buildTool);
        return buildTool;
    }

    private static ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getRoot().getStore(namespace);
    }
}
