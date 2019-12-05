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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.time.DateTime;

/**
 * @author Dave Syer
 */
public class InMemoryMetricRepository implements MetricRepository {

	private ConcurrentMap<String, Measurement> metrics = new ConcurrentHashMap<String, Measurement>();

	@Override
	public void increment(String metricName, int amount, DateTime dateTime) {
		Measurement current = this.metrics.get(metricName);
		if (current != null) {
			Metric metric = current.getMetric();
			this.metrics.replace(metricName, current,
					new Measurement(dateTime, metric.increment(amount)));
		} else {
			this.metrics.putIfAbsent(metricName, new Measurement(dateTime, new Metric(
					metricName, amount)));
		}
	}

	@Override
	public void set(String metricName, double value, DateTime dateTime) {
		Measurement current = this.metrics.get(metricName);
		if (current != null) {
			Metric metric = current.getMetric();
			this.metrics.replace(metricName, current,
					new Measurement(dateTime, metric.set(value)));
		} else {
			this.metrics.putIfAbsent(metricName, new Measurement(dateTime, new Metric(
					metricName, value)));
		}
	}

	@Override
	public void delete(String metricName) {
		this.metrics.remove(metricName);
	}

	@Override
	public Metric findOne(String metricName) {
		if (this.metrics.containsKey(metricName)) {
			return this.metrics.get(metricName).getMetric();
		}
		return new Metric(metricName, 0);
	}

	@Override
	public Collection<Metric> findAll() {
		ArrayList<Metric> result = new ArrayList<Metric>();
		for (Measurement measurement : this.metrics.values()) {
			result.add(measurement.getMetric());
		}
		return result;
	}

}
