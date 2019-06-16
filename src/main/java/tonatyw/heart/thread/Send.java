package tonatyw.heart.thread;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.codec.digest.DigestUtils;

import com.alibaba.fastjson.JSONObject;

import tonatyw.heart.HeartBox;
import tonatyw.heart.constants.Constants;

/**
 * 心跳上报线程类
 * @ClassName Upper
 * @Description: 心跳上报线程类
 * @date 2019年6月10日 下午2:03:37
 */
public class Send implements Runnable{
	/** 连接对象 */
	private DatagramSocket socket;
	/** 心跳信息 */
	private HeartBox heartBox;
	public Send(DatagramSocket ds,HeartBox hb){
		this.socket = ds;
		this.heartBox = hb;
		// 初始化父级消息队列
		heartBox.getMsgQueueMap().put(heartBox.getpIp(), new ArrayBlockingQueue<String>(1000,true));
	}
	@Override
	public void run() {
		// 组装数据
		JSONObject json = new JSONObject();
		String msg = heartBox.getMsgQueueMap().get(heartBox.getpIp()).poll();
		msg = msg==null?"":msg;
		json.put(Constants.HeartMessage.MSG, msg);
		json.put(Constants.HeartMessage.SIGN, DigestUtils.md5Hex(msg));
		json.put(Constants.HeartMessage.MSGTYPE, 1);
		try {
			// 发送数据
			String jsonStr = json.toJSONString();
			byte[] bytes = jsonStr.getBytes(Constants.HeartMessage.ENCODING_UTF_8);
			DatagramPacket dp = new DatagramPacket(bytes, bytes.length, new InetSocketAddress(heartBox.getpIp(), heartBox.getpPort()));
			socket.send(dp);
			// 将数据存入缓冲队列,如果返回确定收到，将队列删除
			heartBox.getAckMap().put(json.getString(Constants.HeartMessage.SIGN), jsonStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}