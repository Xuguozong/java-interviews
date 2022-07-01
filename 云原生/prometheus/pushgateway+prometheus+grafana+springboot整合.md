### 1. docker 安装 pushgateway + prometheus + grafana

> pushgateway : 将 metrics 从应用端推送到 pushgateway，再由 prometheus 配置拉取数据

> docker run --name pushgateway -p 9091:9091 prom/pushgateway
>
> docker run --name prometheus -p 9090:9090 prom/prometheus
>
> docker run --name grafana -p 3000:3000 grafana/grafana

- prometheus 配置拉取 pushgateway：/etc/prometheus/prometheus.yml

  ```yaml
  - job_name: "pushgateway"
    scrape_interval: 15s
    static_configs:
      - targets: ["127.0.0.1:9091"]
  ```

- grafana 配置

  - 默认用户名、密码：admin/admin
  - 配置 JVM dashboard：选择 import，id --> 12856，load 就 ok 了

  

### 2. Prometheus 通过 Federation 从另一个 Prometheus 上同步数据

> 配置文件配置

```yaml
- job_name: 'federate'
  scrape_interval: 15s
  honor_labels: true
  metrics_path: '/federate'
  params:
    'match[]': 
      - '{job="prometheus"}'
      - '{__name__=~"job:.*"}'
  static_configs:
    - targets: ["another_prometheus_ip:port"]
```



### 3. Spring Boot 应用整合 pushgateway + prometheus

> pom.xml

```
actuator/simpleclient_servlet/simpleclient_pushgateway/micrometer-registry-prometheus
```



> application.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        includes: metrics
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        pushgateway:
          baseUrl: 127.0.0.1:9091
          pushRate: 15s
          job: ${spring.application.name}
          enabled: true
```

