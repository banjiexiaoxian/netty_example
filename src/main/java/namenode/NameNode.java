package namenode;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import server.TimeServer;

/**
 * Created by hadoop on 17-5-25.
 */
public class NameNode {
    private int port;


    public static void main(String args[]) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new TimeServer(port).run();
    }

    public NameNode(int port) {
        this.port = port;
    }

    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();//默认Thread数目为DEFAULT_EVENT_LOOP_THREADS=CPU*2
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NameNodeHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

        class NameNodeHandler extends ChannelInboundHandlerAdapter {
            @Override
            public void channelActive(final ChannelHandlerContext ctx) {
                final ByteBuf info = Unpooled.buffer();
                info.writeBytes("hello i`m Namenode".getBytes());

                final ChannelFuture f = ctx.writeAndFlush(info);
                f.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        System.out.print("Server has sent the info");
                        assert f == future;
                        ctx.close();
                    }
                });
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                cause.printStackTrace();
                ctx.close();
            }
        }

}
