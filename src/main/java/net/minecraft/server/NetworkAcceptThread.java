package net.minecraft.server;

import com.legacyminecraft.poseidon.PoseidonConfig;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

class NetworkAcceptThread extends Thread {
    private static final int throttleDelay = PoseidonConfig.getInstance().getInt("settings.connection-throttle", 5000);
    private static final byte[] disconnectPacket;

    final MinecraftServer a;

    final NetworkListenThread b;

    NetworkAcceptThread(NetworkListenThread networklistenthread, String s, MinecraftServer minecraftserver) {
        super(s);
        this.b = networklistenthread;
        this.a = minecraftserver;
    }

    static {
        var packet = new Packet255KickDisconnect(PoseidonConfig.getInstance().getString("message.kick.throttled"));

        var byteArrayOutputStream = new ByteArrayOutputStream();
        try (var dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            Packet.a(packet, dataOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Exception creating throttle disconnect packet", e);
        }

        disconnectPacket = byteArrayOutputStream.toByteArray();
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

                    if (now - lastConnection < throttleDelay) {
                        socket.getOutputStream().write(disconnectPacket);
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
