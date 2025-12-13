package com.vectis.fpdu;

import java.io.IOException;

/**
 * Builder for PESIT CONNECT message
 * Used to establish a PESIT session
 */
public class ConnectMessageBuilder {

    private String demandeur = "CLIENT";
    private String serveur = "SERVER";
    private int accessType = 0; // 0=write, 1=read, 2=mixed

    public ConnectMessageBuilder demandeur(String demandeur) {
        this.demandeur = demandeur;
        return this;
    }

    public ConnectMessageBuilder serveur(String serveur) {
        this.serveur = serveur;
        return this;
    }

    public ConnectMessageBuilder writeAccess() {
        this.accessType = 0;
        return this;
    }

    public ConnectMessageBuilder readAccess() {
        this.accessType = 1;
        return this;
    }

    public ConnectMessageBuilder mixedAccess() {
        this.accessType = 2;
        return this;
    }

    /**
     * Build complete CONNECT FPDU
     * 
     * @return Complete FPDU byte array
     * @throws IOException if serialization fails
     */
    public Fpdu build(int connectionId) throws IOException {
        return new Fpdu(FpduType.CONNECT)
                .withParameter(new ParameterValue(ParameterIdentifier.PI_03_DEMANDEUR, demandeur))
                .withParameter(new ParameterValue(ParameterIdentifier.PI_04_SERVEUR, serveur))
                .withParameter(new ParameterValue(ParameterIdentifier.PI_06_VERSION, 2))
                .withParameter(new ParameterValue(ParameterIdentifier.PI_22_TYPE_ACCES, accessType))
                .withIdSrc(connectionId)
                .withIdDst(0);
    }
}
