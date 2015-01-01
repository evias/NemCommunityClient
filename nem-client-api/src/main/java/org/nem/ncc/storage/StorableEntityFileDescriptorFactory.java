package org.nem.ncc.storage;

import org.eclipse.jetty.util.UrlEncoded;

import java.io.File;
import java.util.function.BiFunction;

/**
 * Factory that creates file-backed storable entity descriptors.
 */
public class StorableEntityFileDescriptorFactory<TEntity extends StorableEntity> implements StorableEntityDescriptorFactory {
	private final File directory;
	private final BiFunction<StorableEntityName, StorableEntityFileExtension, TEntity> entityActivator;

	/**
	 * Creates a new storable entity descriptor factory.
	 *
	 * @param directory The search directory.
	 */
	public StorableEntityFileDescriptorFactory(
			final File directory,
			final BiFunction<StorableEntityName, StorableEntityFileExtension, TEntity> entityActivator) {
		this.directory = directory;
		this.entityActivator = entityActivator;
	}

	@Override
	public StorableEntityDescriptor createNew(final StorableEntityNamePasswordPair pair, final StorableEntityFileExtension fileExtension) {
		final File file = this.createFile(pair.getName(), fileExtension);
		if (file.exists()) {
			throw new StorableEntityStorageException(StorableEntityStorageException.Code.STORABLE_ENTITY_ALREADY_EXISTS);
		}

		final TEntity entity = this.entityActivator.apply(pair.getName(), fileExtension);
		return new StorableEntityFileDescriptor<>(entity, file);
	}

	@Override
	public StorableEntityDescriptor openExisting(final StorableEntityNamePasswordPair pair, final StorableEntityFileExtension fileExtension) {
		final File file = this.createFile(pair.getName(), fileExtension);
		if (!file.exists()) {
			throw new StorableEntityStorageException(StorableEntityStorageException.Code.STORABLE_ENTITY_DOES_NOT_EXIST);
		}

		final TEntity entity = this.entityActivator.apply(pair.getName(), fileExtension);
		return new StorableEntityFileDescriptor<>(entity, file);
	}

	private File createFile(final StorableEntityName name, final StorableEntityFileExtension fileExtension) {
		// this function should be modified if we want to add support for basic import / export
		// (i.e. opening storable entities from and saving them to different, non-default locations)
		// we can add logic here to check if StorableEntityName is really a path and do different things based on that
		return new File(this.directory, this.getStorableEntityFileName(name, fileExtension));
	}

	private String getStorableEntityFileName(final StorableEntityName name, final StorableEntityFileExtension fileExtension) {
		return UrlEncoded.encodeString(name.toString()) + fileExtension.toString();
	}
}
