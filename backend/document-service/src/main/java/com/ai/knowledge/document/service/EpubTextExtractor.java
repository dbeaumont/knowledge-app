package com.ai.knowledge.document.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class EpubTextExtractor {

    public String extract(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             ZipInputStream zip = new ZipInputStream(inputStream)) {
            StringBuilder builder = new StringBuilder();
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName().toLowerCase();
                if (!name.endsWith(".xhtml") && !name.endsWith(".html") && !name.endsWith(".htm")) {
                    continue;
                }
                byte[] bytes = readEntry(zip);
                String text = Jsoup.parse(new ByteArrayInputStream(bytes), null, "").text();
                if (!text.isBlank()) {
                    builder.append(text).append('\n');
                }
            }
            return builder.toString().trim();
        }
    }

    private byte[] readEntry(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
