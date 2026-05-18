package net.minecraft.server;

import com.legacyminecraft.poseidon.PoseidonConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

class NetworkAcceptThread extends Thread {
    private final int throttleDelay = PoseidonConfig.getInstance().getInt("settings.connection-throttle", 5000);

    final MinecraftServer a;

    final NetworkListenThread b;

    NetworkAcceptThread(NetworkListenThread networklistenthread, String s, MinecraftServer minecraftserver) {
        super(s);
        this.b = networklistenthread;
        this.a = minecraftserver;
    }

    public void run() {
        HashMap<InetAddress, Long> hashmap = new HashMap<>();

        while (this.b.b) {
            try {
                Socket socket = NetworkListenThread.a(this.b).accept();

                if (socket != null) {
                    InetAddress inetaddress = socket.getInetAddress();
                    long now = System.currentTimeMillis();
                    long lastConnection = hashmap.getOrDefault(inetaddress, 0L);

                    if (now - lastConnection < throttleDelay && !"127.0.0.1".equals(inetaddress.getHostAddress())) {
                        socket.close();
                    } else {
                        hashmap.put(inetaddress, now);
                        NetLoginHandler netloginhandler = new NetLoginHandler(this.a, socket, "Connection #" + NetworkListenThread.b(this.b));

                        NetworkListenThread.a(this.b, netloginhandler);
                    }
                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
        }
    }
}
