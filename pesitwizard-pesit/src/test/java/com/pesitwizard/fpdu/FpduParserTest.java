package com.pesitwizard.fpdu;

import static com.pesitwizard.fpdu.ParameterIdentifier.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FpduParserTest {
    @Test
    void testFpduParser() {
        // FPDU with header: [len(2)][phase][type][idDst][idSrc][params...]
        // len = 11 (total FPDU length)
        // ACONNECT = phase 0x40, type 0x21
        // PI_99 = 99, length 3, data [0x01, 0x02, 0x03]
        byte[] data = new byte[] { 0x00, 11, 0x40, 0x21, 0x01, 0x01, 99, 0x03, 0x01, 0x02, 0x03 };

        FpduParser parser = new FpduParser(data);
        Fpdu fpdu = parser.parse();

        assertEquals(FpduType.ACONNECT, fpdu.getFpduType());
        assertNotNull(fpdu.getParameters());
        assertEquals(1, fpdu.getParameters().size(), "Expected one parameter");
        assertEquals(PI_99_MESSAGE_LIBRE, fpdu.getParameters().get(0).getParameter());
        assertEquals(3, fpdu.getParameters().get(0).getValue().length, "Expected parameter value length");
        assertEquals(1, fpdu.getIdDst(), "Expected idDst to be 1");
        assertEquals(1, fpdu.getIdSrc(), "Expected idSrc to be 1");
    }
}
