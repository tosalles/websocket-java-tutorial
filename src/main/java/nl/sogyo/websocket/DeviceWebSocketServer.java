package nl.sogyo.websocket;

import lombok.NonNull;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonReader;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nl.sogyo.websocket.DeviceAction.*;
import static nl.sogyo.websocket.DeviceStatus.OFF;
import static nl.sogyo.websocket.MessageProperty.*;

@ApplicationScoped
@ServerEndpoint(value = "/actions")
public class DeviceWebSocketServer {
    private DeviceSessionHandler sessionHandler = new DeviceSessionHandler();

    @OnOpen
    public void open(@NonNull final Session session) {
        sessionHandler.addSession(session);
    }

    @OnClose
    public void close(@NonNull final Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(@NonNull final Throwable error) {
        Logger.getLogger(DeviceWebSocketServer.class.getName()).log(Level.SEVERE, null, error);
    }

    @OnMessage
    public void handleMessage(@NonNull final String message, @NonNull final Session session) {
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            val jsonMessage = reader.readObject();

            if (ADD.equals(jsonMessage.getString(ACTION))) {
                val device = new Device(
                        jsonMessage.getString(NAME),
                        OFF,
                        jsonMessage.getString(TYPE),
                        jsonMessage.getString(DESCRIPTION));
                sessionHandler.addDevice(device);
            }

            else if (REMOVE.equals(jsonMessage.getString(ACTION))) {
                val id = jsonMessage.getInt(ID);
                sessionHandler.removeDevice(id);
            }

            else if (TOGGLE.equals(jsonMessage.getString(ACTION))) {
                val id = jsonMessage.getInt(ID);
                sessionHandler.toggleDevice(id);
            }
        }
    }
}
