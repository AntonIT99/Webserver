package com.wolfsnetz.webserver.minecraft;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Service
public class MinecraftStatusService {

    private final MinecraftProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MinecraftStatusService(MinecraftProperties properties)
    {
        this.properties = properties;
    }

    public MinecraftStatus getStatus()
    {
        try (Socket socket = new Socket())
        {
            socket.connect(
                new java.net.InetSocketAddress(properties.host(), properties.port()),
                properties.timeoutMs()
            );
            socket.setSoTimeout(properties.timeoutMs());

            DataInputStream in = new DataInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();

            sendHandshake(out);
            sendStatusRequest(out);

            int packetLength = readVarInt(in);
            int packetId = readVarInt(in);

            if (packetId != 0x00)
            {
                return new MinecraftStatus(false, null, 0, 0, null, "Invalid packet");
            }

            int jsonLength = readVarInt(in);
            byte[] jsonBytes = in.readNBytes(jsonLength);
            String json = new String(jsonBytes, StandardCharsets.UTF_8);

            JsonNode root = objectMapper.readTree(json);

            String version = root.path("version").path("name").asString("unknown");
            int online = root.path("players").path("online").asInt(0);
            int max = root.path("players").path("max").asInt(0);

            String motd = extractMotd(root.path("description"));

            return new MinecraftStatus(true, version, online, max, motd, null);

        }
        catch (Exception e)
        {
            return new MinecraftStatus(false, null, 0, 0, null, e.getMessage());
        }
    }

    private void sendHandshake(OutputStream out) throws IOException
    {
        ByteArrayOutputStream data = new ByteArrayOutputStream();

        writeVarInt(data, 754); // Protocol 1.16.5+, reicht für Status-Ping meistens
        writeString(data, properties.host());
        data.write((properties.port() >>> 8) & 0xFF);
        data.write(properties.port() & 0xFF);
        writeVarInt(data, 1); // next state: status

        sendPacket(out, 0x00, data.toByteArray());
    }

    private void sendStatusRequest(OutputStream out) throws IOException
    {
        sendPacket(out, 0x00, new byte[0]);
    }

    private void sendPacket(OutputStream out, int packetId, byte[] data) throws IOException
    {
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        writeVarInt(packet, packetId);
        packet.write(data);

        byte[] packetBytes = packet.toByteArray();
        writeVarInt(out, packetBytes.length);
        out.write(packetBytes);
        out.flush();
    }

    private String extractMotd(JsonNode description)
    {
        if (description.isString())
        {
            return description.asString();
        }

        if (description.has("text"))
        {
            return description.path("text").asString();
        }

        return description.toString();
    }

    private int readVarInt(DataInputStream in) throws IOException
    {
        int value = 0;
        int position = 0;
        byte currentByte;

        do {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << position;

            position += 7;

            if (position >= 32) {
                throw new IOException("VarInt too big");
            }
        } while ((currentByte & 0x80) == 0x80);

        return value;
    }

    private void writeVarInt(OutputStream out, int value) throws IOException
    {
        while ((value & 0xFFFFFF80) != 0L) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }

        out.write(value & 0x7F);
    }

    private void writeString(OutputStream out, String value) throws IOException
    {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }
}
