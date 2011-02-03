package com.bigfatgun;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MavenClassLoader {

    public static class ClassLoaderBuilder {

        private final List<RemoteRepository> repositories;
        private final File localRepositoryDirectory;

        public ClassLoaderBuilder(RemoteRepository... repositories) {
            // TODO : preconditions : not empty, not null
            this.repositories = Arrays.asList(repositories);
            this.localRepositoryDirectory = new File(".m2/repository");
        }

        public URLClassLoader forGAV(String gav) {
            try {
                Dependency dependency = new Dependency(new DefaultArtifact(gav), "compile");

                CollectRequest collectRequest = new CollectRequest();
                collectRequest.setRoot(dependency);
                for (RemoteRepository repository : repositories) {
                    collectRequest.addRepository(repository);
                }

                RepositorySystem repositorySystem = newRepositorySystem();
                RepositorySystemSession session = newSession(repositorySystem);
                DependencyNode node = repositorySystem.collectDependencies(session, collectRequest).getRoot();

                repositorySystem.resolveDependencies(session, node, null);

                PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
                node.accept(nlg);

                List<Artifact> artifacts = nlg.getArtifacts(false);
                List<URL> urls = new ArrayList<URL>(artifacts.size());

                for (Artifact artifact : artifacts) {
                    urls.add(artifact.getFile().toURI().toURL());
                }
                URL[] urlArray = urls.toArray(new URL[urls.size()]);
                return new URLClassLoader(urlArray, null);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        public RepositorySystem newRepositorySystem() throws Exception {
            return new DefaultPlexusContainer().lookup(RepositorySystem.class);
        }

        public RepositorySystemSession newSession(RepositorySystem system) {
            MavenRepositorySystemSession session = new MavenRepositorySystemSession();

            LocalRepository localRepo = new LocalRepository(localRepositoryDirectory);
            session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));
            session.setRepositoryListener(new AbstractRepositoryListener() {
            });
            session.setTransferListener(new AbstractTransferListener() {
            });

            return session;
        }
    }

    public static ClassLoader forGAV(String gav) {
        return usingCentralRepo().forGAV(gav);
    }

    public static ClassLoaderBuilder usingCentralRepo() {
        RemoteRepository central = new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/");
        return new ClassLoaderBuilder(central);
    }
}
