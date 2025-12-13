package com.vectis.exception;

import com.vectis.fpdu.DiagnosticCode;
import com.vectis.fpdu.ParameterValue;

/**
 * Base exception for all PESIT protocol errors
 */
public class PesitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PesitException(ParameterValue pi2) {
        super(formatDiagnostic(pi2));
    }

    public PesitException(ParameterValue pi2, Throwable cause) {
        super(formatDiagnostic(pi2), cause);
    }

    private static String formatDiagnostic(ParameterValue pi2) {
        DiagnosticCode code = DiagnosticCode.fromParameterValue(pi2);
        if (code != null) {
            return code.getMessage();
        }
        // Unknown diagnostic code - format as hex
        byte[] bytes = pi2.getValue();
        if (bytes != null && bytes.length >= 3) {
            return String.format("Unknown PeSIT error: 0x%02X%02X%02X",
                    bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF);
        }
        return "Unknown PeSIT error";
    }
}
