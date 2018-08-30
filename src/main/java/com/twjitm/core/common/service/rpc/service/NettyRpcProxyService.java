package com.twjitm.core.common.service.rpc.service;import com.twjitm.core.common.config.global.NettyGameServiceConfigService;import com.twjitm.core.common.factory.thread.NettyRpcHandlerThreadPoolFactory;import com.twjitm.core.common.service.IService;import com.twjitm.core.common.service.rpc.client.NettyAsyncRPCCallback;import com.twjitm.core.common.service.rpc.client.NettyRpcFuture;import com.twjitm.core.common.service.rpc.client.proxy.INettyAsyncRpcProxy;import com.twjitm.core.common.service.rpc.client.proxy.NettyAsyncRpcProxy;import com.twjitm.core.common.service.rpc.client.proxy.NettyObjectProxy;import com.twjitm.core.spring.SpringServiceManager;import org.springframework.stereotype.Service;import javax.annotation.Resource;import java.lang.reflect.InvocationHandler;import java.lang.reflect.Proxy;import java.util.concurrent.ArrayBlockingQueue;import java.util.concurrent.ThreadPoolExecutor;import java.util.concurrent.TimeUnit;/** * @author EGLS0807 - [Created on 2018-08-20 12:10] * @company http://www.g2us.com/ * @jdk java version "1.8.0_77" */@Servicepublic class NettyRpcProxyService implements IService {    @Resource    NettyRpcHandlerThreadPoolFactory threadPoolFactory;    /**     * rpc异步消息代理服务执行线程     */    private static ThreadPoolExecutor threadPoolExecutor;    /**     * 动态代理利用了JDK API，{@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}     * 动态地在内存中构建代理对象，从而实现对目标对象的代理功能。动态代理又被称为JDK代理或接理。     * <p>     * 静态代理与动态代理的区别主要在：     * <p>     * 静态代理在编译时就已经实现，编译完成后代理类是一个实际的class文件     * 动态代理是在运行时动态生成的，即编译完成后没有实际的class文件，而是在运行时动态生成类字节码，并加载到JVM中     * 特点： 动态代理对象不需要实现接口，但是要求目标对象必须实现接口，否则不能使用动态代理。     *     * @param interfaceClass     * @param <T>     * @return     */    @SuppressWarnings("unchecked")    public <T> T createProxy(Class<T> interfaceClass) {        NettyGameServiceConfigService gameServerConfigService = SpringServiceManager.getSpringLoadService().getNettyGameServiceConfigService();        int timeOut = gameServerConfigService.getNettyGameServiceConfig().getRpcTimeOut();        //创建一个动态代理对象        return (T) Proxy.newProxyInstance(                interfaceClass.getClassLoader(),                new Class<?>[]{interfaceClass},                new NettyObjectProxy<T>(interfaceClass, timeOut)        );    }    /**     * 创建一个异步代理对象     *     * @param interfaceClass     * @param <T>     * @return     */    public <T> INettyAsyncRpcProxy createAsync(Class<T> interfaceClass) {        return new NettyAsyncRpcProxy<>(interfaceClass);    }    @Override    public String getId() {        return NettyRpcProxyService.class.getSimpleName();    }    @Override    public void startup() throws Exception {        NettyGameServiceConfigService gameServerConfigService = SpringServiceManager.getSpringLoadService().getNettyGameServiceConfigService();        int threadSize = gameServerConfigService.getNettyGameServiceConfig().getRpcSendProxyThreadSize();        threadPoolExecutor = new ThreadPoolExecutor(threadSize, threadSize, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536));    }    @Override    public void shutdown() throws Exception {        threadPoolExecutor.shutdown();    }    /**     * 提交一个任务，rpc异步请求采用子线程来处理请求任务，将请求任务放入到请求队列中，任务线程     * 从任务队列里取出任务执行。在返回future     * 中注入回调函数{@link NettyRpcFuture#addCallback(NettyAsyncRPCCallback)}。     * 返回给调用线程获取返回结果。     *     * @param task     */    public void submit(Runnable task) {        threadPoolExecutor.submit(task);    }}