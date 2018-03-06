/*
 * Copyright 2013-2018 the original author or authors.
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
 *
 */

package org.springframework.cloud.gateway.filter.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GatewayFilterBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

	private static final Log log = LogFactory.getLog(GatewayFilterBeanPostProcessor.class);

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RouteDefinitionRouteLocator) {
			String[] names = new String[0];
			if (beanFactory instanceof ListableBeanFactory) {
				ListableBeanFactory factory = (ListableBeanFactory) beanFactory;
				names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(factory, GatewayFilter.class);
			}

			List<Class> list = Arrays.stream(names).map(name -> {
				Class<?> type = beanFactory.getType(name);
				if (!beanFactory.isPrototype(name)) {
					log.warn("GatewayFilter is not prototype scoped. This may cause strange behavior. '" +
							name + "', "+ type);
				}
				return type;
			}).collect(Collectors.toList());
			RouteDefinitionRouteLocator locator = (RouteDefinitionRouteLocator) bean;
			locator.setGatewayFilterClasses(list);
			System.out.println(locator);
		}
		return bean;
	}
}