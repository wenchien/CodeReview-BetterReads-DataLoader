package connect;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;

// used for exposing astra specific properties
@ConfigurationProperties(prefix = "datastax.astra")
public class DataStaxAstraProperties {
    
    private File secureConnectBundle;

    // Can be lombok'd instead
    public File getSecureConnectBundle() {
        return secureConnectBundle;
    }

    public void setSecureConnectBundle(File secureConnectBundle) {
        this.secureConnectBundle = secureConnectBundle;
    }
}
