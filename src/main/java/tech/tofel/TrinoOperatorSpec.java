package tech.tofel;

import java.util.List;

public class TrinoOperatorSpec {

    public String getImageRef() {
        return imageRef;
    }

    public List<String> getCatalogs() {
        return catalogs;
    }

    // Add Spec information here
    // Trinodb image ref, registry URL and tag for a workable TrinoDB container image
    String imageRef;

    public boolean isCoordinator() {
        return coordinator;
    }

    public boolean isNodeSchedulerIncludeCoordinator() {
        return nodeSchedulerIncludeCoordinator;
    }

    public int getHttpServerHttpPort() {
        return httpServerHttpPort;
    }

    public String getDiscoveryURI() {
        return discoveryURI;
    }

    // Trino /etc/trino/config.properties file information:
    boolean coordinator;
    boolean nodeSchedulerIncludeCoordinator;
    int httpServerHttpPort;
    String discoveryURI;

    // Names of catalogs to create on trino startup
    List<String> catalogs;
}
