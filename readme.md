 ### springboot websocket 

1. demo:基于spring-boot-starter-websocket 实现websocket简单通信。MyHasdShakeInterceptor用户拦截socket各个的事件
2. netty:基于netty实现websocket服务端,及给特定用户发送消息的功能，MyNettyWebSocketHandler 可拦截http握手请求时的url参数如用户id，并设置到ctx的attr属性，以便能在channel上下文获取到用户的id,
3. demo:MyClusterWebSocketHandler 使用redis 发布订阅消息队列，实现websocket服务端集群
