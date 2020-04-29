package com.panamera.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
//import org.json.JSONObject;

import com.panamera.common.protocol.ProxyMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * @Description ProxyMessage编码器
 * @Author zhangjun
 * @Data 2020/2/20
 * @Time 14:01
 */

public class ProxyMessageEncoder extends MessageToByteEncoder<ProxyMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage, ByteBuf byteBuf) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int type = proxyMessage.getType();
        dos.writeInt(type);

//        JSONObject metaDataJson = new JSONObject(proxyMessage.getMetaData());
//        byte[] metaDataBytes = metaDataJson.toString().getBytes(CharsetUtil.UTF_8);
//        dos.writeInt(metaDataBytes.length);
//        dos.write(metaDataBytes);

        if (proxyMessage.getData()!=null && proxyMessage.getData().length>0){
            dos.write(proxyMessage.getData());
        }

        byte[] data = baos.toByteArray();
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
