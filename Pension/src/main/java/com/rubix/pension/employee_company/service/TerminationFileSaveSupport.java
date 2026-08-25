package com.rubix.pension.employee_company.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Optionally persists generated termination artifacts (PDFs/Excel) to a server-side directory
 * path supplied by the caller (mirrors the legacy Access "Me.URL" folder picker: the frontend may
 * send a save path, or omit it entirely — it's nullable/optional). The HTTP response always still
 * returns the generated file(s); this is purely a best-effort additional side effect.
 *
 * <p>Deliberately never throws: this runs <em>after</em> the termination itself has already been
 * committed to the database, so a bad/unwritable {@code savePath} must never turn an otherwise
 * successful termination + PDF/Excel response into a failed request (that would discard the
 * already-generated file the caller is waiting on, even though the termination already
 * happened). Failures are logged and swallowed instead.</p>
 */
@Service
public class TerminationFileSaveSupport {

    private static final Logger log = LoggerFactory.getLogger(TerminationFileSaveSupport.class);

    public boolean isBlank(String savePath) {
        return savePath == null || savePath.trim().isEmpty();
    }

    public void save(String savePath, String filename, byte[] content) {
        if (isBlank(savePath)) {
            return;
        }
        try {
            Path directory = Path.of(savePath.trim());
            Files.createDirectories(directory);
            Files.write(directory.resolve(filename), content);
        } catch (IOException | java.nio.file.InvalidPathException ex) {
            log.warn("Could not save \"{}\" to \"{}\": {}", filename, savePath, ex.getMessage());
        }
    }
}
