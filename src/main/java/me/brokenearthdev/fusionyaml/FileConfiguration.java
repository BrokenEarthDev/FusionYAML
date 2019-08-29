/*
Copyright 2019 BrokenEarthDev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package me.brokenearthdev.fusionyaml;

import me.brokenearthdev.fusionyaml.error.YamlException;
import me.brokenearthdev.fusionyaml.io.DefaultParser;
import me.brokenearthdev.fusionyaml.io.YamlParser;
import me.brokenearthdev.fusionyaml.object.YamlElement;
import me.brokenearthdev.fusionyaml.object.YamlObject;
import me.brokenearthdev.fusionyaml.object.YamlPrimitive;
import me.brokenearthdev.fusionyaml.utils.StorageUtils;
import me.brokenearthdev.fusionyaml.utils.YamlUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FileConfiguration implements Configuration {

    private YamlObject object;
    private static final DumperOptions defOptions = defOptions();

    public FileConfiguration(File file) throws IOException, YamlException {
        YamlParser parser = new DefaultParser(file);
        Map<String, Object> map = parser.map();
        if (map == null)
            throw new YamlException("parser map returned null");
        object = new YamlObject(YamlUtils.toMap(map));
    }

    /**
     * This method saves the contents into a {@link File} specified.
     *
     * @param options The {@link DumperOptions}, which contains convenient options to fit your needs
     * @param file    The file that'll be saved to. If the file doesn't exist, the file will
     *  @throws IOException Thrown if any IO error occurred.
     */
    @Override
    public void save(DumperOptions options, @NotNull File file) throws IOException {
        Map<String, Object> map = YamlUtils.toMap0(object);
        Yaml yaml = new Yaml((options != null) ? options : defOptions);
        String data = yaml.dump(map);

        // UTF-8 supports more languages
        FileUtils.writeStringToFile(file, data, Charset.forName(StandardCharsets.UTF_8.name()));
    }

    /**
     * This method saves the contents into a {@link File} specified.
     *
     * @param file The file that'll be saved to. If the file doesn't exist, the file will
     *             be created with the data saved to it.
     * @throws IOException Thrown if any IO error occurred.
     */
    @Override
    public void save(@NotNull File file) throws IOException {
        save(defOptions, file);
    }

    /**
     * Sets the value in the path. If the path didn't exist, a new path will be created with the
     * value set to it. If the path does exist, the value in the path will be changed into the new
     * value provided.
     * <p>
     * If {@code null} is passed as the value, the path will be removed. Doing so is equivalent
     * to calling {@link #removePath(String)}.
     * <p>
     * If an {@link Object} other than the following is passed, the value will be set by converting it
     * to a {@link String} using the {@link #toString()} method present in the {@link Object}'s instance:
     * <ul>
     *     <li>primitive data types</li>
     *     <li>string</li>
     *     <li>maps</li>
     *     <li>lists</li>
     *     <li>YamlElements and its children</li>
     * </ul>
     *  @param path The path to the value
     *
     * @param value The value the path contains
     */
    @Override
    public void set(@NotNull String path, Object value) {
        set(Collections.singletonList(path), value);
    }

    /**
     * Sets the value in the path. If the path didn't exist, a new path will be created with the
     * value set to it. If the path does exist, the value in the path will be changed into the new
     * value provided.
     * <p>
     * The separator is a character that when used, the value will be set under the parent path, which
     * is the part of the path used before the separator. Calling this method while not using a separator
     * in the path is equivalent to calling {@link #set(String, Object)}.
     * <p>
     * If {@code null} is passed as the value, the path will be removed. Doing so is equivalent
     * to calling {@link #removePath(String, char)}.
     * <p>
     * If an {@link Object} other than the following is passed, the value will be set by converting it
     * to a {@link String} using the {@link #toString()} method present in the {@link Object}'s instance:
     * <ul>
     *     <li>primitive data types</li>
     *     <li>string</li>
     *     <li>maps</li>
     *     <li>lists</li>
     *     <li>YamlElements and its children</li>
     * </ul>
     *  @param path The path to the value
     *
     * @param separator The path separator. When used, the value will be set under the parent, which is
     *                  the section before the separator.
     * @param value     The value the path contains
     */
    @Override
    public void set(@NotNull String path, char separator, Object value) {
        set(StorageUtils.toList(path, separator), value);
    }

    /**
     * Sets the value in the path. If the path didn't exist, a new path will be created with the
     * value set to it. If the path does exist, the value in the path will be changed into the new
     * value provided.
     * <p>
     * The method requires a {@link List} to be passed in. In an ascending order, every index in the
     * {@link List} is the child of the previous parent except the first index, which is the uppermost
     * parent.
     * <p>
     * If {@code null} is passed as the value, the path will be removed. Doing so is equivalent
     * to calling {@link #removePath(List)}.
     * <p>
     * If an {@link Object} other than the following is passed, the value will be set by converting it
     * to a {@link String} using the {@link #toString()} method present in the {@link Object}'s instance:
     * <ul>
     *     <li>primitive data types</li>
     *     <li>string</li>
     *     <li>maps</li>
     *     <li>lists</li>
     *     <li>YamlElements and its children</li>
     * </ul>
     *  @param path The path to the value
     *
     * @param value The value the path contains
     */
    @Override
    public void set(@NotNull List<String> path, Object value) {
        if (value instanceof YamlElement) {
            set(path, (YamlElement) value);
            return;
        }
        YamlElement converted = YamlUtils.toElement(value, false);
        YamlElement data = (converted != null) ? converted : new YamlPrimitive(value.toString());
        object.set(path, data);
    }

    /**
     * Sets the value in the path. If the path didn't exist, a new path will be created with the
     * value set to it. If the path does exist, the value in the path will be changed into the new
     * value provided.
     * <p>
     * If {@code null} is passed as the value, the path will be removed. Doing so is equivalent
     * to calling {@link #removePath(String)}.
     *
     * @param path  The path to the value
     * @param value The value the path contains
     */
    @Override
    public void set(@NotNull String path, YamlElement value) {
        set(Collections.singletonList(path), value);
    }

    /**
     * Sets the value in the path. If the path didn't exist, a new path will be created with the
     * value set to it. If the path does exist, the value in the path will be changed into the new
     * value provided.
     * <p>
     * The separator is a character that when used, the value will be set under the parent path, which
     * is the part of the path used before the separator. Calling this method while not using a separator
     * in the path is equivalent to calling {@link #set(String, Object)}.
     * <p>
     * If {@code null} is passed as the value, the path will be removed. Doing so is equivalent
     * to calling {@link #removePath(String, char)}.
     *
     * @param path      The path to the value
     * @param separator The path separator. When used, the value will be set under the parent, which is
     *                  the section before the separator.
     * @param value     The value the path contains
     */
    @Override
    public void set(@NotNull String path, char separator, YamlElement value) {
        set(StorageUtils.toList(path, separator), value);
    }

    /**
     * Sets the value in the path. If the path didn't exist, a new path will be created with the
     * value set to it. If the path does exist, the value in the path will be changed into the new
     * value provided.
     * <p>
     * The method requires a {@link List} to be passed in. In an ascending order, every index in the
     * {@link List} is the child of the previous parent except the first index, which is the uppermost
     * parent.
     * <p>
     * If {@code null} is passed as the value, the path will be removed. Doing so is equivalent
     * to calling {@link #removePath(List)}.
     *
     * @param path  The path to the value
     * @param value The value the path contains
     */
    @Override
    public void set(@NotNull List<String> path, YamlElement value) {
        set(path, (Object) value);
    }

    /**
     * Removes the key-value pair found in the path. A path is essentially a key.
     *
     * @param path The path to the key-value pair.
     */
    @Override
    public void removePath(@NotNull String path) {
        set(path, null);
    }

    /**
     * Removes the key-value pair found in the path. A path is essentially a key.
     * <p>
     * The separator is a character that when used, the value will be set under the parent path, which
     * is the part of the path used before the separator. Calling this method while not using a separator
     * in the path is equivalent to calling {@link #set(String, Object)}.
     *
     * @param path      The path to the key-value pair.
     * @param separator The path separator. When used, the value will be set under the parent, which is
     */
    @Override
    public void removePath(@NotNull String path, char separator) {
        set(StorageUtils.toList(path, separator), null);
    }

    /**
     * Removes the key-value pair found in the path. A path is essentially a key.
     * <p>
     * The method requires a {@link List} to be passed in. In an ascending order, every index in the
     * {@link List} is the child of the previous parent except the first index, which is the uppermost
     * parent.
     *
     * @param path The path to the key-value pair. Every index in the {@link List} is a descent.
     */
    @Override
    public void removePath(@NotNull List<String> path) {
        set(path, null);
    }

    /**
     * @return Gets the {@link YamlObject} for the configuration
     */
    @Override
    public YamlObject getContents() {
        return object;
    }

    /**
     * This method retrieves the {@link Object} in a {@link List} of paths with the specified
     * default value if the {@link Object} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@link Object} found in the given path or the default value if not
     */
    @Override
    public Object getObject(@NotNull List<String> path, Object defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link Object} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@link Object} in the given path is not found, {@code null} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@link Object} found in the given path or {@code null} if the {@link Object}
     * in the given path is not found
     */
    @Override
    public Object getObject(@NotNull List<String> path) {
        return null;
    }

    /**
     * This method retrieves the {@link Object} in a given path with a given default value if the {@link Object}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@link Object} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@link Object} is found
     * @param separator The path separator. When used, the method will look for the {@link Object}
     *                  under the parent.
     * @param defValue  If the {@link Object} is not found, the method will return this value.
     * @return The {@link Object} found in the given path or the default value if not
     */
    @Override
    public Object getObject(@NotNull String path, char separator, Object defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link Object} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@link Object} under the path before
     * the separator is used. If an {@link Object} is not found in the given path, {@code null} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@link Object} is found
     * @param separator The path separator. When used, the method will look for the {@link Object}
     *                  under the parent.
     * @return The {@link Object} found in the given path or {@code null} if otherwise
     */
    @Override
    public Object getObject(@NotNull String path, char separator) {
        return null;
    }

    /**
     * This method retrieves the {@link Object} in a given path with a given default value if the {@link Object}
     * isn't found. The method only works on retrieving the {@link Object} in the uppermost key.
     *
     * @param path     The path where the {@link Object} is found
     * @param defValue If the {@link Object} is not found, the method will return this value.
     * @return The {@link Object} found in the given path or the default value if not
     */
    @Override
    public Object getObject(@NotNull String path, Object defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link Object} in a given path or {@code null} if otherwise.
     * The method only works on retrieving the {@link Object} in the uppermost key.
     *
     * @param path The path where the {@link Object} is found
     * @return The {@link Object} found in the given path or {@code null} if otherwise
     */
    @Override
    public Object getObject(@NotNull String path) {
        return null;
    }

    /**
     * This method retrieves the {@link String} in a {@link List} of paths with the specified
     * default value if the {@link String} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@link String} found in the given path or the default value if not
     */
    @Override
    public String getString(@NotNull List<String> path, String defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link String} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@link String} in the given path is not found, {@code null} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@link String} found in the given path or {@code null} if the {@link String}
     * in the given path is not found
     */
    @Override
    public String getString(@NotNull List<String> path) {
        return null;
    }

    /**
     * This method retrieves the {@link String} in a given path with a given default value if the {@link String}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@link String} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@link String} is found
     * @param separator The path separator. When used, the method will look for the {@link String}
     *                  under the parent.
     * @param defValue  If the {@link String} is not found, the method will return this value.
     * @return The {@link String} found in the given path or the default value if not
     */
    @Override
    public String getString(@NotNull String path, char separator, String defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link String} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@link String} under the path before
     * the separator is used. If a {@link String} is not found in the given path, {@code null} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@link String} is found
     * @param separator The path separator. When used, the method will look for the {@link String}
     *                  under the parent.
     * @return The {@link String} found in the given path or {@code null} if otherwise
     */
    @Override
    public String getString(@NotNull String path, char separator) {
        return null;
    }

    /**
     * This method retrieves the {@link String} in a given path with a given default value if the {@link String}
     * isn't found. The method only works on retrieving the {@link String} in the uppermost key.
     *
     * @param path     The path where the {@link String} is found
     * @param defValue If the {@link String} is not found, the method will return this value.
     * @return The {@link String} found in the given path or the default value if not
     */
    @Override
    public String getString(@NotNull String path, String defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link String} in a given path or {@code null} if the {@link String}
     * isn't found. The method only works on retrieving the {@link String} in the uppermost key.
     *
     * @param path The path where the {@link String} is found
     * @return The {@link String} found in the given path or {@code null} if otherwise
     */
    @Override
    public String getString(@NotNull String path) {
        return null;
    }

    /**
     * This method retrieves the {@link YamlElement} in a {@link List} of paths with the specified
     * default value if the {@link YamlElement} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@link YamlElement} found in the given path or the default value if not
     */
    @Override
    public YamlElement getElement(@NotNull List<String> path, YamlElement defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link YamlElement} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@link YamlElement} in the given path is not found, {@code null} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@link YamlElement} found in the given path or {@code null} if the {@link YamlElement}
     * in the given path is not found
     */
    @Override
    public YamlElement getElement(@NotNull List<String> path) {
        return null;
    }

    /**
     * This method retrieves the {@link YamlElement} in a given path with a given default value if the {@link YamlElement}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@link YamlElement} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getElement(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@link YamlElement} is found
     * @param separator The path separator. When used, the method will look for the {@link YamlElement}
     *                  under the parent.
     * @param defValue  If the {@link YamlElement} is not found, the method will return this value.
     * @return The {@link YamlElement} found in the given path or the default value if not
     */
    @Override
    public YamlElement getElement(@NotNull String path, char separator, YamlElement defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link YamlElement} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@link YamlElement} under the path before
     * the separator is used. If a {@link YamlElement} is not found in the given path, {@code null} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getElement(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@link YamlElement} is found
     * @param separator The path separator. When used, the method will look for the {@link YamlElement}
     *                  under the parent.
     * @return The {@link YamlElement} found in the given path or {@code null} if otherwise
     */
    @Override
    public YamlElement getElement(@NotNull String path, char separator) {
        return null;
    }

    /**
     * This method retrieves the {@link YamlElement} in a given path with a given default value if the {@link YamlElement}
     * isn't found. The method only works on retrieving the {@link YamlElement} in the uppermost key.
     *
     * @param path     The path where the {@link YamlElement} is found
     * @param defValue If the {@link YamlElement} is not found, the method will return this value.
     * @return The {@link YamlElement} found in the given path or the default value if not
     */
    @Override
    public YamlElement getElement(@NotNull String path, YamlElement defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link YamlElement} in a given path or {@code null} if the {@link YamlElement}
     * isn't found. The method only works on retrieving the {@link YamlElement} in the uppermost key.
     *
     * @param path The path where the {@link YamlElement} is found
     * @return The {@link YamlElement} found in the given path or {@code null} if otherwise
     */
    @Override
    public YamlElement getElement(@NotNull String path) {
        return null;
    }

    /**
     * This method retrieves the {@code boolean} in a {@link List} of paths with the specified
     * default value if the {@code boolean} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@code boolean} found in the given path or the default value if not
     */
    @Override
    public boolean getBoolean(@NotNull List<String> path, boolean defValue) {
        return false;
    }

    /**
     * This method retrieves the {@code boolean} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@code boolean} in the given path is not found, {@code false} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@code boolean} found in the given path or {@code false} if the {@code boolean}
     * in the given path is not found
     */
    @Override
    public boolean getBoolean(@NotNull List<String> path) {
        return false;
    }

    /**
     * This method retrieves the {@code boolean} in a given path with a given default value if the {@code boolean}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@code boolean} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code boolean} is found
     * @param separator The path separator. When used, the method will look for the {@code boolean}
     *                  under the parent.
     * @param defValue  If the {@code boolean} is not found, the method will return this value.
     * @return The {@code boolean} found in the given path or the default value if not
     */
    @Override
    public boolean getBoolean(@NotNull String path, char separator, boolean defValue) {
        return false;
    }

    /**
     * This method retrieves the {@code boolean} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@code boolean} under the path before
     * the separator is used. If a {@code boolean} is not found in the given path, {@code false} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code boolean} is found
     * @param separator The path separator. When used, the method will look for the {@code boolean}
     *                  under the parent.
     * @return The {@code boolean} found in the given path or {@code false} if otherwise
     */
    @Override
    public boolean getBoolean(@NotNull String path, char separator) {
        return false;
    }

    /**
     * This method retrieves the {@code boolean} in a given path with a given default value if the {@code boolean}
     * isn't found. The method only works on retrieving the {@code boolean} in the uppermost key.
     *
     * @param path     The path where the {@code boolean} is found
     * @param defValue If the {@code boolean} is not found, the method will return this value.
     * @return The {@code boolean} found in the given path or the default value if not
     */
    @Override
    public boolean getBoolean(@NotNull String path, boolean defValue) {
        return false;
    }

    /**
     * This method retrieves the {@code boolean} in a given path with a given default value if the {@code boolean}
     * isn't found. The method only works on retrieving the {@code boolean} in the uppermost key.
     *
     * @param path The path where the {@code boolean} is found
     * @return The {@code byte} found in the given path or {@code false} if otherwise
     */
    @Override
    public boolean getBoolean(@NotNull String path) {
        return false;
    }

    /**
     * This method retrieves the {@code byte} in a {@link List} of paths with the specified
     * default value if the {@code byte} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@code byte} found in the given path or the default value if not
     */
    @Override
    public byte getByte(@NotNull List<String> path, byte defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code byte} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@code byte} in the given path is not found, {@code 0} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@code byte} found in the given path or {@code 0} if the {@code byte}
     * in the given path is not found
     */
    @Override
    public byte getByte(@NotNull List<String> path) {
        return 0;
    }

    /**
     * This method retrieves the {@code byte} in a given path with a given default value if the {@code byte}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@code byte} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code byte} is found
     * @param separator The path separator. When used, the method will look for the {@code byte}
     *                  under the parent.
     * @param defValue  If the {@code byte} is not found, the method will return this value.
     * @return The {@code byte} found in the given path or the default value if not
     */
    @Override
    public byte getByte(@NotNull String path, char separator, byte defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code byte} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@code byte} under the path before
     * the separator is used. If a {@code byte} is not found in the given path, {@code 0} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code byte} is found
     * @param separator The path separator. When used, the method will look for the {@code byte}
     *                  under the parent.
     * @return The {@code byte} found in the given path or {@code 0} if otherwise
     */
    @Override
    public byte getByte(@NotNull String path, char separator) {
        return 0;
    }

    /**
     * This method retrieves the {@code byte} in a given path with a given default value if the {@code byte}
     * isn't found. The method only works on retrieving the {@code byte} in the uppermost key.
     *
     * @param path     The path where the {@code byte} is found
     * @param defValue If the {@code byte} is not found, the method will return this value.
     * @return The {@code byte} found in the given path or the default value if not
     */
    @Override
    public byte getByte(@NotNull String path, byte defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code byte} in a given path with a given default value if the {@code byte}
     * isn't found. The method only works on retrieving the {@code byte} in the uppermost key.
     *
     * @param path The path where the {@code byte} is found
     * @return The {@code byte} found in the given path or {@code 0} if otherwise
     */
    @Override
    public byte getByte(@NotNull String path) {
        return 0;
    }

    /**
     * This method retrieves the {@code short} in a {@link List} of paths with the specified
     * default value if the {@code short} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@code short} found in the given path or the default value if not
     */
    @Override
    public short getShort(@NotNull List<String> path, short defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code short} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@code short} in the given path is not found, {@code 0} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@code short} found in the given path or {@code 0} if the {@code short}
     * in the given path is not found
     */
    @Override
    public short getShort(@NotNull List<String> path) {
        return 0;
    }

    /**
     * This method retrieves the {@code short} in a given path with a given default value if the {@code short}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@code short} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code short} is found
     * @param separator The path separator. When used, the method will look for the {@code short}
     *                  under the parent.
     * @param defValue  If the {@code short} is not found, the method will return this value.
     * @return The {@code short} found in the given path or the default value if not
     */
    @Override
    public short getShort(@NotNull String path, char separator, short defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code short} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@code short} under the path before
     * the separator is used. If a {@code short} is not found in the given path, {@code 0} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code short} is found
     * @param separator The path separator. When used, the method will look for the {@code short}
     *                  under the parent.
     * @return The {@code short} found in the given path or {@code 0} if otherwise
     */
    @Override
    public short getShort(@NotNull String path, char separator) {
        return 0;
    }

    /**
     * This method retrieves the {@code short} in a given path with a given default value if the {@code short}
     * isn't found. The method only works on retrieving the {@code short} in the uppermost key.
     *
     * @param path     The path where the {@code short} is found
     * @param defValue If the {@code short} is not found, the method will return this value.
     * @return The {@code short} found in the given path or the default value if not
     */
    @Override
    public short getShort(@NotNull String path, short defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code short} in a given path with a given default value if the {@code short}
     * isn't found. The method only works on retrieving the {@code short} in the uppermost key.
     *
     * @param path The path where the {@code short} is found
     * @return The {@code short} found in the given path or {@code 0} if otherwise
     */
    @Override
    public short getShort(@NotNull String path) {
        return 0;
    }

    /**
     * This method retrieves the {@code float} in a {@link List} of paths with the specified
     * default value if the {@code float} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@code float} found in the given path or the default value if not
     */
    @Override
    public float getFloat(@NotNull List<String> path, float defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code float} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@code float} in the given path is not found, {@code 0} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@code float} found in the given path or {@code 0} if the {@code float}
     * in the given path is not found
     */
    @Override
    public float getFloat(@NotNull List<String> path) {
        return 0;
    }

    /**
     * This method retrieves the {@code float} in a given path with a given default value if the {@code float}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@code float} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code float} is found
     * @param separator The path separator. When used, the method will look for the {@code float}
     *                  under the parent.
     * @param defValue  If the {@code float} is not found, the method will return this value.
     * @return The {@code float} found in the given path or the default value if not
     */
    @Override
    public float getFloat(@NotNull String path, char separator, float defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code float} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@code float} under the path before
     * the separator is used. If a {@code float} is not found in the given path, {@code 0} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code float} is found
     * @param separator The path separator. When used, the method will look for the {@code float}
     *                  under the parent.
     * @return The {@code float} found in the given path or {@code 0} if otherwise
     */
    @Override
    public float getFloat(@NotNull String path, char separator) {
        return 0;
    }

    /**
     * This method retrieves the {@code float} in a given path with a given default value if the {@code float}
     * isn't found. The method only works on retrieving the {@code float} in the uppermost key.
     *
     * @param path     The path where the {@code float} is found
     * @param defValue If the {@code float} is not found, the method will return this value.
     * @return The {@code float} found in the given path or the default value if not
     */
    @Override
    public float getFloat(@NotNull String path, float defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code float} in a given path with a given default value if the {@code float}
     * isn't found. The method only works on retrieving the {@code float} in the uppermost key.
     *
     * @param path The path where the {@code float} is found
     * @return The {@code float} found in the given path or {@code 0} if otherwise
     */
    @Override
    public float getFloat(@NotNull String path) {
        return 0;
    }

    /**
     * This method retrieves the {@code double} in a {@link List} of paths with the specified
     * default value if the {@code double} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@code double} found in the given path or the default value if not
     */
    @Override
    public double getDouble(@NotNull List<String> path, double defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code double} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@code double} in the given path is not found, {@code 0} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@code double} found in the given path or {@code 0} if the {@code double}
     * in the given path is not found
     */
    @Override
    public double getDouble(@NotNull List<String> path) {
        return 0;
    }

    /**
     * This method retrieves the {@code double} in a given path with a given default value if the {@code double}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@code double} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code double} is found
     * @param separator The path separator. When used, the method will look for the {@code double}
     *                  under the parent.
     * @param defValue  If the {@code double} is not found, the method will return this value.
     * @return The {@code double} found in the given path or the default value if not
     */
    @Override
    public double getDouble(@NotNull String path, char separator, double defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code double} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@code double} under the path before
     * the separator is used. If a {@code double} is not found in the given path, {@code 0} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code double} is found
     * @param separator The path separator. When used, the method will look for the {@code double}
     *                  under the parent.
     * @return The {@code double} found in the given path or {@code 0} if otherwise
     */
    @Override
    public double getDouble(@NotNull String path, char separator) {
        return 0;
    }

    /**
     * This method retrieves the {@code double} in a given path with a given default value if the {@code double}
     * isn't found. The method only works on retrieving the {@code double} in the uppermost key.
     *
     * @param path     The path where the {@code double} is found
     * @param defValue If the {@code double} is not found, the method will return this value.
     * @return The {@code double} found in the given path or the default value if not
     */
    @Override
    public double getDouble(@NotNull String path, double defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code double} in a given path with a given default value if the {@code double}
     * isn't found. The method only works on retrieving the {@code double} in the uppermost key.
     *
     * @param path The path where the {@code double} is found
     * @return The {@code double} found in the given path or {@code 0} if otherwise
     */
    @Override
    public double getDouble(@NotNull String path) {
        return 0;
    }

    /**
     * This method retrieves the {@code int} in a {@link List} of paths with the specified
     * default value if the {@code int} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@code int} found in the given path or the default value if not
     */
    @Override
    public int getInt(@NotNull List<String> path, int defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code int} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@code int} in the given path is not found, {@code 0} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@code int} found in the given path or {@code 0} if the {@code int}
     * in the given path is not found
     */
    @Override
    public int getInt(@NotNull List<String> path) {
        return 0;
    }

    /**
     * This method retrieves the {@code int} in a given path with a given default value if the {@code int}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@code int} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code int} is found
     * @param separator The path separator. When used, the method will look for the {@code int}
     *                  under the parent.
     * @param defValue  If the {@code int} is not found, the method will return this value.
     * @return The {@code int} found in the given path or the default value if not
     */
    @Override
    public int getInt(@NotNull String path, char separator, int defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code int} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@code int} under the path before
     * the separator is used. If a {@code int} is not found in the given path, {@code 0} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code int} is found
     * @param separator The path separator. When used, the method will look for the {@code int}
     *                  under the parent.
     * @return The {@code int} found in the given path or {@code 0} if otherwise
     */
    @Override
    public int getInt(@NotNull String path, char separator) {
        return 0;
    }

    /**
     * This method retrieves the {@code int} in a given path with a given default value if the {@code int}
     * isn't found. The method only works on retrieving the {@code int} in the uppermost key.
     *
     * @param path     The path where the {@code int} is found
     * @param defValue If the {@code int} is not found, the method will return this value.
     * @return The {@code int} found in the given path or the default value if not
     */
    @Override
    public int getInt(@NotNull String path, int defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code int} in a given path with a given default value if the {@code int}
     * isn't found. The method only works on retrieving the {@code int} in the uppermost key.
     *
     * @param path The path where the {@code int} is found
     * @return The {@code int} found in the given path or {@code 0} if otherwise
     */
    @Override
    public int getInt(@NotNull String path) {
        return 0;
    }

    /**
     * This method retrieves the {@code long} in a {@link List} of paths with the specified
     * default value if the {@code long} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@code long} found in the given path or the default value if not
     */
    @Override
    public long getLong(@NotNull List<String> path, long defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code long} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@code long} in the given path is not found, {@code 0} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@code long} found in the given path or {@code 0} if the {@code long}
     * in the given path is not found
     */
    @Override
    public long getLong(@NotNull List<String> path) {
        return 0;
    }

    /**
     * This method retrieves the {@code long} in a given path with a given default value if the {@code long}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@code long} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code long} is found
     * @param separator The path separator. When used, the method will look for the {@code long}
     *                  under the parent.
     * @param defValue  If the {@code long} is not found, the method will return this value.
     * @return The {@code long} found in the given path or the default value if not
     */
    @Override
    public long getLong(@NotNull String path, char separator, long defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code long} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@code long} under the path before
     * the separator is used. If a {@code long} is not found in the given path, {@code 0} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@code long} is found
     * @param separator The path separator. When used, the method will look for the {@code long}
     *                  under the parent.
     * @return The {@code long} found in the given path or {@code 0} if otherwise
     */
    @Override
    public long getLong(@NotNull String path, char separator) {
        return 0;
    }

    /**
     * This method retrieves the {@code long} in a given path with a given default value if the {@code long}
     * isn't found. The method only works on retrieving the {@code long} in the uppermost key.
     *
     * @param path     The path where the {@code long} is found
     * @param defValue If the {@code long} is not found, the method will return this value.
     * @return The {@code long} found in the given path or the default value if not
     */
    @Override
    public long getLong(@NotNull String path, long defValue) {
        return 0;
    }

    /**
     * This method retrieves the {@code long} in a given path with a given default value if the {@code long}
     * isn't found. The method only works on retrieving the {@code long} in the uppermost key.
     *
     * @param path The path where the {@code long} is found
     * @return The {@code long} found in the given path or {@code 0} if otherwise
     */
    @Override
    public long getLong(@NotNull String path) {
        return 0;
    }

    /**
     * This method retrieves the {@link List} in a {@link List} of paths with the specified
     * default value if the {@link List} in the path didn't exist. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     *
     * @param path     The path to the value. Every index is a child of the previous index
     *                 except at index {@code 0}
     * @param defValue The default value if the value in the given path doesn't exist
     * @return The {@link List} found in the given path or the default value if not
     */
    @Override
    public List getList(@NotNull List<String> path, List defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link List} in a {@link List} of paths. Every index in the
     * list represents a parent that has one or more children excluding the last index.
     * If the {@link List} in the given path is not found, {@code null} is returned.
     *
     * @param path The path to the value. Every index is a child of the previous index
     *             except at index {@code 0}
     * @return The {@link List} found in the given path or {@code null} if the {@link List}
     * in the given path is not found
     */
    @Override
    public List getList(@NotNull List<String> path) {
        return null;
    }

    /**
     * This method retrieves the {@link List} in a given path with a given default value if the {@link List}
     * isn't found. The separator is a {@code char} when used, it is a descent, and therefore, the method
     * will look for the {@link List} under the path before the separator is used.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@link List} is found
     * @param separator The path separator. When used, the method will look for the {@link List}
     *                  under the parent.
     * @param defValue  If the {@link List} is not found, the method will return this value.
     * @return The {@link List} found in the given path or the default value if not
     */
    @Override
    public List getList(@NotNull String path, char separator, List defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link List} in a given path. The separator is a {@code char} when used,
     * it is a descent, and therefore, the method will look for the {@link List} under the path before
     * the separator is used. If an {@link List} is not found in the given path, {@code null} is returned.
     * <p>
     * For example, trying to retrieve the value of "player.stats.wins" is equal to calling
     * {@link #getObject(List)} with a {@link List} with 3 indexes, namely player, stats, and wins,
     * in its parameter.
     *
     * @param path      The path where the {@link List} is found
     * @param separator The path separator. When used, the method will look for the {@link List}
     *                  under the parent.
     * @return The {@link List} found in the given path or {@code null} if otherwise
     */
    @Override
    public List getList(@NotNull String path, char separator) {
        return null;
    }

    /**
     * This method retrieves the {@link List} in a given path with a given default value if the {@link List}
     * isn't found. The method only works on retrieving the {@link List} in the uppermost key.
     *
     * @param path     The path where the {@link List} is found
     * @param defValue If the {@link List} is not found, the method will return this value.
     * @return The {@link List} found in the given path or the default value if not
     */
    @Override
    public List getList(@NotNull String path, List defValue) {
        return null;
    }

    /**
     * This method retrieves the {@link List} in a given path or {@code null} if otherwise.
     * The method only works on retrieving the {@link List} in the uppermost key.
     *
     * @param path The path where the {@link List} is found
     * @return The {@link List} found in the given path or {@code null} if otherwise
     */
    @Override
    public List getList(@NotNull String path) {
        return null;
    }

    private static DumperOptions defOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return options;
    }

}