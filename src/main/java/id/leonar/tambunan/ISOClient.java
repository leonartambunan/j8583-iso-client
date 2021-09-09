package id.leonar.tambunan;

import com.github.kpavlov.jreactive8583.IsoMessageListener;
import com.github.kpavlov.jreactive8583.client.ClientConfiguration;
import com.github.kpavlov.jreactive8583.client.Iso8583Client;
import com.github.kpavlov.jreactive8583.iso.ISO8583Version;
import com.github.kpavlov.jreactive8583.iso.J8583MessageFactory;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ISOClient {
    public static final Logger logger = LoggerFactory.getLogger(ISOClient.class);
    private final static int port = 7501;
    private final static String host = "localhost";
    public static void main(String[] args) throws Exception {
        MessageFactory<IsoMessage> mf = new MessageFactory<>();
        ConfigParser.configureFromClasspathConfig(mf,"j8583.xml");
        J8583MessageFactory<IsoMessage> messageFactory = new J8583MessageFactory<>(mf, ISO8583Version.V1987);// [1]

        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
                .addLoggingHandler(true)
                .logSensitiveData(true)
                .workerThreadsCount(4)
                .replyOnError(true)
                .workerThreadsCount(12)
                .idleTimeout(0)
                .build();

        SocketAddress serverAddress = new InetSocketAddress(host,port);
        final Iso8583Client<IsoMessage> client = new Iso8583Client<IsoMessage>(serverAddress,clientConfiguration, messageFactory);// [2]

        client.addMessageListener(new IsoMessageListener<IsoMessage>() {
            public boolean onMessage(@NotNull ChannelHandlerContext ctx, @NotNull IsoMessage isoMessage) {

                IsoMessage response = client.getIsoMessageFactory().createResponse(isoMessage);

                response.setField(39, IsoType.ALPHA.value("00", 2));

                logger.info("------------------");
                logger.info(response.debugString());

                ctx.writeAndFlush(response);

                return false;
            }

            public boolean applies(@NotNull IsoMessage isoMessage) {
                return true;
            } // [3]


        });

        clientConfiguration.replyOnError();
        client.init();

        client.connect();// [6]
        if (client.isConnected()) { // [7]
            logger.info("ding dong");
            //you can send the message here
        }
    }
}
