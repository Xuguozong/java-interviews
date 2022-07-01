### 概述

> 主要是在配置类中获取所有 RestTemplate ，在 RestTemplate 中添加拦截器，在拦截器的执行逻辑中执行负载均衡相关的方法



#### 关键类

#### LoadBalancerAutoConfiguration

> spring-cloud-commons 中负载均衡相关的自动配置类

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnBean(LoadBalancerClient.class)
@EnableConfigurationProperties(LoadBalancerRetryProperties.class)
public class LoadBalancerAutoConfiguration {

	@LoadBalanced
	@Autowired(required = false)
	private List<RestTemplate> restTemplates = Collections.emptyList();

	@Autowired(required = false)
	private List<LoadBalancerRequestTransformer> transformers = Collections.emptyList();

	@Bean
	public SmartInitializingSingleton loadBalancedRestTemplateInitializerDeprecated(
			final ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
		return () -> restTemplateCustomizers.ifAvailable(customizers -> {
			for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
				for (RestTemplateCustomizer customizer : customizers) {
					customizer.customize(restTemplate);
				}
			}
		});
	}

	@Bean
	@ConditionalOnMissingBean
	public LoadBalancerRequestFactory loadBalancerRequestFactory(
			LoadBalancerClient loadBalancerClient) {
		return new LoadBalancerRequestFactory(loadBalancerClient, this.transformers);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingClass("org.springframework.retry.support.RetryTemplate")
	static class LoadBalancerInterceptorConfig {

		@Bean
		public LoadBalancerInterceptor ribbonInterceptor(
				LoadBalancerClient loadBalancerClient,
				LoadBalancerRequestFactory requestFactory) {
			return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
		}

		@Bean
		@ConditionalOnMissingBean
		public RestTemplateCustomizer restTemplateCustomizer(
				final LoadBalancerInterceptor loadBalancerInterceptor) {
			return restTemplate -> {
				List<ClientHttpRequestInterceptor> list = new ArrayList<>(
						restTemplate.getInterceptors());
				list.add(loadBalancerInterceptor);
				restTemplate.setInterceptors(list);
			};
		}

	}

	/**
	 * Auto configuration for retry mechanism.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(RetryTemplate.class)
	public static class RetryAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public LoadBalancedRetryFactory loadBalancedRetryFactory() {
			return new LoadBalancedRetryFactory() {
			};
		}

	}

	/**
	 * Auto configuration for retry intercepting mechanism.
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(RetryTemplate.class)
	public static class RetryInterceptorAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public RetryLoadBalancerInterceptor ribbonInterceptor(
				LoadBalancerClient loadBalancerClient,
				LoadBalancerRetryProperties properties,
				LoadBalancerRequestFactory requestFactory,
				LoadBalancedRetryFactory loadBalancedRetryFactory) {
			return new RetryLoadBalancerInterceptor(loadBalancerClient, properties,
					requestFactory, loadBalancedRetryFactory);
		}

		@Bean
		@ConditionalOnMissingBean
		public RestTemplateCustomizer restTemplateCustomizer(
				final RetryLoadBalancerInterceptor loadBalancerInterceptor) {
			return restTemplate -> {
				List<ClientHttpRequestInterceptor> list = new ArrayList<>(
						restTemplate.getInterceptors());
				list.add(loadBalancerInterceptor);
				restTemplate.setInterceptors(list);
			};
		}

	}

}

```



#### LoadBalancerInterceptor

> 负载均衡拦截器，拦截 http 请求，进行负载均衡的转换

```java
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {
	private LoadBalancerClient loadBalancer;
	private LoadBalancerRequestFactory requestFactory;

	@Override
	public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
			final ClientHttpRequestExecution execution) throws IOException {
		final URI originalUri = request.getURI();
		String serviceName = originalUri.getHost();
		Assert.state(serviceName != null,
				"Request URI does not contain a valid hostname: " + originalUri);
		return this.loadBalancer.execute(serviceName,
				this.requestFactory.createRequest(request, body, execution));
	}
}
```



#### RestTemplateCustomizer

> **用来定制 RestTemplate，添加 loadBalancerInterceptor **

```java
public interface RestTemplateCustomizer {
	void customize(RestTemplate restTemplate);
}

@Bean
@ConditionalOnMissingBean
public RestTemplateCustomizer restTemplateCustomizer(
		final LoadBalancerInterceptor loadBalancerInterceptor) {
	return restTemplate -> {
		List<ClientHttpRequestInterceptor> list = new ArrayList<>(
				restTemplate.getInterceptors());
		list.add(loadBalancerInterceptor);
		restTemplate.setInterceptors(list);
	};
}
```



#### LoadBalancerClient

> 执行最终的负载均衡逻辑的接口类

```java
public interface LoadBalancerClient extends ServiceInstanceChooser {

	/**
	 * Executes request using a ServiceInstance from the LoadBalancer for the specified
	 * service.
	 * @param serviceId The service ID to look up the LoadBalancer.
	 * @param request Allows implementations to execute pre and post actions, such as
	 * incrementing metrics.
	 * @param <T> type of the response
	 * @throws IOException in case of IO issues.
	 * @return The result of the LoadBalancerRequest callback on the selected
	 * ServiceInstance.
	 */
	<T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException;

	/**
	 * Executes request using a ServiceInstance from the LoadBalancer for the specified
	 * service.
	 * @param serviceId The service ID to look up the LoadBalancer.
	 * @param serviceInstance The service to execute the request to.
	 * @param request Allows implementations to execute pre and post actions, such as
	 * incrementing metrics.
	 * @param <T> type of the response
	 * @throws IOException in case of IO issues.
	 * @return The result of the LoadBalancerRequest callback on the selected
	 * ServiceInstance.
	 */
	<T> T execute(String serviceId, ServiceInstance serviceInstance,
			LoadBalancerRequest<T> request) throws IOException;

	/**
	 * Creates a proper URI with a real host and port for systems to utilize. Some systems
	 * use a URI with the logical service name as the host, such as
	 * http://myservice/path/to/service. This will replace the service name with the
	 * host:port from the ServiceInstance.
	 * @param instance service instance to reconstruct the URI
	 * @param original A URI with the host as a logical service name.
	 * @return A reconstructed URI.
	 */
	URI reconstructURI(ServiceInstance instance, URI original);

}
```

#### RibbonLoadBalancerClient

> LoadBalancerClient 的实现类，最终执行负载均衡转换的类

```java
public <T> T execute(String serviceId, LoadBalancerRequest<T> request, Object hint)
		throws IOException {
    // 获取负载均衡器
	ILoadBalancer loadBalancer = getLoadBalancer(serviceId);
    // 获取目标 server
	Server server = getServer(loadBalancer, hint);
	if (server == null) {
		throw new IllegalStateException("No instances available for " + serviceId);
	}
	RibbonServer ribbonServer = new RibbonServer(serviceId, server,
			isSecure(server, serviceId),
			serverIntrospector(serviceId).getMetadata(server));

	return execute(serviceId, ribbonServer, request);
}

@Override
public <T> T execute(String serviceId, ServiceInstance serviceInstance,
		LoadBalancerRequest<T> request) throws IOException {
	Server server = null;
	if (serviceInstance instanceof RibbonServer) {
		server = ((RibbonServer) serviceInstance).getServer();
	}
	if (server == null) {
		throw new IllegalStateException("No instances available for " + serviceId);
	}

	RibbonLoadBalancerContext context = this.clientFactory
			.getLoadBalancerContext(serviceId);
	RibbonStatsRecorder statsRecorder = new RibbonStatsRecorder(context, server);

	try {
		T returnVal = request.apply(serviceInstance);
		statsRecorder.recordStats(returnVal);
		return returnVal;
	}
	// catch IOException and rethrow so RestTemplate behaves correctly
	catch (IOException ex) {
		statsRecorder.recordStats(ex);
		throw ex;
	}
	catch (Exception ex) {
		statsRecorder.recordStats(ex);
		ReflectionUtils.rethrowRuntimeException(ex);
	}
	return null;
}

// 负载均衡选择要发送请求的 server
protected Server getServer(ILoadBalancer loadBalancer, Object hint) {
	if (loadBalancer == null) {
		return null;
	}
	// Use 'default' on a null hint, or just pass it on?
	return loadBalancer.chooseServer(hint != null ? hint : "default");
}
```

