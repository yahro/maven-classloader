package com.bigfatgun;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public final class MavenClassLoader {

    public static class ClassLoaderBuilder {

        private static final String COMPILE_SCOPE = "compile";
        private static final ClassLoader SHARE_NOTHING = null;

        private final ImmutableList<RemoteRepository> repositories;
        private final File localRepositoryDirectory;

        private ClassLoaderBuilder(RemoteRepository... repositories) {
            Preconditions.checkNotNull(repositories);
            Preconditions.checkArgument(repositories.length > 0, "Must specify at least one remote repository.");

            this.repositories = ImmutableList.copyOf(repositories);
            this.localRepositoryDirectory = new File(".m2/repository");
        }

        public URLClassLoader forGAV(String gav) {
            try {
                CollectRequest collectRequest = createCollectRequestForGAV(gav);
                List<Artifact> artifacts = collectDependenciesIntoArtifacts(collectRequest);

                List<URL> urls = Lists.transform(artifacts, new Function<Artifact, URL>() {
                    @Override
                    public URL apply(Artifact input) {
                        try {
                            return input.getFile().toURI().toURL();
                        } catch (MalformedURLException e) {
                            throw Throwables.propagate(e);
                        }
                    }
                });

                return new URLClassLoader(urls.toArray(new URL[urls.size()]), SHARE_NOTHING);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

        private CollectRequest createCollectRequestForGAV(String gav) {
            Dependency dependency = new Dependency(new DefaultArtifact(gav), COMPILE_SCOPE);

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot(dependency);
            for (RemoteRepository repository : repositories) {
                collectRequest.addRepository(repository);
            }

            return collectRequest;
        }

        private List<Artifact> collectDependenciesIntoArtifacts(CollectRequest collectRequest)
                throws PlexusContainerException, ComponentLookupException, DependencyCollectionException, ArtifactResolutionException {

            RepositorySystem repositorySystem = newRepositorySystem();
            RepositorySystemSession session = newSession(repositorySystem);
            DependencyNode node = repositorySystem.collectDependencies(session, collectRequest).getRoot();

            repositorySystem.resolveDependencies(session, node, null);

            PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
            node.accept(nlg);

            return nlg.getArtifacts(false);
        }

        private RepositorySystem newRepositorySystem() throws PlexusContainerException, ComponentLookupException {
            return new DefaultPlexusContainer().lookup(RepositorySystem.class);
        }

        private RepositorySystemSession newSession(RepositorySystem system) {
            MavenRepositorySystemSession session = new MavenRepositorySystemSession();

            LocalRepository localRepo = new LocalRepository(localRepositoryDirectory);
            session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

            return session;
        }
    }

    /**
     * Creates a classloader that will resolve artifacts against the default "central" repository. Throws 
     * {@link IllegalArgumentException} if the GAV is invalid, {@link NullPointerException} if the GAV is null.
     *
     * @param gav artifact group:artifact:version, i.e. joda-time:joda-time:1.6.2
     * @return a classloader that can be used to load classes from the given artifact
     */
    public static URLClassLoader forGAV(String gav) {
        return usingCentralRepo().forGAV(Preconditions.checkNotNull(gav));
    }

    public static ClassLoaderBuilder usingCentralRepo() {
        RemoteRepository central = new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/");
        return new ClassLoaderBuilder(central);
    }
}
