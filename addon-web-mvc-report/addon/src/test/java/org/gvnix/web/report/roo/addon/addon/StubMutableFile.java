package org.gvnix.web.report.roo.addon.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.Validate;
import org.springframework.roo.process.manager.MutableFile;

/**
 * Provides an easy way to create a {@link MutableFile} without actually
 * creating a file on the file system. Records all write operations in an
 * OutputStream and makes it available via {@link #getOutputAsString()}.
 * 
 * @author Rossen Stoyanchev
 * @since 1.1.1
 */
public class StubMutableFile implements MutableFile {

    private final File file;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public StubMutableFile() {
        this.file = null;
    }

    public StubMutableFile(File file) {
        Validate.notNull(file, "File required");
        this.file = file;
    }

    public String getCanonicalPath() {
        try {
            return file.getCanonicalPath();
        }
        catch (IOException ioe) {
            throw new IllegalStateException(
                    "Cannot determine canoncial path for '" + file + "'", ioe);
        }
    }

    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        }
        catch (IOException ioe) {
            throw new IllegalStateException(
                    "Unable to acquire input stream for file '"
                            + getCanonicalPath() + "'", ioe);
        }
    }

    public OutputStream getOutputStream() {
        return output;
    }

    public void setDescriptionOfChange(String message) {
    }

    public String getOutputAsString() {
        return output.toString();
    }

}
