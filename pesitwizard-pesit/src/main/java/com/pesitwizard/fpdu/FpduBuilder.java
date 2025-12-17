package com.pesitwizard.fpdu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * FPDU (File Transfer Protocol Data Unit) Builder
 * Constructs binary PESIT protocol messages according to PESIT E specification
 */
public class FpduBuilder {
    public static byte[] buildFpdu(FpduType fpduType, int idDest, int idSrc, byte[] data) {
        ByteBuffer fpdu = ByteBuffer.allocate(6 + data.length);
        fpdu.putShort((short) (6 + data.length)); // Total length
        fpdu.put((byte) fpduType.getPhase());
        fpdu.put((byte) fpduType.getType());
        fpdu.put((byte) idDest);
        fpdu.put((byte) idSrc);
        fpdu.put(data);
        return fpdu.array();
    }

    public static byte[] buildFpdu(FpduType fpduType, int idDest, int idSrc) {
        return buildFpdu(fpduType, idDest, idSrc, new ParameterValue[0]);
    }

    /**
     * Build complete FPDU message with PI and PGI builders
     * 
     * @param fpduType FPDU type
     * @param idDest   Destination connection ID
     * @param idSrc    Source connection ID (or 0 for file-level FPDUs)
     * @param pis      Parameter Identifier builders
     * @param pgis     Parameter Group Identifier builders
     * @return Complete FPDU byte array
     */
    public static byte[] buildFpdu(FpduType fpduType, int idDest, int idSrc, ParameterValue... pis) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Parameter> parameters = new ArrayList<>();
        for (ParameterValue pi : pis) {
            try {
                out.write(pi.getBytes());
                parameters.add(pi.getParameter());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Validate mandatory parameters after building
        for (ParameterRequirement req : fpduType.getParameterRequirements()) {
            if (req.isMandatory() && !parameters.contains(req.getParameter())) {
                throw new IllegalArgumentException("Missing mandatory PI: " + req.getParameter().toString());
            }
        }

        return buildFpdu(fpduType, idDest, idSrc, out.toByteArray());
    }

    public static byte[] buildFpdu(Fpdu fpdu) {
        return buildFpdu(fpdu.getFpduType(), fpdu.getIdDst(), fpdu.getIdSrc(),
                fpdu.getParameters().toArray(new ParameterValue[0]));
    }
}
