package com.corn.sprngboot.websocket.demo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接收/处理/响应客户端websocket请求的核心业务处理类
 * @author liuyazhuang
 *
 */

@Component
public class MyNettyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

	/**用户id session**/
	private static final Map<String, ChannelHandlerContext> sessionsMap = new ConcurrentHashMap<>();
	
	private WebSocketServerHandshaker handshaker;

	private static final String WEB_SOCKET_URL = "ws://localhost:8888/websocket";
	//客户端与服务端创建连接的时候调用
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		NettyConfig.group.add(ctx.channel());
	}

	//客户端与服务端断开连接的时候调用
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		NettyConfig.group.remove(ctx.channel());
		String userId = ctx.channel().attr(AttributeKey.valueOf("userId")).get().toString();
		System.out.println("用户ID："+userId+"离线了");
		if(sessionsMap.containsKey(userId)){
			sessionsMap.remove(userId);
		}
		System.out.println("客户端与服务端连接关闭...");
	}

	//服务端接收客户端发送过来的数据结束之后调用
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
	}

	//工程出现异常的时候调用
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	//服务端处理客户端websocket请求的核心方法
	@Override
	protected void channelRead0(ChannelHandlerContext context, Object msg) throws Exception {
		//处理客户端向服务端发起http握手请求的业务
		if (msg instanceof FullHttpRequest) {
			handHttpRequest(context,  (FullHttpRequest)msg);
		}else if (msg instanceof WebSocketFrame) { //处理websocket连接业务
			handWebsocketFrame(context, (WebSocketFrame)msg);
		}
	}
//	@Override
//	protected void messageReceived(ChannelHandlerContext context, Object msg) throws Exception {
//		//处理客户端向服务端发起http握手请求的业务
//		if (msg instanceof FullHttpRequest) {
//			handHttpRequest(context,  (FullHttpRequest)msg);
//		}else if (msg instanceof WebSocketFrame) { //处理websocket连接业务
//			handWebsocketFrame(context, (WebSocketFrame)msg);
//		}
//	}
	
	/**
	 * 处理客户端与服务端之前的websocket业务
	 * @param ctx
	 * @param frame
	 */
	private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame){
		System.out.println("开始处理websocket的消息"+ctx.attr(AttributeKey.valueOf("userId")).get().toString());
		//判断是否是关闭websocket的指令
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
		}
		//判断是否是ping消息
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		
		//判断是否是二进制消息，如果是二进制消息，抛出异常
		if( ! (frame instanceof TextWebSocketFrame) ){
			System.out.println("目前我们不支持二进制消息");
			throw new RuntimeException("【"+this.getClass().getName()+"】不支持消息");
		}
		//返回应答消息
		//获取客户端向服务端发送的消息
		String request = ((TextWebSocketFrame) frame).text();
		System.out.println("服务端收到客户端的消息====>>>" + request);
		TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString() 
									+ ctx.channel().id()
									+ " ===>>> "
									+ request);
		//群发，服务端向每个连接上来的客户端群发消息
		NettyConfig.group.writeAndFlush(tws);
	}
	/**
	 * 处理客户端向服务端发起http握手请求的业务
	 * @param ctx
	 * @param req
	 */
	private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req){
		/**获取http握手请求url上面的参数：ws://127.0.0.1:12345/websocket?id=1 **/
		QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
		Map<String, List<String>> parame = decoder.parameters();
		String id = parame.get("id").get(0);
		System.out.println("id"+id);
		//设置ctx上面channel的属性，用于参数传递
		ctx.channel().attr(AttributeKey.newInstance("userId")).set(id);
		if (!req.getDecoderResult().isSuccess() 
				|| ! ("websocket".equals(req.headers().get("Upgrade")))) {
			sendHttpResponse(ctx, req, 
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				WEB_SOCKET_URL, null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		}else{
			handshaker.handshake(ctx.channel(), req);
			/**握手完成，保存userId与ctx上下文**/
			sessionsMap.put(id,ctx);
			System.out.println("用户ID："+id+"上线了");

			System.out.println("http握手完成");
		}
	}

	/**
	 * 给某个用户发送消息
	 * @param message
	 * @param userId
	 * @return
	 * @throws IOException
	 */
	public void sendMessage(String message,String userId) throws IOException {
		if(sessionsMap.containsKey(userId)){
			ChannelHandlerContext ctx = sessionsMap.get(userId);
			TextMessage textMessage = new TextMessage(message);
			TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
					+ ctx.channel().id()
					+ ":from netty server===>>> "
					+ message);

			/**发送消息**/
			ctx.channel().writeAndFlush(tws);
			System.out.println("finish send!");
		}
	}
	
	/**
	 * 服务端向客户端响应消息
	 * @param ctx
	 * @param req
	 * @param res
	 */
	private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
			DefaultFullHttpResponse res){
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
		}
		//服务端向客户端发送数据
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		System.out.println(1111111111);
		if (res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}


}
