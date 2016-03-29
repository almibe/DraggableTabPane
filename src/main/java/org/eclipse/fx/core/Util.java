/*******************************************************************************
 * Copyright (c) 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.fx.core.function.ExExecutor;
import org.eclipse.fx.core.internal.JavaDSServiceProcessor;

/**
 * Class with static utility methods
 */
public class Util {
    /**
     * @return <code>true</code> if we are on JavaFX 2
     */
    public static boolean isFX2() {
        return System.getProperty("javafx.version") != null && System.getProperty("javafx.version").startsWith("2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * @return <code>true</code> if we are on JavaFX 9
     * @since 2.2.0
     */
    public static boolean isFX9() {
        return System.getProperty("javafx.version") != null && System.getProperty("javafx.version").startsWith("9"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    /**
     * @return <code>true</code> if we are on JavaFX 8
     * @since 2.2.0
     */
    public static boolean isFX8() {
        return System.getProperty("javafx.version") != null && System.getProperty("javafx.version").startsWith("8"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    /**
     * Make use the value is not null
     *
     * @param value
     *            the nullable value
     * @param defaultValue
     *            the default if the value is null
     * @return a nonnull string
     * @since 2.0
     */
    public static String notNull(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static Boolean isOSGi;

    /**
     * @return <code>true</code> if running on OSGi
     */
    public static boolean isOsgiEnv() {
        if (isOSGi == null) {
            isOSGi = Boolean.FALSE;
            try {
                Class.forName("org.osgi.framework.FrameworkUtil"); //$NON-NLS-1$
                if (org.osgi.framework.FrameworkUtil.getBundle(Util.class) != null) {
                    isOSGi = Boolean.TRUE;
                }
            } catch (Throwable e) {
                // nothing
            }
        }
        return isOSGi.booleanValue();
    }

    private static <S> S _lookupService(Class<?> requestor, Class<S> serviceClass) {
        List<S> _lookupServiceList = _lookupServiceList(requestor, serviceClass);
        if (!_lookupServiceList.isEmpty()) {
            return _lookupServiceList.get(0);
        }
        return null;
    }

    private static <S> List<S> _lookupServiceList(Class<?> requestor,
                                                                    Class<S> serviceClass) {
        if (isOsgiEnv()) {
            return OSGiUtil.lookupServiceList(requestor, serviceClass);
        } else {
            return JavaDSServiceProcessor.lookupServiceList(requestor, serviceClass);
        }
    }

    /**
     * Look up the service with the given type
     *
     * @param requestor
     *            the class requesting the service
     *
     * @param serviceClass
     *            the service class type
     * @return the service with the highest rank or <code>null</code>
     * @since 1.2
     */
    public static <S> S lookupService(Class<?> requestor, Class<S> serviceClass) {
        return _lookupService(requestor, serviceClass);
    }

    /**
     * Look up the service with the given type
     *
     * @param serviceClass
     *            the service class type
     * @return the service with the highest rank or <code>null</code>
     * @since 1.2
     */
    public static <S> S lookupService(Class<S> serviceClass) {
        return _lookupService(null, serviceClass);
    }

    /**
     * Lookup the service with the given type
     *
     * @param serviceClass
     *            the service class type
     * @return the service with the highest rank as an optional
     * @since 2.2.0
     */
    @SuppressWarnings("null")
    public static <S> Optional<S> getService(Class<S> serviceClass) {
        @Nullable
        S v = _lookupService(null, serviceClass);
        if (v == null) {
            return Optional.empty();
        } else {
            return Optional.of(v);
        }
    }

    /**
     * Look up the service with the given type
     *
     * @param requestor
     *            the class requesting the service
     *
     * @param serviceClass
     *            the service class type
     * @return the service with the highest rank or <code>null</code>
     * @since 2.2.0
     */
    @SuppressWarnings("null")
    public static <S> Optional<S> getService(Class<?> requestor, Class<S> serviceClass) {
        @Nullable
        S v = _lookupService(requestor, serviceClass);
        if (v == null) {
            return Optional.empty();
        } else {
            return Optional.of(v);
        }
    }

    /**
     * Look up all service with the given type
     *
     * @param requestor
     *            the class requesting the service
     *
     * @param serviceClass
     *            the service class type
     * @return the service with the highest rank or <code>null</code>
     * @since 1.2
     */
    public static <S> List<S> lookupServiceList(Class<?> requestor,
                                                                  Class<S> serviceClass) {
        return _lookupServiceList(requestor, serviceClass);
    }

    /**
     * Look up all service with the given type
     *
     * @param serviceClass
     *            the service class type
     * @return the service with the highest rank or <code>null</code>
     * @since 1.2
     */
    public static <S> List<S> lookupServiceList(Class<S> serviceClass) {
        return _lookupServiceList(null, serviceClass);
    }

    /**
     * Read the content for the given path
     *
     * @param path
     *            the path
     * @return the content
     * @throws IOException
     *             if an I/O error occurs
     * @since 2.0
     */
    public static String slurpFileContent(Path path) throws IOException {
        byte[] buf = new byte[(int) Files.size(path)];

        try (InputStream in = Files.newInputStream(path)) {
            in.read(buf);
            return new String(buf);
        }
    }

    /**
     * Read the input stream into a string
     *
     * @param in
     *            the stream
     * @param charset
     *            the charset to be used
     * @return the string
     */
    public static String readToString(InputStream in, Charset charset) {
        return readToString(in, 1024, charset);
    }

    /**
     * Read the input stream into a string
     *
     * @param in
     *            the stream
     * @param bufferLength
     *            the buffer length
     * @param charset
     *            the charset
     * @return the string
     */
    public static String readToString(InputStream in, int bufferLength, Charset charset) {
        StringBuilder b = new StringBuilder();
        char[] buf = new char[bufferLength];
        InputStreamReader r = new InputStreamReader(in, charset);
        int l;
        try {
            while ((l = r.read(buf, 0, bufferLength)) != -1) {
                b.append(buf, 0, l);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return b.toString();
    }

    /**
     * Copy the complete input stream to an output stream
     *
     * @param sourceStream
     *            the source stream
     * @param targetStream
     *            the output stream
     * @throws IOException
     *             if something is going wrong
     * @since 2.3.0
     */
    public static void copyToStream(InputStream sourceStream, OutputStream targetStream) throws IOException {
        byte[] buf = new byte[1024];
        int l;
        while ((l = sourceStream.read(buf)) != -1) {
            targetStream.write(buf, 0, l);
        }
    }

    /**
     * Zip up a complete directory with all the sub directories
     *
     * @param dir
     *            the directory to zip
     * @param zipFile
     *            the zip file or <code>null</code> if you want a temporary zip
     *            file to be created
     * @return the directory
     * @throws IOException
     *             if somethings going wrong
     * @since 2.3.0
     */
    @SuppressWarnings("null")
    public static Path zipDirectory(Path dir, Path zipFile) throws IOException {
        Path target = zipFile;
        if (target == null) {
            target = Files.createTempFile("generated-zip", ".zip"); //$NON-NLS-1$//$NON-NLS-2$
        }

        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(target))) {
            for (Path c : Files.list(dir).collect(Collectors.toList())) {
                if (!c.equals(target)) {
                    addEntry(out, dir, c);
                }
            }

            out.close();
        }

        return target;
    }

    private static void addEntry(ZipOutputStream out, Path rootPath, Path p) throws IOException {
        if (Files.isDirectory(p)) {
            for (Path c : Files.list(p).collect(Collectors.toList())) {
                addEntry(out, rootPath, c);
            }
        } else {
            ZipEntry e = new ZipEntry(rootPath.relativize(p).toString());
            out.putNextEntry(e);
            try (InputStream s = Files.newInputStream(p)) {
                copyToStream(s, out);
            }
            out.closeEntry();
        }
    }

    /**
     * @return <code>true</code> if running on OS-X
     * @since 2.2.0
     */
    public static boolean isMacOS() {
        return "Mac OS X".equals(System.getProperty("os.name")); //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * @return <code>true</code> if running on windows
     * @since 2.2.0
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");  //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Constraint the given value to the upper and lower bound
     *
     * @param v
     *            the value to constraint
     * @param min
     *            the lower bound (only values >= 0 are applied)
     * @param max
     *            the upper bound (only values >= 0 are applied)
     * @return the value
     * @since 2.2.0
     */
    public static double unsignedConstraintValue(double v, double min, double max) {
        double rv = v;
        if (min >= 0) {
            rv = Math.max(rv, min);
        }

        if (max >= 0) {
            rv = Math.min(rv, max);
        }
        return rv;
    }

    /**
     * Helper method allowing to insert temporary debug information in an
     * {@link Stream} mapping
     *
     * @param data
     *            the data
     * @return the data
     * @since 2.2.0
     */
    public static <T> T debugStderr(T data) {
        System.err.println(data);
        return data;
    }

    /**
     * Convert an URL to a path on the local filesystem
     *
     * @param url
     *            the url
     * @param copyIfNeeded
     *            <code>true</code> if the url can not be converted to a local
     *            the content is copied to the local filesystem
     * @return the path
     * @since 2.2.0
     */
    public static Optional<Resource<Path>> getLocalPath(URL url, boolean copyIfNeeded) {
        return lookupServiceList(URLResolver.class).stream().filter(r -> r.test(url)).findFirst()
        .map(r -> Optional.of(Resource.createResource(r.resolveToLocalPath(url))))
        .orElseGet(() -> copyIfNeeded
        ? ExExecutor.executeSupplier(() -> Util.copyToTempFile(url), "Unable to copy resource") //$NON-NLS-1$
        : Optional.empty());
    }

    /**
     * Convert an URL to a path on the local filesystem
     *
     * @param url
     *            the url
     * @return the path
     * @since 2.2.0
     */
    public static Optional<URL> getLocalURL(URL url) {
        return lookupServiceList(URLResolver.class).stream().filter(r -> r.test(url)).findFirst()
        .map(r -> r.resolveToLocalURL(url));
    }

    private static Resource<Path> copyToTempFile(URL url) throws IOException {
        Path path = Files.createTempFile("tmp", Paths.get(url.getPath()).getFileName().toString()); //$NON-NLS-1$

        try (InputStream stream = url.openStream()) {
            Files.copy(stream, path);
        }

        if (path == null) {
            return null;
        }

        return Resource.createTempResource(path);
    }

    /**
     * Create a new URL instance from the provide value
     *
     * @param url
     *            the url
     * @return the url instance
     * @throws IllegalArgumentException
     *             if something wrong with the URL provided
     * @since 2.3.0
     */
    public static URL createUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
