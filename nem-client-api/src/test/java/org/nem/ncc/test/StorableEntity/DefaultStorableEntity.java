package org.nem.ncc.test.StorableEntity;

import org.nem.core.model.primitive.BlockHeight;
import org.nem.core.serialization.*;
import org.nem.ncc.storable.entity.*;

import java.util.*;

/**
 * Default implementation of a storable entity with object deserialization.
 */
public class DefaultStorableEntity implements StorableEntity, ObjectDeserializer<DefaultStorableEntity> {
	/**
	 * The default file extension for the DefaultStorableEntity class.
	 */
	public static final String DEFAULT_FILE_EXTENSION = ".bar";

	private final List<BlockHeight> heights;
	private final StorableEntityName name;
	private final StorableEntityFileExtension fileExtension;
	private final String label;

	public DefaultStorableEntity(final StorableEntityName name, final StorableEntityFileExtension fileExtension) {
		this.heights = new ArrayList<>();
		this.name = name;
		this.fileExtension = fileExtension;
		this.label = "defaultStorableEntity";
	}

	public DefaultStorableEntity(final StorableEntityName name) {
		this.heights = new ArrayList<>();
		this.name = name;
		this.fileExtension = getDefaultFileExtension();
		this.label = "defaultStorableEntity";
	}

	public DefaultStorableEntity(final Deserializer deserializer) {
		this.fileExtension = getDefaultFileExtension();
		this.label = "defaultStorableEntity";
		this.name = StorableEntityName.readFrom(deserializer, this.label);
		this.heights = deserializer.readObjectArray("heights", BlockHeight::new);
	}

	public void addEntries(final int count) {
		for (int i = 1; i <= count; i++) {
			this.heights.add(new BlockHeight(i));
		}
	}

	/**
	 * Gets a value indicating whether the extension is the default extension.
	 *
	 * @return true if the extension is the default extension, false otherwise.
	 */
	public boolean isDefaultExtension() {
		return this.toString().toLowerCase().equals(DEFAULT_FILE_EXTENSION);
	}

	/**
	 * Gets the default file extension.
	 *
	 * @return The file extension.
	 */
	public static StorableEntityFileExtension getDefaultFileExtension() {
		return new StorableEntityFileExtension(DEFAULT_FILE_EXTENSION);
	}

	/**
	 * Gets a value indicating whether the supplies file name is valid and has the default extension.
	 *
	 * @param fileName The file name.
	 * @return true if the file name is valid and has the default extension, false otherwise.
	 */
	public static boolean isValidAndHasDefaultExtension(final String fileName) {
		return fileName.toLowerCase().endsWith(DEFAULT_FILE_EXTENSION) && fileName.indexOf(".") > 0;
	}

	@Override
	public DefaultStorableEntity deserialize(final Deserializer deserializer) {
		return new DefaultStorableEntity(deserializer);
	}

	@Override
	public StorableEntityName getName() {
		return this.name;
	}

	@Override
	public StorableEntityFileExtension getFileExtension() {
		return this.fileExtension;
	}

	@Override
	public void serialize(final Serializer serializer) {
		StorableEntityName.writeTo(serializer, this.label, this.name);
		serializer.writeObjectArray("heights", this.heights);
	}

	/**
	 * TODO 20140101 BR: Would probably be better to check equivalence.
	 * Checks for equality.
	 *
	 * @param entity The object to check for equality.
	 * @return true if the entities are equal, false otherwise.
	 */
	public boolean isEqual(final Object entity) {
		if (!(entity instanceof DefaultStorableEntity)) {
			throw new RuntimeException("can only check equality of DefaultStorableEntity objects");
		}

		final DefaultStorableEntity rhs = (DefaultStorableEntity)entity;
		return this.name.equals(rhs.name) &&
				this.fileExtension.equals(rhs.fileExtension) &&
				this.label.equals(rhs.label) &&
				this.heights.equals(rhs.heights);
	}
}