### 1. 自定义注解区分同一接口不同Media Type --> @Consumes

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Consumes("application/x-thrift") @interface ConsumesThrift {
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Consumes("application/x-protobuf") @interface ConsumesProtobuf {
}

@Repeatable(ConsumesGroup.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Consumes {

    /**
     * A media type string. For example,
     * <ul>
     *   <li>{@code application/json; charset=utf-8}</li>
     *   <li>{@code application/xml}</li>
     *   <li>{@code application/octet-stream}</li>
     *   <li>{@code text/html}</li>
     * </ul>
     */
    String value();
}

// 使用
@Post("/api/v2/spans")
@ConsumesThrift
public HttpResponse uploadSpansV1Thrift(ServiceRequestContext ctx, HttpRequest req) {
    return validateAndStoreSpans(SpanBytesDecoder.THRIFT, ctx, req);
}

@Post("/api/v2/spans")
@ConsumesProtobuf
public HttpResponse uploadSpansProtobuf(ServiceRequestContext ctx, HttpRequest req) {
    return validateAndStoreSpans(SpanBytesDecoder.PROTO3, ctx, req);
}
```

> 实现关键是 armeria 的 @Consumes 注解