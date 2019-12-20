package com.github.fridujo.junit.extension.classpath.buildtool.maven;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
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
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.impl.DefaultArtifactResolver;
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager;
import org.eclipse.aether.internal.impl.DefaultRepositoryConnectorProvider;
import org.eclipse.aether.internal.impl.DefaultRepositoryEventDispatcher;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.internal.impl.DefaultSyncContextFactory;
import org.eclipse.aether.internal.impl.EnhancedLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;

import com.github.fridujo.junit.extension.classpath.PathElement;

abstract class MavenOperations {

    static final String ESC_SEP = Pattern.quote(File.separator);

    static final Pattern MAVEN_ARTIFACT_PATH_PATTERN = Pattern.compile("(?:.*)" + ESC_SEP
        + "(?<artifactId>.*)" + ESC_SEP + "(?<version>.*)" + ESC_SEP
        + "\\k<artifactId>-\\k<version>" + Pattern.quote(".jar"));

    private final DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
    private final ProjectModelResolver modelResolver = new ProjectModelResolver(
        session,
        null,
        new DefaultRepositorySystem()
            .setVersionRangeResolver(new DefaultVersionRangeResolver())
            .setArtifactResolver(new DefaultArtifactResolver()
                .setSyncContextFactory(new DefaultSyncContextFactory())
                .setRepositoryEventDispatcher(new DefaultRepositoryEventDispatcher())
                .setVersionResolver(new DefaultVersionResolver())
                .setRemoteRepositoryManager(new DefaultRemoteRepositoryManager())
                .setRepositoryConnectorProvider(new DefaultRepositoryConnectorProvider())),
        new DefaultRemoteRepositoryManager(),
        Collections.emptyList(),
        ProjectBuildingRequest.RepositoryMerging.POM_DOMINANT,
        null
    );

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
            return Optional.empty();
        }
        Optional<Path> pomPath = getPomPath(jarPath);
        if (!pomPath.isPresent()) return Optional.empty();
        try {
            ModelBuildingRequest request = new DefaultModelBuildingRequest()
                .setPomFile(pomPath.get().toFile())
                .setModelResolver(modelResolver);
            DefaultModelBuilder defaultModelBuilder = new DefaultModelBuilderFactory().newInstance();
            ModelBuildingResult result = defaultModelBuilder.build(request);

            return Optional.of(result.getEffectiveModel());
        } catch (ModelBuildingException e) {
            return Optional.empty();
        }
    }
}
