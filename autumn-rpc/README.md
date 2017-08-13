# AutumnRPC
> 轻量级 HTTP RPC 框架

## 简介
AutumnRPC 是一个基于 HTTP 协议的 RPC 框架，实现了 Java 应用之间的相互调用，将来还会加入跨语言支持。

## 快速上手
### 服务端
- 配置保存在 `autumn-rpc-server.properties` 文件中
- 在需要暴露的接口**实现**类上添加 `@AutumnRPCExpose` 注解
- 在程序中启动服务: `AutumnRPCServer.start(...)`

### 客户端
- 配置保存在 `autumn-rpc-client.properties` 文件中
- 通过 `new AutumnRPCClient(...)` 新建客户端实例
- 使用 `getService(...)` 方法获得接口的代理对象

## 设计
### HTTP 服务器
- [x] rapidoid-http-fast **[Default]**
- [x] AutumnHTTP

### 序列化 / 反序列化
- [x] Jackson **[Default]**
- [x] Hessian2 (provided by hessian-lite)
- ~~FastJson~~

### 反射调用方式
- [x] Java reflection
- [x] ReflectASM **[Default]**
- ~~javassist~~

### 动态代理
- [x] Java proxy
- [ ] CGLib

### 框架支持
- [ ] Spring
- [ ] Autumn

### 跨语言支持
- [ ] PHP (Consumer)
- [ ] Python (Consumer)
