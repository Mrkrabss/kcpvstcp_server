package io.jpower.kcp.example.rtt;

import io.jpower.kcp.example.echo.EchoServerHandler;
import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpServerChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.UkcpServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Measures RTT(Round-trip time) for KCP.
 * <p>
 * Receives a message from client and sends a response.
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class KcpRttServer {

    static final int CONV = Integer.parseInt(System.getProperty("conv", "10"));

    public static void main(String[] args) throws Exception {

        new Thread(new Runnable() {

            @Override
            public void run() {

                // Configure the server.
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    UkcpServerBootstrap b = new UkcpServerBootstrap();
                    b.group(group)
                            .channel(UkcpServerChannel.class)
                            .childHandler(new ChannelInitializer<UkcpChannel>() {
                                @Override
                                public void initChannel(UkcpChannel ch) throws Exception {
                                    ChannelPipeline p = ch.pipeline();
                                    p.addLast(new KcpRttServerHandler());
                                }
                            });
                    ChannelOptionHelper.nodelay(b, true, 20, 2, true)
                            .childOption(UkcpChannelOption.UKCP_MTU, 512);

                    // Start the server.
                    ChannelFuture f = b.bind(8080).sync();

                    // Wait until the server socket is closed.
                    f.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Shut down all event loops to terminate all threads.
                    group.shutdownGracefully();
                }
            }

        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(group)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline p = ch.pipeline();
                                    p.addLast(new TcpRttServerHandler());
                                }
                            }).childOption(ChannelOption.TCP_NODELAY, true);

                    // Start the server.
                    ChannelFuture f = b.bind(8081).sync();

                    // Wait until the server socket is closed.
                    f.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Shut down all event loops to terminate all threads.
                    group.shutdownGracefully();
                }

            }

        }).start();
        

    }

}
