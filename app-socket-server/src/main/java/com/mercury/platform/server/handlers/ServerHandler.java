package com.mercury.platform.server.handlers;

import com.mercury.platform.holder.UpdateHolder;
import com.mercury.platform.server.bus.UpdaterServerAsyncEventBus;
import com.mercury.platform.server.bus.event.ClientActiveEvent;
import com.mercury.platform.server.bus.event.ClientUnregisteredEvent;
import com.mercury.platform.server.bus.event.ClientUpdatedEvent;
import com.mercury.platform.update.AlreadyLatestUpdateMessage;
import com.mercury.platform.update.PatchNotesDescriptor;
import com.mercury.platform.update.UpdateDescriptor;
import com.mercury.platform.update.UpdateType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Created by Frost on 14.01.2017.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private String json = "{\n" +
            "  \"version\":\"1.0.0.3\",\n" +
            "  \"notes\":[\n" +
            "  {\n" +
            "    \"title\" : \"New update\",\n" +
            "    \"text\" : \"- Added a button to TaskBar - \\\"Travel to hideout\\\".\\n- Added a button to History - \\\"Clear History\\\".\\n- Added a button to History - \\\"Restore notification panel\\\".\\n- Added a checkbox option - \\\"Close Notification panel on Kick\\\".\\n- Added checkboxes for allowing Response buttons to close notification panel on click. See settings.\\n- Increased character limit on \\\"Label\\\" text field: You can now have longer shortcut names.\\n- Fixed a bug with response buttons not being properly removed.\",\n" +
            "    \"image\" : \"\",\n" +
            "    \"layout\" : \"VERTICAL\"\n" +
            "  }\n" +
            "]}";
    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class.getSimpleName());
    private UpdaterServerAsyncEventBus eventBus = UpdaterServerAsyncEventBus.getInstance();
    private UpdateHolder updateHolder = UpdateHolder.getInstance();

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        InetSocketAddress address = (InetSocketAddress) context.channel().remoteAddress();
        eventBus.post(new ClientActiveEvent(address));
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {

        if (msg instanceof UpdateDescriptor) {
            UpdateDescriptor descriptor = (UpdateDescriptor) msg;
            if (descriptor.getVersion() < updateHolder.getVersion()) {
                switch (descriptor.getType()) {
                    case REQUEST_PATCH_NOTES:{
                        PatchNotesDescriptor patchDescriptor = new PatchNotesDescriptor(json);
                        context.channel().writeAndFlush(patchDescriptor);
                        break;
                    }
                    case REQUEST_INFO: {
                        UpdateDescriptor updateDescriptor = new UpdateDescriptor(UpdateType.REQUEST_INFO,updateHolder.getVersion());
                        context.channel().writeAndFlush(updateDescriptor);
                        break;
                    }
                    case REQUEST_UPDATE: {
                        byte[] update = UpdateHolder.getInstance().getUpdate();
                        context.channel().writeAndFlush(update.length);
                        int chunkSize = 800 * 1024;
                        int chunkStart = 0;
                        int chunkEnd = 0;

                        while (chunkStart < update.length) {
                            if (chunkStart + chunkSize > update.length) {
                                chunkSize = update.length - chunkStart;
                            }

                            chunkEnd = chunkStart + chunkSize;

                            context.channel().writeAndFlush(Arrays.copyOfRange(update, chunkStart, chunkEnd));

                            chunkStart += chunkSize;
                        }
                        InetSocketAddress address = (InetSocketAddress) context.channel().remoteAddress();
                        eventBus.post(new ClientUpdatedEvent(address));
                        break;
                    }
                }
            }else {
                context.channel().writeAndFlush(new AlreadyLatestUpdateMessage());
            }
        }else if(msg instanceof Integer){
            Integer version = (Integer)msg;
            if(version < updateHolder.getVersion()) {
                byte[] update = UpdateHolder.getInstance().getUpdate();
                context.channel().writeAndFlush(update.length);
                int chunkSize = 800 * 1024;
                int chunkStart = 0;
                int chunkEnd = 0;

                while (chunkStart < update.length) {
                    if (chunkStart + chunkSize > update.length) {
                        chunkSize = update.length - chunkStart;
                    }

                    chunkEnd = chunkStart + chunkSize;

                    context.channel().writeAndFlush(Arrays.copyOfRange(update, chunkStart, chunkEnd));

                    chunkStart += chunkSize;
                }
                InetSocketAddress address = (InetSocketAddress) context.channel().remoteAddress();
                eventBus.post(new ClientUpdatedEvent(address));
            }
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Channel {} read complete" , ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
    }


    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {

    }


    @Override
    public void channelUnregistered(ChannelHandlerContext context) throws Exception {
        LOGGER.info("{} channel is unregistered" , this);
        InetSocketAddress address = (InetSocketAddress) context.channel().remoteAddress();
        eventBus.post(new ClientUnregisteredEvent(address));
    }
}
