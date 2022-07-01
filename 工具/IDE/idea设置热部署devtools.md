## 1. 添加 devtools 依赖

```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>

```

## 2. 添加 maven 插件

```java
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
      <fork>true</fork>
      <addResources>true</addResources>
    </configuration>
</plugin>
```

## 3. idea 设置 auto compiler

	build --> Compiler --> 勾选 A(Automatically...) D(Display....) B(Build...) C(Compile...) 四个选项

## 4. idea 设置 Registry

	快捷键调出 Registry ： Shift + Ctrl + Alt + /
    勾选：
    	compiler.automake.allow.when.app.running
        actionSystem.assertFocusAccessFromEdt

## 5. 重启 idea