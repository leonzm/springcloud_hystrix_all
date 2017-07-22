# Spring Cloud Hystrix 笔记

# 简介
* 断路器出现的原因及原理
> 在微服务架构中，存在着那么多的服务单元，若一个单元出现故障，就很容易因依赖关系而引起故障的蔓延，最终导致整个系统的瘫痪，这样的架构相较传统
架构更加不稳定。为了解决这样的问题，产生了断路器等一系列的服务保护机制。
"断路器"本身是一种开关装置，用于在电路上保护线路过载，当线路中又电路发生短路时，"断路器"能及时切断故障电路，防止发生过载、发热甚至起火等严重
后果。
* Hystrix 中的断路器
> 在分布式架构中，断路器模式的作用也是类似的，当某个服务单元发生故障（类似用电器发生短路）之后，通过断路器的故障监控（类似于熔断保险丝），
向调用方法返回一个错误响应，而不是长时间的等待。这样就不会使得线程因调用故障服务被长时间占用不释放，避免了故障在分布式系统的蔓延。
> Spring Cloud Hystrix 基于 Netflix 的开源框架 Hystrix 实现了断路器、线程隔离等一系列服务保护功能，通过控制那些访问远程系统、服务和第
三方库的节点，从而对延迟和故障提供更强大的容错能力。Hystrix 具备服务降级、服务熔断、线程和信号隔离、请求缓存、请求合并以及服务监控等强大功
能。


# 使用 Spring Cloud Hystrix 步骤
* 引入 spring-cloud-starter-hystrix 依赖
* Spring Boot 启动类上加 @EnableCircuitBreaker 注解开启断路器功能
> 也可以使用 @SpringCloudApplication 注解代替 @SpringBootApplication、@EnableDiscoveryClient、@EnableCircuitBreaker 3个注解
* 改造服务消费方式，新增 XxxService 类，注入 RestTemplate 实例，在该 XxxService 中使用 RestTemplate 调用服务，并在 xxxService 函数上增加 @HystrixCommand 注解来指定回调方法

# 计算断路器的监控度
> Hystrix 会将"成功"、"失败"、"拒绝"、"超时"等信息报告给断路器，而断路器会维护一组计数器来统计这些数据。
> 断路器会使用这些统计数据来决定是否要将断路器打开，来对某个依赖服务的请求进行"熔断/短路"，直到恢复期结束。若在恢复期结束后，根据统计数据
判断如果还是未达到监控指标，就再次"熔断/短路"。

# fallback 服务降级处理
> 当命令执行失败的时候，Hystrix 会进入 fallback 尝试回退处理，该操作也称为"服务降级"。而能够引起服务降级处理的情况有：
* 当前命名处于"熔断/短路"状态，断路器是打开的时候
* 当前命令的线程池、请求队列或者信号量被占满的时候
* HystrixObservableCommand.construct() 或 HystrixCommand.run() 抛出异常的时候
> 注意：实现一个有可能失败的降级逻辑是一种非常糟糕的做法，我们应该在实现服务降级策略时尽可能避免失败的情况

# 断路器打开的条件
> 由下面的两个状态决定，只有两个同时为true时，才打开
* circuitBreakerRequestVolumeThreshold 如果它的请求数（QPS）在预设的阀值范围内就返回false，表示断路器处于未打开的状态。默认值为20
* circuitBreakerErrorThresholdPercentage 如果错误百分比在阀值范围内就返回 false，表示断路器处于未打开状态。默认值为50

# 断路器尝试关闭的过程
> 当断路器处于打开的时候，会判断断开时的 时间戳+配置中 circuitBreakerSleepWindowInMillliseconds 时间是否小于当前时间，是的话，就将当前
时间更新到记录断路器打开的时间对象 circuitOpenedOrLastTestedTime 中，并且允许此次请求。
> 简单地说，通过 circuitBreakerSleepWindowInMillliseconds 属性设置了一个断路器打开之后的休眠时间（默认是5秒），在该休眠时间到达之后，
将再次允许请求尝试访问，此时断路器处于"半开"状态，若此时请求继续失败，断路器又进入打开状态，并继续等待下一个休眠窗口过去之后再次尝试；若请求
成功，则将断路器重新置于关闭状态。
> 总结：断路器会在"断路打开"、"休眠"、"尝试关闭"和"断路关闭"间转换

# 依赖隔离
* 线程池
> 优点：1.应用自身得到完全保护，不会受不可控的依赖服务影响；2.可以有效降低接入新服务的风险
> 缺点：增加了系统的负载和开销
* 信号量
> 优点：相比线程池，开销远低于线程池的开销
> 缺点：不能设置超时和实现异步访问。所以只有在依赖服务是足够可靠的情况下才使用信号量

# 异常处理
* 异常传播
> 在 HystrixCommand 实现的 run() 方法中抛出异常时，除了 HystrixBadRequestException 之外，其他异常均会被 Hystrix 认为命令执行失败并
触发服务降级的处理逻辑
> 在使用注册配置实现 Hystrix 命令时，还支持忽略指定异常类型功能，只需要通过设置 @HystrixCommand 注解的 ignoreExceptions 参数，如：
@HystrixCommand(ignoreExceptions = {BadRequestException.class})

* 异常获取
> 在以传统继承方式实现的 Hystrix 命令中，可以用 getFallback() 方法通过 Throwable getExecutionException() 方法来获取具体的异常
> 在注解配置的方式中，只需要在 fallback 实现的方法的参数中增加 Throwable e 对象的定义即可获取具体的异常

# 请求缓存

# 请求合并

# 常用配置及属性
## execution 配置（控制 HystrixCommand.run()的执行）
* execution.isolation.strategy：该属性用来设置 HystrixCommand.run() 的执行的隔离策略，两种配置：1.THREAD：默认，通过线程池隔离的策略；2.SEMAPHORE：通过信号量隔离的策略
* execution.isolation.thread.timeoutInMilliseconds：该属性用来配置 HystrixCommand 执行的超时时间，单位为毫秒，默认：1000
* execution.timeout.enabled：配置 HystrixCommand.run() 的执行是否启用超时时间，默认为：true。如果为false，那么 execution.isolation.thread.timeoutInMilliseconds 配置将不起作用
* execution.isolation.thread.interruptOnTimeout：配置当 HystrixCommand.run() 执行超时的时候是否要将它中断，默认：true
* execution.isolation.thread.interruptOnCancel：配置当 HystrixCommand.run() 执行被取消的时候是否要将它中断，默认：true
* execution.isolation.semaphore.maxConcurrentRequests：配置信号量大小（并发请求数），默认：10

## fallback 配置（服务降级相关）
* fallback.isolation.semaphore.maxConcurrentRequests：配置从调用线程中允许 HystrixCommand.getFallback() 方法执行的最大并发请求数，默认为：10
* fallback.enabled：配置服务降级策略是否启用，默认为：true。如果设置为 false，那么当请求失败或者拒绝发生时，将不会调用 HystrixCommand.getFallback() 来执行服务降级逻辑

## circuitBreaker 配置（断路器相关）
* circuitBreaker.enabled：配置当服务请求失败时，是否启用断路器来跟踪其健康指标和熔断请求，默认为：true
* circuitBreaker.requestVolumeThreashold：配置在滚动时间窗口，断路器熔断的最小请求数，默认为：20
* circuitBreaker.sleepWindowInMilliseconds：配置当断路器打开之后的休眠时间窗，默认为：5000。休眠时间窗结束之后，会将断路器置为"半开"状态，尝试熔断的请求命令，如果依然失败就将断路器继续设置为"半开"状态，如果成功就设置为"关闭"状态
* circuitBreaker.errorThresholdPercentage：配置断路器打开的错误百分比条件，默认：50
* circuitBreaker.forceOpen：配置为 true 时，断路器将强制进入"打开"状态，它会拒绝所有请求，默认：false
* circuitBreaker.forceClosed：配置为 true 时，断路器将强制进入"关闭"状态，它会接受所有的请求，默认：false。如果此时 circuitBreaker.forceOpen 也配置为 true，那么forceOpen不会生效

## metrics 配置（HystrixCommand 和 HystrixObservableCommand 执行中捕获的指标信息有关）
* metrics.rollingStats.timeInMillseconds：设置滚动时间窗的长度，单位为毫秒，默认：10000。该时间用于断路器判断健康度时需要收集信息的持续时间。断路器在收集指标信息的时候会根据设置的时间窗口长度拆分成多个"桶"来累计各度量值，每个"桶"记录了一段时间内的采集指标
* metrics.rollingStats.numBuckets：设置滚动时间窗统计指标信息时划分"桶"的数量，默认：10。注：metrics.rollingStats.timeInMilliseconds 参数的设置必须能够被 metrics.rollingStats.numBuckets 参数整除，否则将抛出异常
* metrics.rollingPercentile.enabled：设置对命令执行的延迟是否使用百分位数来跟踪和计算，默认：true。如果设置为 false，那么所有的概要统计都将返回-1
* metrics.rollingPercentile.timeInMilliseconds：设置百分位统计的滚动窗口的持续时间，单位为毫秒，默认：60000
* metrics.rollingPercentile.numBuckets：设置百分位统计滚动窗口中使用"桶"的数量，默认：6。注：metrics.rollingPercentile.timeInMilliseconds 参数的设置必须能够被 metrics.rollingPercentile.numBuckets 参数整除，否则将抛出异常
* metrics.rollingPercentile.bucketSize：设置在执行过程中，每个"桶"中保留的最大执行次数，默认：100
* metrics.healthSnapshot.intervalInMilliseconds：配置采集影响断路器状态的健康快照（请求成功、错误百分比）的间隔等待时间，默认：500

## requestContext 配置（HystrixCommand 使用的 HystrixRequestContext 的设置）
* requestCache.enabled：配置是否开启请求缓存，默认：true
* requestLog.enabled：设置 HystrixCommand 的执行和事件是否打印日志到 HystrixRequestLog 中，默认：true

## collapser 属性（用来控制命令合并相关的命令。可以在代码中用 set 和配置文件配置之外，也可以使用注解进行配置）
* maxRequestsInBatch：设置一次请求合并批处理中允许的最大请求数，默认：Integer.MAX_VALUE
* timerDelayInMillseconds：设置批处理过程中每个命令延迟的时间，单位为毫秒，默认：10
* requestCache.enabled：设置批处理过程中是否开启请求缓存

## threadPool 属性（可以在代码中用 set 和配置文件配置之外，也可以使用注解进行配置）
* coreSize：该参数用来配置执行命令线程池的核心线程数，也就是命令执行的最大并发量，默认：10
* maxQueueSize：设置线程池的最大队列大小，默认：-1。当设置-1时，线程池将使用 SynchronousQueue 实现的队列，否则将使用 LinkedBlockingQueue 实现的队列
* queueSizeRejectionThreshold：为队列设置阀值，默认：5。通过设置该参数，即使队列没有达到最大值也能拒绝请求，该参数主要是对 LinkedBlockingQueue 队列的补充
* metrics.rollingStates.timeInMilliseconds：设置滚动时间窗的长度，单位为毫秒，默认：10000。用于线程池的指标度量，它会被分成多个"桶"来统计指标
* metrics.rollingStates.numBuckets：设置滚动时间窗被划分成"桶"的数量，默认：10。注：metrics.rollingStates.timeInMilliseconds 参数的设置必须能够被 metrics.rollingStates.numBuckets 参数整除，否则将会抛出异常

# Hystrix 仪表盘
> 在 HystrixCommand 和 HystrixObservableCommand 实例执行过程中，会记录关于请求命令的度量指标信息，它们除了在 Hystrix 断路器实现中使用之外，
也可以用在系统运维中。这些指标信息会以"滚动时间窗"与"桶"结合的方式进行汇总，并在内存中驻留一段时间，以供内部或外部进行查询使用，Hystrix 仪表
盘就是这些指标内容的消费者之一。

# 搭建 Hystrix Dashboard 步骤
* 创建一个标准的 Spring Boot 工程
* pom 中加入 hystrix、hystrix-dashboard、actuator 依赖
* 主类上加 @EnableHystrixDashboard，启用 Hystrix Dashboard 功能
* 根据时间情况配置 appliacation.propertis 配置文件
* 然后访问 http://[ip]:[port]/hystrix 就可以看到 Hystrix Dashboard 的监控首页了，通过添加具体的监控来实现监控目标

# Hystrix Dashboard 支持三种监控
* 默认的集群监控：通过 URL http://turbine-hostname:port/turbine.stream 开启，实现对默认集群的监控；
* 指定的集群监控：通过 URL http://turbine-hostname:port/turbine.stream?cluster=[clusterName] 开启，实现对 clusterName 集群的监控
* 单体应用的监控：通过 URL http://hystrix-app:port/hystrix.stream 开启，实现对具体某个服务实例的监控

# 单体应用的监控的使用步骤
* 服务依赖中再加入 actuator 模块，确保该应用已经引入断路器
* 主类中 @EnableCircuitBreaker 注解，开启断路器功能
* 启动服务实例后，在 Hystrix Dashbaord 中添加该单体应用的监控

# Turbine 集群监控的使用步骤
* 创建一个标准的 Spring Boot 工程
* pom 中加入 turbine、actuator 依赖
* 主类上加 @EnableTurbine，别忘了 @EnableDiscoveryClient 注解，启用 Turbine
* 根据时间情况配置 appliacation.propertis 配置文件
> turbine.app-config 参数指定需要收集监控信息的服务名；
> turbine.cluster-name-expression 参数指定了集群名称；
> turbine.combine-host-port 参数如果设置为true，会以ip+port来区分不同的服务，默认为false，会以ip来区分不同的服务
* 在 Hystrix Dashboard 的监控首页上添加 http://[ip]:[port]/turbine.stream 监控




