package com.pesitwizard.fpdu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FpduParser {
    ByteBuffer buffer;
    int dataLength;
    boolean isConcatenated;

    public FpduParser(byte[] data) {
        this.buffer = ByteBuffer.wrap(data);
        this.dataLength = data.length;

        // Read first 2 bytes as potential FPDU length
        int firstLen = buffer.getShort() & 0xFFFF;

        if (firstLen == dataLength) {
            // Standard single FPDU: [len][phase][type][idDst][idSrc][data...]
            this.isConcatenated = false;
        } else if (firstLen > 6 && firstLen < dataLength) {
            // Concatenated FPDUs (section 4.5): first 2 bytes are sub-FPDU length, not
            // global length
            // Structure: [sub1_len][sub1_content][sub2_len][sub2_content]...
            // Reset buffer to read from start
            buffer.rewind();
            this.isConcatenated = true;
            log.info("Detected concatenated message: first sub-FPDU len={}, total data={}", firstLen, dataLength);
        } else {
            throw new IllegalArgumentException("Invalid Fpdu length: expected " + firstLen + ", got " + dataLength);
        }
    }

    public Fpdu parse() {
        if (isConcatenated) {
            return parseConcatenatedDtf();
        }
        return parseSingleFpdu();
    }

    private boolean isDtfType(FpduType type) {
        return type == FpduType.DTF || type == FpduType.DTFDA
                || type == FpduType.DTFMA || type == FpduType.DTFFA;
    }

    /**
     * Parse concatenated DTF FPDUs (section 4.5).
     * Structure:
     * [global_len][fpdu1_len][fpdu1_header+data][fpdu2_len][fpdu2_header+data]...
     */
    private Fpdu parseConcatenatedDtf() {
        ByteArrayOutputStream allData = new ByteArrayOutputStream();
        Fpdu resultFpdu = null;
        int subFpduCount = 0;

        while (buffer.remaining() >= 6) { // Minimum: len(2) + phase(1) + type(1) + idDst(1) + idSrc(1)
            int subLen = buffer.getShort() & 0xFFFF;
            if (subLen < 6 || subLen > buffer.remaining() + 2) {
                log.warn("Invalid sub-FPDU length: {} (remaining: {})", subLen, buffer.remaining());
                break;
            }

            int subPhase = buffer.get() & 0xFF;
            int subType = buffer.get() & 0xFF;
            int subIdDst = buffer.get() & 0xFF;
            int subIdSrc = buffer.get() & 0xFF;

            FpduType subFpduType = FpduType.from(subPhase, subType);
            subFpduCount++;

            // Data length = sub-FPDU length - 6 (header: len + phase + type + idDst +
            // idSrc)
            int dataLen = subLen - 6;
            if (dataLen > 0) {
                byte[] subData = new byte[dataLen];
                buffer.get(subData);
                try {
                    allData.write(subData);
                } catch (IOException e) {
                    // ByteArrayOutputStream doesn't throw
                }
                log.debug("Sub-FPDU {}: {} with {} bytes of data", subFpduCount, subFpduType, dataLen);
            }

            // Create result FPDU from first sub-FPDU
            if (resultFpdu == null) {
                resultFpdu = new Fpdu();
                resultFpdu.setFpduType(subFpduType);
                resultFpdu.setIdDst(subIdDst);
                resultFpdu.setIdSrc(subIdSrc);
            }
        }

        if (resultFpdu != null) {
            resultFpdu.setData(allData.toByteArray());
            log.info("Concatenated DTF: {} sub-FPDUs, {} total bytes", subFpduCount, allData.size());
        }

        return resultFpdu;
    }

    private Fpdu parseSingleFpdu() {
        Fpdu fpdu = new Fpdu();
        int phase = buffer.get() & 0xFF;
        int type = buffer.get() & 0xFF;
        System.out.println("Parsing Fpdu with phase: " + phase + ", type: " + type);
        fpdu.setFpduType(FpduType.from(phase, type));
        System.out.println("Fpdu type is: " + fpdu.getFpduType());
        int idDest = buffer.get();
        int idSrc = buffer.get();
        fpdu.setIdDst(idDest);
        fpdu.setIdSrc(idSrc);

        // DTF FPDUs contain raw data, not parameters
        if (fpdu.getFpduType() == FpduType.DTF || fpdu.getFpduType() == FpduType.DTFDA
                || fpdu.getFpduType() == FpduType.DTFMA || fpdu.getFpduType() == FpduType.DTFFA) {
            if (buffer.hasRemaining()) {
                byte[] rawData = new byte[buffer.remaining()];
                buffer.get(rawData);
                fpdu.setData(rawData);
                log.info("{} FPDU contains {} bytes of data", fpdu.getFpduType(), rawData.length);
            }
            return fpdu;
        }

        while (buffer.hasRemaining()) {
            // Read parameters from the buffer
            int paramId = buffer.get();
            System.out.println("Parsing parameter ID: " + paramId);
            int paramLength = buffer.get();
            if (paramLength == 0xff) {
                paramLength = buffer.getShort();
            }
            byte[] paramData = new byte[paramLength];
            buffer.get(paramData);
            if (ParameterIdentifier.fromId(paramId) != null) {
                ParameterIdentifier paramIdEnum = ParameterIdentifier.fromId(paramId);
                log.info("PI {} found which is {} and has a size of {} bytes", paramId, paramIdEnum, paramLength);
                ParameterValue paramValue = new ParameterValue(paramIdEnum, paramData);
                fpdu.getParameters().add(paramValue);
            } else if (ParameterGroupIdentifier.fromId(paramId) != null) {
                ParameterGroupIdentifier groupId = ParameterGroupIdentifier.fromId(paramId);
                log.info("PGI {} found which is {}", paramId, groupId);
                ParameterValue groupParameterValue = new ParameterValue(groupId, new ParameterValue[0]);
                fpdu.getParameters().add(groupParameterValue);
                ByteBuffer groupBuffer = ByteBuffer.wrap(paramData);
                while (groupBuffer.hasRemaining()) {
                    int groupParamId = groupBuffer.get();
                    int groupParamLength = groupBuffer.get();
                    byte[] groupParamData = new byte[groupParamLength];
                    groupBuffer.get(groupParamData);
                    ParameterValue groupParamValue = new ParameterValue(ParameterIdentifier.fromId(groupParamId),
                            groupParamData);
                    log.info("PI {} found which is {}", groupParamId, groupParamValue.getParameter());
                    groupParameterValue.getValues().add(groupParamValue);
                }
            } else {
                throw new IllegalArgumentException("Unknown parameter ID: " + paramId);
            }
        }
        return fpdu;
    }
}
