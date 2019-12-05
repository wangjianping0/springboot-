/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.bootstrap.bind;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.validation.DataBinder;

/**
 * A {@link PropertyValues} implementation backed by a {@link PropertySources}, bridging
 * the two abstractions and allowing (for instance) a regular {@link DataBinder} to be
 * used with the latter.
 * 
 * @author Dave Syer
 */
public class PropertySourcesPropertyValues implements PropertyValues {

	private Map<String, PropertyValue> propertyValues = new ConcurrentHashMap<String, PropertyValue>();

	private PropertySources propertySources;

	/**
	 * Create a new PropertyValues from the given PropertySources
	 * 
	 * @param propertySources a PropertySources instance
	 */
	public PropertySourcesPropertyValues(PropertySources propertySources) {
		this.propertySources = propertySources;
		// TODO: maybe lazy initialization?
		PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(
				propertySources);
		for (PropertySource<?> source : propertySources) {
			if (source instanceof EnumerablePropertySource) {
				EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
				if (enumerable.getPropertyNames().length > 0) {
					for (String propertyName : enumerable.getPropertyNames()) {
						Object value = resolver.getProperty(propertyName);
						this.propertyValues.put(propertyName, new PropertyValue(
								propertyName, value));
					}
				}
			}
		}
	}

	@Override
	public PropertyValue[] getPropertyValues() {
		Collection<PropertyValue> values = this.propertyValues.values();
		return values.toArray(new PropertyValue[values.size()]);
	}

	@Override
	public PropertyValue getPropertyValue(String propertyName) {
		PropertyValue propertyValue = this.propertyValues.get(propertyName);
		if (propertyValue != null) {
			return propertyValue;
		}
		for (PropertySource<?> source : this.propertySources) {
			Object value = source.getProperty(propertyName);
			if (value != null) {
				propertyValue = new PropertyValue(propertyName, value);
				this.propertyValues.put(propertyName, propertyValue);
				return propertyValue;
			}
		}
		return null;
	}

	@Override
	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		// for each property value in the new set
		for (PropertyValue newPv : getPropertyValues()) {
			// if there wasn't an old one, add it
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null) {
				changes.addPropertyValue(newPv);
			} else if (!pvOld.equals(newPv)) {
				// it's changed
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}

	@Override
	public boolean contains(String propertyName) {
		return getPropertyValue(propertyName) != null;
	}

	@Override
	public boolean isEmpty() {
		return this.propertyValues.isEmpty();
	}

}
