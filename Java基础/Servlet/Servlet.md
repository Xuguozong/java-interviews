## Servlet 接口

1. 实例数量
	通常单实例（注解描述和部署描述的）
2. 生命周期
	1）加载和实例化
		发生在容器启动时或延迟初始化直到容器决定有请求需要处理时。类加载器加载本地、远程文件系统或网络服务的servlet类。
	2）初始化
		init(ServletConfig config) ServletConfig 包含 ServletContext 对象
		错误处理：初始化阶段可能会抛出UnavailableException或ServletException,servlet容器会释放它。如果初始化没有成功，destroy方法不应该被调用
	3）请求处理
		service(ServletRequest req, ServletResponse res)
		异常处理：
			ServletException：处理请求时出现的错误，容器应当采取适当措施清理掉这个请求
			UnavailableException：临时性的或者永久性的无法处理请求
				永久性的：移除servlet，调用destroy，释放servlet实例，返回SC_NOT_FOUND(404)响应
				临时性的：可选择在临时不可用的这段时间内路由任何请求到Servlet，被拒绝的请求会返回SC_SERVICE_UNAVAILABLE(503)响应，同时返回Retry-After头指示此servlet何时可用。
		异步处理：
			A. 请求 ---> Filter ---> Servlet
			B. 根据请求参数和内容体确定请求类型
			C. servlet获取资源或IO等耗时操作
			D. servlet不产生响应并返回
			E. 当资源可用，要么在统一线程上继续处理事件，要么通过ASyncContext分配到容器中的一个资源上。
	4）服务的终止
		调用destroy方法之前，必须让当前正在执行service方法的任何线程完成执行，或者超过了服务器定义的时间限制。一旦调用了destroy方法，容器再无法路由其他请求到该servlet实例了。如果要用，必须新实例化新的servlet。



## 分发请求

1. RequestDispatcher接口提供了一种机制来把请求转发给另一个servlet处理或在请求中包含另一个servlet的输出。



## 过滤器 Filter

1. 作用
	一种代码重用技术，可以转换HTTP请求和响应的头信息和内容：
	1）在请求执行之前访问资源
	2）在执行请求之前处理资源的请求
	3）用请求对象的自定义包装版本对请求进行修改
	4）用响应对象的自定义包装版本对响应进行修改
	5）拦截资源调用之后的调用
	6）作用在一个或一组Servlet，或静态内容上的0个，一个或多个拦截器按指定的顺序执行



## 会话 Session

1. 会话跟踪机制
	Cookie、SSL会话、URL重写（当客户端不接受cookie时，服务器可使用URL重写作为会话跟踪的基础）
2. 会话范围
	HttpSession对象必须被限定在应用（或Servlet上下文）级别。
3. 绑定属性到对话
	HttpSessionBindingListener接口的valueBound/valueUnbound方法用于标识一个对象被绑定到会话或从会话解除绑定时行为。
4. 重要会话语义
	容器必须在迁移会话时通知实现了HttpSessionActivationListener的所有会话属性。它们必须在序列化会话之前通知钝化监听器，在反序列化之后通知激活监听器。



## 请求 Request

1. 文件上传
	满足以下两种情况之一，servlet容器会处理 multipart/form-data 数据格式的数据：
	1）使用了 @MultipartConfig 注解
	2）部署描述符中包含了 multipart-config 元素
	如何可用：
	1）如果servlet支持multipart/form-data数据处理，可通过HttpServletRequest的getParts()和getPart(String name)方法获取
	2）如果不支持，可通过HttpServletRequest的getInputStream()方法得到数据流
2. 请求路径元素
	1）Context Path 请求上下文
	2）Servlet Path 路径部分直接与激活请求的映射对应
	3）Pathinfo 请求路径的一部分
3. 路径转换方法
	获取和某个特定路径等价的文件系统路径：
	ServletContext.getRealPath(String path)
	HttpServletRequest.getPathTranslated() 负责推断出请求的pathinfo的实际路径
4. 非阻塞IO
	非阻塞IO只在异步处理或升级处理时有效，否则调用ServletInputStream.setReadLister或ServletOutputStream.setWriteListener时会跑走出IllegalStateException
5. SSL属性
	ServletRequest的isSecure方法公开请求是否通过安全协议发送。Web容器必须公开以下属性给servlet开发者：
	密码套件      --->  javax.servlet.request.cipher_suite
	算法的位大小  --->  javax.servlet.request.key_size
	SSL会话ID     --->  javax.servlet.request.ssl_session_id
6. 请求对象声明周期
	正常情况在servlet的service方法和过滤器的doFilter方法的作用域内。异步处理情况，在request的startAsync方法调用之后、AsyncContext的complete方法调用为止
7. ServletContext接口
	定义了servlet运行在Web应用中的视图，由应用容器提供其实现，它是应用服务器中一直路径的根。每一个部署到容器的web应用都有一个ServletContext实例与之对应
8. 资源
	getResource/getResourceAsStream 用于访问静态内容层次结构的文件
	参数为"/"，给定的资源路径是相对于上下文的根，或者web应用WEB-INF/lib目录下jar包中的META-INF/resources目录



## 响应 Response

1. 缓冲
	servlet容器容许（但不要求）缓存输出到客户端的内容，以下是ServletResponse接口的相关方法：
	getBufferSize/setBufferSize/isCommitted/reset/flushBuffer/resetBuffer
	1）setBufferSize 必须在ServletOutputStream或Writer的写方法之前调用，否则抛出IllegalStateException
	2）isCommitted 表示是否有响应字节已经返回到客户端
	3）flushBuffer 强制刷出缓存区的内容到客户端
	4）reset 响应未提交之前清空缓存区数据
	5）resetBuffer 响应未提交之前清空缓存区数据
2. 结束响应对象
	当响应被关闭时，容器必须立即刷出响应缓存区中的所有剩余内容到客户端：
	1）service方法终止
	2）setContentLength和setContentLengthLong已生效并写入到响应中
	3）sendError已经调用
	4）sendRedirect已经调用
	5）AsyncContext的complete方法已经调用



## 注解和可插拔性

1. 部署描述符的“metadata-complete”属性
	Web应用部署描述符的web-app元素包含一个新的“metadata-complete”，她定义了web描述符是否完整，若设置为true，部署工具必须忽略存在于应用的类文件中的所有指定部署信息的servlet注解和web fragments。
	@WebServlet/@WebFilter/@WebInitParam/@WebListener/@MultipartConfig,其中，被@WebListener注解的类必须实现以下接口的一个或多个：
	ServletContextListener/ServletContextAttributeListener/ServletRequestListener/ServletRequestAttributeListener/HttpSessionListener/HttpSessionAttributeListener/HttpSessionIdListener
	
2. web-fragment
	web-fragment是web.xml的部分或全部，可以在一个类库或框架jar包的META-INF目录指定和包括。
	
	

