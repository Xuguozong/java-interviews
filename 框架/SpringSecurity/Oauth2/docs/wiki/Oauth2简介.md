# OAuth 2.0

`简介`：OAuth是一个关于授权的（authorization）的开放网络标准，参考材料[RFC6749](http://www.rfcreader.com/#rfc6749)

### 角色（Roles）：

    Client:                第三方应用（APP或向外提供接口）
    Redource Owner:        资源所有者（用户）
    Authentication Server: 授权认证服务器（发配Access Token）
    Resource Server:       资源服务器（存放用户资源）

### 流程（Protocol Flow）：

     +--------+                               +---------------+
     |        |--(A)- Authorization Request ->|   Resource    |
     |        |                               |     Owner     |
     |        |<-(B)-- Authorization Grant ---|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(C)-- Authorization Grant -->| Authorization |
     | Client |                               |     Server    |
     |        |<-(D)----- Access Token -------|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(E)----- Access Token ------>|    Resource   |
     |        |                               |     Server    |
     |        |<-(F)--- Protected Resource ---|               |
     +--------+                               +---------------+

     （A） 用户打开客户端后，客户端要求用户给予授权
     （B） 用户同意给予客户端授权
     （C） 客户端用上一步获取的授权，向认证服务器申请令牌
     （D） 认证服务器对客户端进行认证后，确认无误后发放令牌
     （E） 客户端使用令牌向资源服务器申请获取资源
     （F） 资源服务器确认令牌无误后向客户端提供受保护资源

### 4种授权模式（Authorization Grant）

	1） 授权码模式（Authorization Code）
    2） 简化模式（Implicit）
    3） 密码模式（Resource Owner Password Credentials）
    4） 客户端模式（Client Credentials）