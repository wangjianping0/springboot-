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

package org.springframework.bootstrap.service.metrics;

import java.util.Collection;

import org.joda.time.DateTime;

/**
 * @author Dave Syer
 */
public interface MetricRepository {

	void increment(String metricName, int amount, DateTime dateTime);

	void set(String metricName, double value, DateTime dateTime);

	void delete(String metricName);

	Metric findOne(String metricName);

	Collection<Metric> findAll();

}
