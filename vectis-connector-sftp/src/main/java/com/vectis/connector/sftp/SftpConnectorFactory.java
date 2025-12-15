package com.vectis.connector.sftp;

import java.util.List;
import com.vectis.connector.ConfigParameter;
import com.vectis.connector.ConnectorFactory;
import com.vectis.connector.StorageConnector;

public class SftpConnectorFactory implements ConnectorFactory {
    @Override public String getType() { return "sftp"; }
    @Override public String getName() { return "SFTP"; }
    @Override public String getVersion() { return "1.0.0"; }
    @Override public String getDescription() { return "Connect to SFTP servers"; }
    @Override public StorageConnector create() { return new SftpConnector(); }
    @Override public List<ConfigParameter> getRequiredParameters() {
        return List.of(ConfigParameter.required("host", "SFTP host"), ConfigParameter.required("username", "Username"));
    }
    @Override public List<ConfigParameter> getOptionalParameters() {
        return List.of(ConfigParameter.password("password", "Password"), ConfigParameter.integer("port", "Port", 22));
    }
}
