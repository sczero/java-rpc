## 前言

### 问题

当今,我们使用一些`通用目的的应用或者类库`去和其他用户进行交流.

比如,我们通常使用`HTTP客户端`去从`web服务器`获取信息,或者通过web services来进行RPC调用.

但是这些通用目的协议有时伸缩性不是特别好,比如说我们利用HTTP服务去上传大文件,邮件信息,近实时的金融消息或者多人游戏的数据.

还有一些需要特别优化的其他协议去实现这些功能.比如,基于HTPP的聊天应用,流媒体,大文件传输.你甚至可能想去设计和实现一个全新的协议,去精确定制你的需求.


## 解决方案

Netty致力于提供一个异步事件驱动的网络应用框架,快速的搭建一个可维护的高性能和高伸缩性协议的服务端和客户端.

换一句话说,Netty是一个NIO的客户端和服务端的框架,能狗快速和简单的开发网络应用,它简化了网络应用(TCP或者UDP)的开发.

'快速和简单'并不意味着利用Netty搭建的应用会有维护性或者性能问题.Netty借鉴了很多,能够做的很好 

## 开始

这个章节将简单的介绍Netty的examples,并且让你能够快速的使用netty.结束本章的阅读之后,你能够立即完成一个client和server

如果你想了解一下Netty框架自上而下的一些内容的话,你可以去访问一下 [Chapter 2, Architectural Overview](https://netty.io/3.8/guide/#architecture)

### 完成一个 Discard(抛弃) Server

这个世界上最简单的协议不是'HelloWorld'!而是 [`DISCARD`](http://tools.ietf.org/html/rfc863). 这个协议抛弃任何接收到的数据,没有任何返回

要实现这个`DISCARD`协议,需要去实现一个handler,这个handler是用来处理Netty生成的I/O事件的

```java
package io.netty.example.discard;

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 处理服务端的 channel.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // 抛弃接收到的数据
        ((ByteBuf) msg).release(); // (3)
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // 当有异常的时候关闭链接
        cause.printStackTrace();
        ctx.close();
    }
}
```

1. `DiscardServerHandler` 继承 [`ChannelInboundHandlerAdapter`], 实现了 [`ChannelInboundHandler`]. [`ChannelInboundHandler`] 提供了许多你可以重写的事件处理方法. 
2. 这里,我们重写了`channelRead()`这个事件处理方法.当新的数据从客户端发送过来时,这个方法会被调用.在这个例子中,接收到的消息类型为[`ByteBuf`]
3. 忽略了接收到的数据.[`ByteBuf`]是一个引用计数的对象,必须通过`release()`方法去释放掉.请记住这是handler的责任去释放掉引用计数的对象,通常`channelRead()`处理方法应该像下面这样实现:

  ```java
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
      try {
          // 处理msg
      } finally {
          ReferenceCountUtil.release(msg);
      }
  }
  ```

4.当有I/O异常或者handler在处理事件报错时,`exceptionCaught()` 事件处理方法会被调用.大多数在这种情况下,异常应该被记录,相关联的channel应该被关闭.

当目前为止,我们已经实现了`DISCARD`服务,接下来我们还剩下启动一个服务端来启动`DISCARD`服务:

```java
package io.netty.example.discard;
    
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
    
/**
 * 抛弃任何输入的数据
 */
public class DiscardServer {
    
    private int port;
    
    public DiscardServer(int port) {
        this.port = port;
    }
    
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new DiscardServerHandler());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
    
            // 绑定和开始接受连接
            ChannelFuture f = b.bind(port).sync(); // (7)
    
            // 等待直到服务端的socket关闭,在这个例子中,是不会发生的,但你这样做可以优雅的关闭服务端
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new DiscardServer(port).run();
    }
}
```

1. [`NioEventLoopGroup`] 是一个多线程的Eve ntLoop,用来处理I/0操作.Netty提供了不同的 [`EventLoopGroup`] 实现.我们打算实现一个服务端的应用,所以会使用两个[`NioEventLoopGroup`].第一个EventLoopGroup,通常叫'boss',接收接入的请求,第二个EventLoopGroup,通常叫'worker',一旦boss接收到连接,就会注册这个连接到worker.[`Channel`]对应的线程数依赖[`EventLoopGroup`]的实现方式,也可以通过构造函数进行配置.
2. [`ServerBootstrap`] 是一个配置服务端的帮助类,你可以直接用[`Channel`]配置服务端,但这个是一个枯燥的过程,大多数情况下,你不必这个样
3. 当有新的连接接入时,这里我们用[`NioServerSocketChannel`]类去实例化一个[`Channel`]
4. The handler specified here will always be evaluated by a newly accepted [`Channel`]. 
The [`ChannelInitializer`] is a special handler that is purposed to help a user configure a new [`Channel`].  
It is most likely that you want to configure the [`ChannelPipeline`] of the new [`Channel`] by adding some handlers such as `DiscardServerHandler` to implement your network application.  
As the application gets complicated, 
it is likely that you will add more handlers to the pipeline and extract this anonymous class into a top-level class eventually.
5. 你也可以设置一些参数,去配置`Channel`的实现.我们正在写TCP/IP的服务,所以我们允许设置socket的配置,比如`tcpNoDelay`和`keepAlive`.请参考[`ChannelOption`]和[`ChannelConfig`]来配置`ChannelOption`
6. 注意到 `option()` 和 `childOption()`了吗?  `option()` 是配置[`NioServerSocketChannel`]的,接收接入的请求.`childOption()` 是配置 [`Channel`]的,由父级 [`ServerChannel`]接入, 在这个例子中也是[`NioServerSocketChannel`]
7. 剩下的就是绑定端口和启动方服务了,这里我们绑定了机器上所有网卡的`8080`端口,你也可以调用`bind()`去绑定不同的端口

### 查看接收到的数据Data

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf in = (ByteBuf) msg;
    try {
        while (in.isReadable()) { // (1)
            System.out.print((char) in.readByte());
            System.out.flush();
        }
    } finally {
        ReferenceCountUtil.release(msg); // (2)
    }
}
```

1. 这个低效的循环可以被简化成: `System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII))`
2. 你可以用`in.release()`代替

完整的源代码可以在[`io.netty.example.discard`]中找到

### 完成一个EchoServer

```java
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.write(msg); // (1)
        ctx.flush(); // (2)
    }
```

1. [`ChannelHandlerContext`]对象提供了不同的操作,能够使你调用不同的I/O事件和操作.这里我们调用`write(Object)`把接收到的信息逐字的写回去.这里要注意的是,我们没有像`DISCARD`例子那样释放接收到的信息,因为Netty在我们调用write的时候会自动为我们释放.

2. `ctx.write(Object)` 会暂时的缓存write的内容,`ctx.flush()`会触发flush过程.你也可以调用替代`ctx.writeAndFlush(msg)`.

完整的源代码可以在[`io.netty.example.echo`]中找到

### 完成一个时间服务

时间服务,不同于之前的地方在于,发送一个32比特整数,不接受任何请求数据,发送数据后直接关闭消息. 

因为我们需要忽略到所有接收到的消息,当连接建立时,尽可能快的发送消息.所以这次我们不能用`channelRead()`.我们应该重写`channelActive()`方法.

```java
package io.netty.example.time;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        final ByteBuf time = ctx.alloc().buffer(4); // (2)
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
        
        final ChannelFuture f = ctx.writeAndFlush(time); // (3)
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
            }
        }); // (4)
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

1. 当连接建立的时候,`channelActive()`将会被调用
2. 为了发送消息,我们需要分配一个新的buffer去装载消息,因为我们需要输出一个32比特的整数,所以我们的[`ByteBuf`]容量至少是4字节.通过`ChannelHandlerContext.alloc()`方法可以获得[`ByteBufAllocator`]
3. 像通常一样,我们输出了构造好的消息.但是,为什么在发送消息之前,我们没有调用`java.nio.ByteBuffer.flip()`呢?`ByteBuf`没有这个方法因为它有两个指针,一个是读的,一个是写的.

   另一个注意点是`ChannelHandlerContext.write()`和`ChannelHandlerContext.writeAndFlush()`方法返回的[`ChannelFuture`].[`ChannelFuture`]代表了I/O操作还没有发生,因为在Netty中,所有的操作都是异步的.
   
   下面的例子可能会发生关闭连接在发送消息之前:
   
   ```java
   Channel ch = ...;
   ch.writeAndFlush(message);
   ch.close();
   ```

   因此你需要在`write()`返回的[`ChannelFuture`]完成事件之后,调用`close()`方法.值得一提的是,`close()`方法也不会立即关闭连接,它返回的还是一个[`ChannelFuture`].

4. 最简单的方法去获得write请求完成的通知,就是在返回的`ChannelFuture`上新增一个[`ChannelFutureListener`].
   
   这里我们创建了一个匿名的[`ChannelFutureListener`],你也可以使用内置的一个Listener:

   ```java
   f.addListener(ChannelFutureListener.CLOSE);
   ```

### 完成一个时间客户端

Netty服务端和客户端最大的不同点在于[`Bootstrap`]和[`Channel`]的实现:

```java
package io.netty.example.time;

public class TimeClient {
    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });
            
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
```

1. [`Bootstrap`]和[`ServerBootstrap`]相似,除了它是用来生成非服务端的Channel,比如一个客户端或者无连接的channel.
2. 如果只指定一个[`EventLoopGroup`],它将会把这个group同时给boos和worker用,boss worker在客户端是没有什么用处的.
3. [`NioSocketChannel`]被用来创建客户端的[`Channel`],替代之前的[`NioServerSocketChannel`].
4. 注意这里我们不用`childOption()`,因为客户端的[`SocketChannel`]没有parent
5. 这里我们用 `connect()` 方法替换之前的`bind()` 方法

如何实现[`ChannelHandler`]呢?

```java
package io.netty.example.time;

import java.util.Date;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg; // (1)
        try {
            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        } finally {
            m.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

1. 在 TCP/IP中, Netty解析data生成[`ByteBuf`].
这看起来十分简单,和服务端的代码并没有什么不同.然后这个handler有时候会拒绝工作,并且抛出`IndexOutOfBoundsException`.我们将在下一章节讨论为什么会发生这种情况.

### 处理流基础的协议(Stream-Based) 

#### 一个小的警告关于Socket Buffer

In a stream-based transport such as TCP/IP, received data is stored into a socket receive buffer. Unfortunately, the buffer of a stream-based transport is not a queue of packets but a queue of bytes. It means, even if you sent two messages as two independent packets, an operating system will not treat them as two messages but as just a bunch of bytes. Therefore, there is no guarantee that what you read is exactly what your remote peer wrote. For example, let us assume that the TCP/IP stack of an operating system has received three packets: 

![Three packets received as they were sent](https://github.com/djxhero/some_little_thing/blob/master/res/images/netty/1.png)

Because of this general property of a stream-based protocol, there's a high chance of reading them in the following fragmented form in your application:

![Three packets split and merged into four buffers](https://github.com/djxhero/some_little_thing/blob/master/res/images/netty/2.png)

Therefore, a receiving part, regardless it is server-side or client-side, should defrag the received data into one or more meaningful frames that could be easily understood by the application logic. In the case of the example above, the received data should be framed like the following:

![Four buffers defragged into three](https://github.com/djxhero/some_little_thing/blob/master/res/images/netty/3.png)

#### The First Solution

Now let us get back to the `TIME` client example. We have the same problem here. A 32-bit integer is a very small amount of data, and it is not likely to be fragmented often. However, the problem is that it can be fragmented, and the possibility of fragmentation will increase as the traffic increases.

The simplistic solution is to create an internal cumulative buffer and wait until all 4 bytes are received into the internal buffer. The following is the modified `TimeClientHandler` implementation that fixes the problem:

```java
package io.netty.example.time;

import java.util.Date;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    private ByteBuf buf;
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(4); // (1)
    }
    
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release(); // (1)
        buf = null;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        buf.writeBytes(m); // (2)
        m.release();
        
        if (buf.readableBytes() >= 4) { // (3)
            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

1. A [`ChannelHandler`] has two life cycle listener methods: `handlerAdded()` and `handlerRemoved()`.  You can perform an arbitrary (de)initialization task as long as it does not block for a long time.
1. First, all received data should be cumulated into `buf`. 
1. And then, the handler must check if `buf` has enough data, 4 bytes in this example, and proceed to the actual business logic. Otherwise, Netty will call the `channelRead()` method again when more data arrives, and eventually all 4 bytes will be cumulated.

#### The Second Solution

Although the first solution has resolved the problem with the `TIME` client, the modified handler does not look that clean. Imagine a more complicated protocol which is composed of multiple fields such as a variable length field. Your [`ChannelInboundHandler`] implementation will become unmaintainable very quickly.

As you may have noticed, you can add more than one [`ChannelHandler`] to a [`ChannelPipeline`], and therefore, you can split one monolithic [`ChannelHandler`] into multiple modular ones to reduce the complexity of your application. For example, you could split `TimeClientHandler` into two handlers:

* `TimeDecoder` which deals with the fragmentation issue, and
* the initial simple version of `TimeClientHandler`.

Fortunately, Netty provides an extensible class which helps you write the first one out of the box:

```java
package io.netty.example.time;

public class TimeDecoder extends ByteToMessageDecoder { // (1)
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        if (in.readableBytes() < 4) {
            return; // (3)
        }
        
        out.add(in.readBytes(4)); // (4)
    }
}
```

1. [`ByteToMessageDecoder`] is an implementation of [`ChannelInboundHandler`] which makes it easy to deal with the fragmentation issue.
2. [`ByteToMessageDecoder`] calls the `decode()` method with an internally maintained cumulative buffer whenever new data is received.
3. `decode()` can decide to add nothing to `out` when there is not enough data in the cumulative buffer.  [`ByteToMessageDecoder`] will call `decode()` again when there is more data received.
4. If `decode()` adds an object to `out`, it means the decoder decoded a message successfully.  [`ByteToMessageDecoder`] will discard the read part of the cumulative buffer.  Please remember that you don't need to decode multiple messages. [`ByteToMessageDecoder`] will keep calling the `decode()` method until it adds nothing to `out`.

Now that we have another handler to insert into the [`ChannelPipeline`], we should modify the [`ChannelInitializer`] implementation in the `TimeClient`:

```java
b.handler(new ChannelInitializer<SocketChannel>() {
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
    }
});
```

If you are an adventurous person, you might want to try the [`ReplayingDecoder`] which simplifies the decoder even more. You will need to consult the API reference for more information though.

```java
public class TimeDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(
            ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        out.add(in.readBytes(4));
    }
}
```

Additionally, Netty provides out-of-the-box decoders which enables you to implement most protocols very easily and helps you avoid from ending up with a monolithic unmaintainable handler implementation. Please refer to the following packages for more detailed examples: 

* [`io.netty.example.factorial`] for a binary protocol, and
* [`io.netty.example.telnet`] for a text line-based protocol.

### Speaking in POJO instead of `ByteBuf`

All the examples we have reviewed so far used a [`ByteBuf`] as a primary data structure of a protocol message. In this section, we will improve the `TIME` protocol client and server example to use a POJO instead of a [`ByteBuf`].

The advantage of using a POJO in your [`ChannelHandler`]s is obvious; your handler becomes more maintainable and reusable by separating the code which extracts information from `ByteBuf` out from the handler. In the `TIME` client and server examples, we read only one 32-bit integer and it is not a major issue to use `ByteBuf` directly. However, you will find it is necessary to make the separation as you implement a real-world protocol.

First, let us define a new type called `UnixTime`.

```java
package io.netty.example.time;

import java.util.Date;

public class UnixTime {

    private final long value;
    
    public UnixTime() {
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }
    
    public UnixTime(long value) {
        this.value = value;
    }
        
    public long value() {
        return value;
    }
        
    @Override
    public String toString() {
        return new Date((value() - 2208988800L) * 1000L).toString();
    }
}
```

We can now revise the `TimeDecoder` to produce a `UnixTime` instead of a [`ByteBuf`].

```java
@Override
protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    if (in.readableBytes() < 4) {
        return;
    }

    out.add(new UnixTime(in.readUnsignedInt()));
}
```

With the updated decoder, the `TimeClientHandler` does not use [`ByteBuf`] anymore:

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    UnixTime m = (UnixTime) msg;
    System.out.println(m);
    ctx.close();
}
```

Much simpler and elegant, right? The same technique can be applied on the server side. Let us update the `TimeServerHandler` first this time:

```java
@Override
public void channelActive(ChannelHandlerContext ctx) {
    ChannelFuture f = ctx.writeAndFlush(new UnixTime());
    f.addListener(ChannelFutureListener.CLOSE);
}
```

Now, the only missing piece is an encoder, which is an implementation of [`ChannelOutboundHandler`] that translates a `UnixTime` back into a [`ByteBuf`]. It's much simpler than writing a decoder because there's no need to deal with packet fragmentation and assembly when encoding a message.

```java
package io.netty.example.time;

public class TimeEncoder extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        UnixTime m = (UnixTime) msg;
        ByteBuf encoded = ctx.alloc().buffer(4);
        encoded.writeInt((int)m.value());
        ctx.write(encoded, promise); // (1)
    }
}
```

1. There are quite a few important things in this single line.

   First, we pass the original [`ChannelPromise`] as-is so that Netty marks it as success or failure when the encoded data is actually written out to the wire.
   
   Second, we did not call `ctx.flush()`.  There is a separate handler method `void flush(ChannelHandlerContext ctx)` which is purposed to override the `flush()` operation.

To simplify even further, you can make use of [`MessageToByteEncoder`]:

```java
public class TimeEncoder extends MessageToByteEncoder<UnixTime> {
    @Override
    protected void encode(ChannelHandlerContext ctx, UnixTime msg, ByteBuf out) {
        out.writeInt((int)msg.value());
    }
}

```

The last task left is to insert a `TimeEncoder` into the [`ChannelPipeline`] on the server side before the `TimeServerHandler`, and it is left as a trivial exercise.

### Shutting Down Your Application

Shutting down a Netty application is usually as simple as shutting down all [`EventLoopGroup`]s you created via `shutdownGracefully()`.  It returns a [`Future`] that notifies you when the [`EventLoopGroup`] has been terminated completely and all [`Channel`]s that belong to the group have been closed.

### Summary

In this chapter, we had a quick tour of Netty with a demonstration on how to write a fully working network application on top of Netty.

There is more detailed information about Netty in the upcoming chapters. We also encourage you to review the Netty examples in the [`io.netty.example`] package.

Please also note that [the community](http://netty.io/community.html) is always waiting for your questions and ideas to help you and keep improving Netty and its documentation based on your feedback. 

[`Bootstrap`]: http://netty.io/4.1/api/io/netty/bootstrap/Bootstrap.html
[`ByteBuf`]: http://netty.io/4.1/api/io/netty/buffer/ByteBuf.html
[`ByteBufAllocator`]: http://netty.io/4.1/api/io/netty/buffer/ByteBufAllocator.html
[`ByteToMessageDecoder`]: http://netty.io/4.1/api/io/netty/handler/codec/ByteToMessageDecoder.html
[`Channel`]: http://netty.io/4.1/api/io/netty/channel/Channel.html
[`ChannelConfig`]: http://netty.io/4.1/api/io/netty/channel/ChannelConfig.html
[`ChannelFuture`]: http://netty.io/4.1/api/io/netty/channel/ChannelFuture.html
[`ChannelFutureListener`]: http://netty.io/4.1/api/io/netty/channel/ChannelFutureListener.html
[`ChannelHandlerContext`]: http://netty.io/4.1/api/io/netty/channel/ChannelHandlerContext.html
[`ChannelHandler`]: http://netty.io/4.1/api/io/netty/channel/ChannelHandler.html
[`ChannelInboundHandler`]: http://netty.io/4.1/api/io/netty/channel/ChannelInboundHandler.html
[`ChannelInboundHandlerAdapter`]: http://netty.io/4.1/api/io/netty/channel/ChannelInboundHandlerAdapter.html
[`ChannelInitializer`]: http://netty.io/4.1/api/io/netty/channel/ChannelInitializer.html
[`ChannelOption`]: http://netty.io/4.1/api/io/netty/channel/ChannelOption.html
[`ChannelOutboundHandler`]: http://netty.io/4.1/api/io/netty/channel/ChannelOutboundHandler.html

[`ChannelPipeline`]: http://netty.io/4.1/api/io/netty/channel/ChannelPipeline.html
[`ChannelPromise`]: http://netty.io/4.1/api/io/netty/channel/ChannelPromise.html
[`EventLoopGroup`]: http://netty.io/4.1/api/io/netty/channel/EventLoopGroup.html
[`Future`]: http://netty.io/4.1/api/io/netty/util/concurrent/Future.html
[`MessageToByteEncoder`]: http://netty.io/4.1/api/io/netty/handler/codec/MessageToByteEncoder.html
[`NioEventLoopGroup`]: http://netty.io/4.1/api/io/netty/channel/nio/NioEventLoopGroup.html
[`NioServerSocketChannel`]: http://netty.io/4.1/api/io/netty/channel/socket/nio/NioServerSocketChannel.html
[`NioSocketChannel`]: http://netty.io/4.1/api/io/netty/channel/socket/nio/NioSocketChannel.html
[`ReplayingDecoder`]: http://netty.io/4.1/api/io/netty/handler/codec/ReplayingDecoder.html
[`ServerBootstrap`]: http://netty.io/4.1/api/io/netty/bootstrap/ServerBootstrap.html
[`ServerChannel`]: http://netty.io/4.1/api/io/netty/channel/ServerChannel.html
[`SocketChannel`]: http://netty.io/4.1/api/io/netty/channel/socket/SocketChannel.html

[`io.netty.example`]: https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example
[`io.netty.example.discard`]: http://netty.io/4.1/xref/io/netty/example/discard/package-summary.html
[`io.netty.example.echo`]: http://netty.io/4.1/xref/io/netty/example/echo/package-summary.html
[`io.netty.example.factorial`]: http://netty.io/4.1/xref/io/netty/example/factorial/package-summary.html
[`io.netty.example.telnet`]: http://netty.io/4.1/xref/io/netty/example/telnet/package-summary.html

```
ChannelInboundHandlerAdapter <- ByteToMessageDecoder <- DelimiterBasedFrameDecoder
ChannelInboundHandlerAdapter <- ByteToMessageDecoder <- FixedLengthFrameDecoder
ChannelInboundHandlerAdapter <- ByteToMessageDecoder <- LengthFieldBasedFrameDecoder
ChannelInboundHandlerAdapter <- ByteToMessageDecoder <- ReplayingDecoder<T>
ChannelInboundHandlerAdapter <- MessageToMessageDecoder<T> <- StringDecoder
ChannelInboundHandlerAdapter <- SimpleChannelInboundHandler<T>

ChannelOutboundHandlerAdapter <- MessageToByteEncoder<T>
ChannelOutboundHandlerAdapter <- MessageToMessageEncoder<T> <- StringEncoder
```