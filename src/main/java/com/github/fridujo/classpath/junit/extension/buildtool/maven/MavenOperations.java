package com.github.fridujo.classpath.junit.extension.buildtool.maven;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectModelResolver;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.RepositoryConnectorProvider;
import org.eclipse.aether.internal.impl.DefaultArtifactResolver;
import org.eclipse.aether.internal.impl.DefaultRepositoryEventDispatcher;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.internal.impl.DefaultSyncContextFactory;
import org.eclipse.aether.internal.impl.EnhancedLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import com.github.fridujo.classpath.junit.extension.PathElement;
import com.github.fridujo.classpath.junit.extension.Configuration;

abstract class MavenOperations {

    static final String ESC_SEP = Pattern.quote(File.separator);

    static final Pattern MAVEN_ARTIFACT_PATH_PATTERN = Pattern.compile("(?:.*)" + ESC_SEP
        + "(?<artifactId>.*)" + ESC_SEP + "(?<version>.*)" + ESC_SEP
        + "\\k<artifactId>-\\k<version>" + Pattern.quote(".jar"));

    private final DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
    private final ProjectModelResolver modelResolver;

    {
        DefaultServiceLocator serviceLocator = new DefaultServiceLocator();
        serviceLocator.setService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        serviceLocator.setService(TransporterFactory.class, HttpTransporterFactory.class);

        RemoteRepositoryManager remoteRepositoryManager = serviceLocator.getService(RemoteRepositoryManager.class);
        RepositoryConnectorProvider repositoryConnectorProvider = serviceLocator.getService(RepositoryConnectorProvider.class);
        modelResolver = new ProjectModelResolver(
            session,
            null,
            new DefaultRepositorySystem()
                .setVersionRangeResolver(new DefaultVersionRangeResolver())
                .setArtifactResolver(new DefaultArtifactResolver()
                    .setSyncContextFactory(new DefaultSyncContextFactory())
                    .setRepositoryEventDispatcher(new DefaultRepositoryEventDispatcher())
                    .setVersionResolver(new DefaultVersionResolver())
                    .setRemoteRepositoryManager(remoteRepositoryManager)
                    .setRepositoryConnectorProvider(repositoryConnectorProvider)),
            remoteRepositoryManager,
            Collections.emptyList(),
            ProjectBuildingRequest.RepositoryMerging.POM_DOMINANT,
            null
        );
    }

    protected MavenOperations(String localRepo) {
        try {
            this.session.setLocalRepositoryManager(new EnhancedLocalRepositoryManagerFactory().newInstance(session, new LocalRepository(localRepo)));
        } catch (NoLocalRepositoryManagerException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Optional<Path> getPomPath(PathElement jarPath) {
        String rawPath = jarPath.toPath().toString();
        if (!rawPath.endsWith(".jar")) {
            return Optional.empty();
        }
        Path pomPath = Paths.get(rawPath.substring(0, rawPath.length() - 4) + ".pom");
        if (!Files.exists(pomPath)) {
            return Optional.empty();
        }
        return Optional.of(pomPath);
    }

    /**
     * This method is relatively slow.<br>
     * Considering that it will be called hundreds of times, even if it take only 10ms (90th),
     * the overall time will be above seconds.
     * <p>
     * This is why most of the results of this methods must be cached, to avoid degrading performances proportionally to the number of uses.
     */
    protected Optional<Model> loadMavenProject(PathElement jarPath) {
        Matcher matcher = MAVEN_ARTIFACT_PATH_PATTERN.matcher(jarPath.toPath().toString());
        if (!matcher.matches()) {
            log("Not a Maven artifact jarPath: " + jarPath);
            return Optional.empty();
        }
        Optional<Path> pomPath = getPomPath(jarPath);
        if (!pomPath.isPresent()) {
            log("No POM file matching jarPath: " + jarPath);
            return Optional.empty();
        }
        try {
            ModelBuildingRequest request = new DefaultModelBuildingRequest()
                .setPomFile(pomPath.get().toFile())
                .setModelResolver(modelResolver);
            DefaultModelBuilder defaultModelBuilder = new DefaultModelBuilderFactory().newInstance();
            defaultModelBuilder.setProfileSelector((profiles, context, problems) -> Collections.emptyList());
            ModelBuildingResult result = defaultModelBuilder.build(request);

            return Optional.of(result.getEffectiveModel());
        } catch (ModelBuildingException e) {
            log("Failed to read POM model from: " + pomPath.get() + ": " + e.getMessage().replaceAll("\r?\n", " "));
            return Optional.empty();
        }
    }

    private void log(String message) {
        if (Configuration.INSTANCE.verbose) {
            System.out.println(message);
        }
    }

    protected Result executeGoal(String... cmdLine) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Arrays.asList(cmdLine));
        request.setBatchMode(true);
        Path pomPath = Paths.get("").toAbsolutePath().resolve("pom.xml");
        if (Files.exists(pomPath)) {
            // Useful to lookup for declared remote repositories (other than central)
            request.setPomFile(pomPath.toFile());
        }
        Invoker invoker = new DefaultInvoker();
        PrefixedPrintStreamHandler outputHandler = new PrefixedPrintStreamHandler("    <internal> ");
        invoker.setOutputHandler(outputHandler);
        invoker.setErrorHandler(outputHandler);

        try {
            InvocationResult result = invoker.execute(request);
            return new Result(result.getExitCode());
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }

    static final class Result {
        private final int exitCode;

        private Result(int exitCode) {
            this.exitCode = exitCode;
        }

        public <THROWABLE extends Throwable> void throwsOnError(Supplier<? extends THROWABLE> exceptionSupplier) throws THROWABLE {
            if (exitCode != 0) {
                throw exceptionSupplier.get();
            }
        }
    }
}
