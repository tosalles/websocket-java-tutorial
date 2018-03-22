package nl.sogyo.websocket;

import lombok.NonNull;
import lombok.val;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static nl.sogyo.websocket.DeviceAction.*;
import static nl.sogyo.websocket.MessageProperty.*;

public class DeviceSessionHandler {
    private int deviceId = 0;
    private final Set<Session> sessions = new HashSet<>();
    private final Set<Device> devices = new HashSet<>();

    public void addSession(@NonNull final Session session) {
        sessions.add(session);
        devices.forEach(device -> {
            val addMessage = createAddMessage(device);
            sendToSession(session, addMessage);
        });
    }

    public void addDevice(@NonNull final Device device) {
        device.setId(deviceId);
        devices.add(device);
        deviceId++;
        val addMessage = createAddMessage(device);
        sendToAllConnectedSessions(addMessage);
    }

    public void removeSession(@NonNull Session session) {
        sessions.remove(session);
        val provider = JsonProvider.provider();
        val removeMessage = provider.createObjectBuilder()
                .add(ACTION, REMOVE)
                .add(ID, session.getId())
                .build();
        sendToAllConnectedSessions(removeMessage);
    }

    public void removeDevice(final int id) {
        getDeviceById(id)
                .ifPresent(device -> {
                    devices.remove(device);
                    val provider = JsonProvider.provider();
                    val removeMessage = provider.createObjectBuilder()
                            .add(ACTION, REMOVE)
                            .add(ID, id)
                            .build();
                    sendToAllConnectedSessions(removeMessage);
                });
    }

    public void toggleDevice(final int id) {
        getDeviceById(id)
                .ifPresent(device -> {
                    device.toggleStatus();

                    val provider = JsonProvider.provider();
                    val updateDevMessage = provider.createObjectBuilder()
                            .add(ACTION, TOGGLE)
                            .add(ID, device.getId())
                            .add(STATUS, device.getStatus())
                            .build();
                    sendToAllConnectedSessions(updateDevMessage);
                });
    }

    private Optional<Device> getDeviceById(final int id) {
        return devices.stream()
                .filter(d -> d.getId() == id)
                .findFirst();
    }

    private JsonObject createAddMessage(@NonNull final Device device) {
        val provider = JsonProvider.provider();
        return provider.createObjectBuilder()
                .add(ACTION, ADD)
                .add(ID, device.getId())
                .add(NAME, device.getName())
                .add(TYPE, device.getType())
                .add(STATUS, device.getStatus())
                .add(DESCRIPTION, device.getDescription())
                .build();
    }

    private void sendToAllConnectedSessions(@NonNull final JsonObject message) {
        sessions.forEach(session -> sendToSession(session, message));
    }

    private void sendToSession(@NonNull final Session session, @NonNull final JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (final IOException ex) {
            sessions.remove(session);
            Logger.getLogger(DeviceSessionHandler.class.getName()).log(Level.SEVERE, format("Could not send to session %s", session.getId()), ex);
        }
    }
}