package client;

import codec.MsgDecoder;
import codec.MsgEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Client用来连接其他节点进行通信的类
 * Created by hadoop on 17-5-17.
 */
public class ClientConnector {
    int port;
    String host;
    static List<String> message = new ArrayList<String>();

    public ClientConnector(String host, int port){
        this.port = port;
        this.host = host;

    }

    public void start() throws Exception{
        //与Namenode建立连接
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap b_namenode = new Bootstrap();
            b_namenode.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host,port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new namenodeHandler());
//                            ch.pipeline().addLast(new MsgEncoder());
//                            ch.pipeline().addLast(new DispatcherHandler());
//                            ch.pipeline().addLast(new MsgDecoder());
                        }
                    });
            ChannelFuture f = b_namenode.connect().sync();
//            f.addListener(new ChannelFutureListener() {
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    System.out.println("namenode has connected"+System.currentTimeMillis());
//                }
//            });
            f.channel().closeFuture().sync();

            //与Datanode建立连接
            Bootstrap b_datanode = new Bootstrap();
            b_datanode.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host,8081))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                                ch.pipeline().addLast(new datanodeHandler());
//                            ch.pipeline().addLast(new MsgEncoder());
//                            ch.pipeline().addLast(new DispatcherHandler());
//                            ch.pipeline().addLast(new MsgDecoder());
                        }
                    });

            ChannelFuture f_datanode = b_datanode.connect().sync();

//            f_datanode.addListener(new ChannelFutureListener() {
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    System.out.println("datanode has connected"+System.currentTimeMillis());
//                }
//            });

            f_datanode.channel().closeFuture().sync();
            System.out.println(message.size());

        }finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String args[]) throws Exception{
//        if(args.length != 2){
//            System.err.println(
//                    "Usage :" + ClientImpl.class.getSimpleName()+
//                            "<host><port>"
//            );
//            return;
//        }
//
//        final String host = args[0];
//        final int port = Integer.parseInt(args[1]);

        new ClientConnector("localhost", 8080).start();

    }

    class datanodeHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Connnecting to datanode"+System.currentTimeMillis());
            System.out.println(message.size());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf)msg;
            System.out.println(in.toString(CharsetUtil.UTF_8));
            message.add(in.toString());
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    class namenodeHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Connnecting to namenode"+System.currentTimeMillis());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf)msg;
            System.out.println(in.toString(CharsetUtil.UTF_8));
            message.add(in.toString());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
