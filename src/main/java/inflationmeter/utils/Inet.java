package inflationmeter.utils;

import java.net.InetSocketAddress;
import java.net.Socket;

public class Inet {

    public static boolean isProxyAvailable(final String ip, final int port, final int timeout) {
        try (final Socket socket = new Socket()) {
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
            socket.connect(inetSocketAddress, timeout);
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

}
