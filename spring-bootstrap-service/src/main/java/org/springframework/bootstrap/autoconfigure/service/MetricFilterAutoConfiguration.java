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

package org.springframework.bootstrap.autoconfigure.service;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.bootstrap.context.annotation.ConditionalOnBean;
import org.springframework.bootstrap.context.annotation.ConditionalOnClass;
import org.springframework.bootstrap.context.annotation.EnableAutoConfiguration;
import org.springframework.bootstrap.service.metrics.CounterService;
import org.springframework.bootstrap.service.metrics.GaugeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.UrlPathHelper;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for service apps.
 * 
 * @author Dave Syer
 */
@Configuration
// FIXME: make this conditional
// @ConditionalOnBean({ CounterService.class, GaugeService.class })
@ConditionalOnClass({ Servlet.class })
public class MetricFilterAutoConfiguration {

	@Autowired(required = false)
	private CounterService counterService;

	@Autowired(required = false)
	private GaugeService gaugeService;

	@Bean
	@ConditionalOnBean({ CounterService.class, GaugeService.class })
	public Filter metricFilter() {
		return new CounterServiceFilter();
	}

	/**
	 * Filter that counts requests and measures processing times.
	 * 
	 * @author Dave Syer
	 * 
	 */
	@Order(Integer.MIN_VALUE)
	// TODO: parameterize the order (ideally it runs before any other filter)
	private final class CounterServiceFilter extends GenericFilterBean {
		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			HttpServletResponse servletResponse = (HttpServletResponse) response;
			UrlPathHelper helper = new UrlPathHelper();
			String suffix = helper.getPathWithinApplication(servletRequest);
			int status = 999;
			DateTime t0 = new DateTime();
			try {
				chain.doFilter(request, response);
				status = servletResponse.getStatus();
			} finally {
				set("response", suffix, new Duration(t0, new DateTime()).getMillis());
				increment("status." + status, suffix);
			}
		}

		private void increment(String prefix, String suffix) {
			if (MetricFilterAutoConfiguration.this.counterService != null) {
				String key = getKey(prefix + suffix);
				MetricFilterAutoConfiguration.this.counterService.increment(key);
			}
		}

		private void set(String prefix, String suffix, double value) {
			if (MetricFilterAutoConfiguration.this.gaugeService != null) {
				String key = getKey(prefix + suffix);
				MetricFilterAutoConfiguration.this.gaugeService.set(key, value);
			}
		}

		private String getKey(String string) {
			String value = string.replace("/", "."); // graphite compatible metric names
			value = value.replace("..", ".");
			if (value.endsWith(".")) {
				value = value + "root";
			}
			if (value.startsWith("_")) {
				value = value.substring(1);
			}
			return value;
		}
	}

}
