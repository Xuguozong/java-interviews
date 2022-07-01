# <center> Spring MVC应用添加自定义响应头的几种方式 </center>

### 背景
** &emsp;&emsp;调用链埋点库hik.brave.sdk的开发需求中有一条是将每条请求产生的traceId作为header设置在响应头中,最终通过查看Spring MVC中各种设置自定义响应头的方法并选定通过实现自定义拦截器的方法完成了这个需求。在此将之前学习的设置自定义响应头的方法和场景记录下来以供交流学习。**

### 添加响应头的几种方法
#### 1. 在 Controller 方法中获取 HttpServletResponse 参数添加
```
@GetMapping("/test")
public String test(HttpServletResponse response){
    // 设置响应头
    response.addHeader("dummy-header", "dummy-value");
    return "ok";
}
```
** &emsp;&emsp;这种方式添加响应头有较大的灵活性，对于每个 controller 的每个方法都能有精准的控制，但每次都要获取 HttpServletResponse 参数，对于全局响应头的添加不友好。 **

#### 2. 自定义拦截器，在 postHandle 方法中添加响应头
```
public class DummyInterceptor extends HandlerInterceptorAdapter {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    	// 设置响应头
        response.addHeader("dummy-header, "dummy-value");
    }

}
```
** &emsp;&emsp;自定义的拦截器需要配置进拦截器链中才能生效，这种添加响应头的方式适合为该拦截器拦截到的所有请求添加若干共性的响应头，比如调用链埋点库会为每个 HTTP 请求生成一个响应头，用于存放每个请求产生的唯一 traceId，用于后续查询调用链详情的操作。不过这种方式也有一个问题，在有 @ResponseBody 注解的方法中会失效（因此在 @RestController 类中会失效），具体原因是加了 @ResponseBody 注解的方法，HttpMessageConverter 会在 postHandle()方法执行之前修改并提交了 response 的内容，到执行 postHandle() 的时候， response 的内容已经不能再做修改(有一种特殊情况，controller 方法中返回值类型是 ResponseEntity 的还是会生效的，后续文章再详细介绍)。这种情况下可以使用下面的方法去添加响应头： **

#### 3. 实现 ResponseBodyAdvice 接口并结合 @ControllerAdvice 注解，在 beforeBodyWrite() 方法中添加响应头
```
@ControllerAdvice
public class HeaderModifierAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 设置响应头
        response.getHeaders().add("dummy-header","dummy-value");
        return body;
    }

}
```
** &emsp;&emsp;这种添加响应头的方法解决了方法2中失效的情况，通过查看 ResponseBodyAdvice.beforeBodyWrite()方法的注释可以发现，该方法会在 HttpMessageConverter 写 response 的方法执行之前执行，它对 @Controller 和 @RestController 都有效，但在 controller 方法中返回值类型是 ResponseEntity 的时候是会生效的(与方法2的特殊情况相反！) **

#### 4. 自定义过滤器，在 doFilter 方法中添加响应头
```
@Order(1)
@WebFilter(filterName = "addheader", urlPatterns = {"/**"})
public class AddHeaderFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            chain.doFilter(request, response);
        } finally {
            // 设置响应头
            response.addHeader("dummy-header", "dummy-header");
        }
    }

    @Override
    public void destroy() {

    }
}
```
** &emsp;&emsp;需要配置自定义过滤器以生效 **

### 总结
 序号 | 实现方法 | 优点 | 缺点 | 适用场景
:-: | :-: | :-: | :-: | :-: | :-:
1 | 在controller方法参数中获取HttpServletResponse参数添加 | 灵活精准 | 重复代码 | 对于特定接口的header定制
2 | 在拦截器的postHandle()方法中添加响应头 | 添加全局响应头 | 对@RestController失效 | 能对根据拦截器需求添加特定的一类响应头(取决于拦截器配置)
3 | 实现ResponseBodyAdvice在beforeBodyWrite()方法中添加响应头 | 添加全局响应头 | controller方法中返回值类型是ResponseEntity的时候会生效 | 适合添加全局的响应头
4 | 自定义过滤器，在doFilter方法中添加响应头 | 添加全局响应头 | 不够灵活 | 适合添加全局的响应头

参考：
&emsp;&emsp;&emsp;https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc
&emsp;&emsp;&emsp;https://stackoverflow.com/questions/43481539/spring-how-can-i-add-a-header-to-all-responses-that-i-return