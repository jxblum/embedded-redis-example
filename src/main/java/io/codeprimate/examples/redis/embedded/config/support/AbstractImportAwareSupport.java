/*
 *  Copyright 2024 Author or Authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.codeprimate.examples.redis.embedded.config.support;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Abstract Spring {@link ImportAware} implementation supporting configuration through {@link Annotation} metadata.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.core.annotation.AnnotationAttributes
 * @see org.springframework.core.type.AnnotationMetadata
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractImportAwareSupport<T extends Annotation> implements ImportAware {

	private final AtomicReference<Class<T>> resolvedAnnotationTypeReference = new AtomicReference<>(null);

	protected void assertAnnotationPresent(AnnotationMetadata metadata) {
		Assert.state(isAnnotationPresent(metadata), () -> "Annotation [%s] was not declared"
			.formatted(getAnnotationName()));
	}

	protected boolean isAnnotationPresent(AnnotationMetadata metadata) {
		String annotationName = getAnnotationName();
		return metadata.hasAnnotation(annotationName);
	}

	protected AnnotationAttributes getAnnotationAttributes(AnnotationMetadata metadata) {
		assertAnnotationPresent(metadata);
		String annotationName = getAnnotationName();
		return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationName));
	}

	protected String getAnnotationName() {
		return getAnnotationType().getName();
	}

	@SuppressWarnings("all")
	protected Class<T> getAnnotationType() {

		Supplier<Class<T>> resolvingAnnotationTypeSupplier = () -> {

			List<TypeInformation<?>> typeArguments = TypeInformation.of(getClass())
				.getRequiredSuperTypeInformation(AbstractImportAwareSupport.class)
				.getTypeArguments();

			Assert.isTrue(typeArguments.size() == 1, () ->
				"Type arguments [%s] of class [%s] require an Annotation type enabling this configuration"
					.formatted(typeArguments, ClassUtils.getUserClass(getClass())));

			TypeInformation annotationTypeArgument = typeArguments.get(0);

			Class<T> annotationType = (Class<T>) annotationTypeArgument.toTypeDescriptor()
				.getResolvableType()
				.getRawClass();

			return annotationType;
		};

		return this.resolvedAnnotationTypeReference.updateAndGet(annotationType ->
			annotationType != null ? annotationType : resolvingAnnotationTypeSupplier.get());
	}
}
