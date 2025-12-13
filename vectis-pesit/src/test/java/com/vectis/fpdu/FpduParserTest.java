package com.vectis.fpdu;

import static com.vectis.fpdu.ParameterIdentifier.PI_99_MESSAGE_LIBRE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class FpduParserTest {
    @Test
    void testFpduParser() {
        // Example byte array representing an Fpdu
        byte[] data = new byte[] { 0x00, 11, 0x40, 0x21, 0x01, 0x01, 99, 0x03, 0x01, 0x02, 0x03 };

        // Create a FpduParser instance
        FpduParser parser = new FpduParser(data);

        // Parse the Fpdu
        Fpdu fpdu = parser.parse();

        assertEquals(FpduType.ACONNECT, fpdu.getFpduType());
        assertNotNull(fpdu.getParameters());
        assertEquals(1, fpdu.getParameters().size(), "Expected one parameter");
        assertEquals(PI_99_MESSAGE_LIBRE, fpdu.getParameters().get(0).getParameter(), "Expected one parameter");
        assertEquals(5, fpdu.getParameters().get(0).getBytes().length, "Expected parameter value length");
        assertEquals(1, fpdu.getIdDst(), "Expected idDst to be 1");
        assertEquals(1, fpdu.getIdSrc(), "Expected idSrc to be 1");
    }
}
