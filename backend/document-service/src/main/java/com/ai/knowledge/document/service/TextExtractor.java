package com.ai.knowledge.document.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

@Component
public class TextExtractor {

    public String extract(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        try {
            return decode(bytes, StandardCharsets.UTF_8.newDecoder());
        } catch (CharacterCodingException ex) {
            return decode(bytes, StandardCharsets.ISO_8859_1.newDecoder());
        }
    }

    private String decode(byte[] bytes, CharsetDecoder decoder) throws CharacterCodingException {
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        CharBuffer buffer = decoder.decode(ByteBuffer.wrap(bytes));
        return buffer.toString();
    }
}
