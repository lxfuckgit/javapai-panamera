# javapai-panamera

## feature
### 支持正向代理
https://github.com/1991wangliang/distribute-netty
https://github.com/1991wangliang/sds
https://github.com/loveinsky100/panama
1、支持http协议代理
2、支持https协议代理(ing)

### 支持反向代理
Client -> TcpProxyServer -> RealServer
 
静态代理配置：server-config.xml#upstream
动态代理注册：client注册到zookeeper，server通过线程check zookeeper(后续实现)动态IP代理。

类似：zk/etcd/consul + nginx + 第三方模块(nginx-upsync-module)的方式来实现nginx零重启更新upstream的操作。
https://www.jianshu.com/p/76e1f25a58fc
https://www.cnblogs.com/xuliang666/p/11157156.html
https://mp.weixin.qq.com/s/AOUaeq3glhJrb_NeRzXjbA

## how to use
### 1、启动proxy server
### 2、设置proxy host:port
### 3、内置proxy event
方案1：
http.setHeader("event":"get_proxy_ips");
http.setHeader("event":"list_proxy_ips");

方案2：
http://proxyip:proxyport/get_proxy_ips
http://proxyip:proxyport/list_proxy_ips






## Client与Server交互过程

PanameraServer 程序主入口。

## panamera-server（代理服务）
### 代理服务IP&Port指定
jvm参数（第一优先级）：
-Dserver.host=xxxx
-Dserver.port=8080

配置文件（第二优先级）：
server.host=xxxx
server.port=8080

目录规范：当前用户的主目录
文件名规范：server.propreties

