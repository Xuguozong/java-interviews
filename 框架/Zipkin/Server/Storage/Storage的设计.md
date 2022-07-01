### 1. 类继承结构

> 类签名：public abstract class StorageComponent extends Component

*父类 Component*

```java
/**
 * Components are object graphs used to compose a zipkin service or client. For example, a storage
 * component might return a query api.
 *
 * <p>Components are lazy with regards to I/O. They can be injected directly to other components so
 * as to avoid crashing the application graph if a network service is unavailable.
 */
public abstract class Component implements Closeable {

  /**
   * Answers the question: Are operations on this component likely to succeed?
   *
   * <p>Implementations should initialize the component if necessary. It should test a remote
   * connection, or consult a trusted source to derive the result. They should use least resources
   * possible to establish a meaningful result, and be safe to call many times, even concurrently.
   * 测试数据来源的正确性
   * @see CheckResult#OK
   */
  public CheckResult check() {
    return CheckResult.OK;
  }

  /**
   * Closes any network resources created implicitly by the component.
   *
   * <p>For example, if this created a connection, it would close it. If it was provided one, this
   * would close any sessions, but leave the connection open.
   * 关闭各种资源，如果是连接，需要关闭该连接打开的会话而不是连接本身
   */
  @Override public void close() throws IOException {
  }
}
```

### 2. 主要属性及方法

