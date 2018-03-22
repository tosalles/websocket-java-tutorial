package nl.sogyo.websocket;

import lombok.Data;
import lombok.val;

import static nl.sogyo.websocket.DeviceStatus.*;

@Data
class Device {
    private int id;
    private final String name;
    private String status;
    private final String type;
    private final String description;

    Device(final String name, final String status, final String type, final String description) {
        this.name = name;
        this.status = status;
        this.type = type;
        this.description = description;
    }

    public void toggleStatus() {
        val newStatus = ON.equals(this.getStatus()) ? OFF : ON;
        this.setStatus(newStatus);
    }
}
