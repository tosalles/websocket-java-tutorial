
window.onload = hideForm;
const socket = new WebSocket("ws://localhost:8080/actions");
socket.onmessage = event => {
    const device = JSON.parse(event.data);
    if (device.action === action.add) {
        printDeviceElement(device);
    }
    else if (device.action === action.remove) {
        document.getElementById(device.id).remove();
    }
    else if (device.action === action.toggle) {
        const node = document.getElementById(device.id);
        const statusText = node.children[2];

        statusText.innerHTML = deviceStatusText(device);
    }
};

function addDevice(name, type, description) {
    const DeviceAction = {
        action: action.add,
        name: name,
        type: type,
        description: description
    };
    socket.send(JSON.stringify(DeviceAction));
}

function removeDevice(element) {
    const DeviceAction = {
        action: action.remove,
        id: element
    };
    socket.send(JSON.stringify(DeviceAction));
}

function toggleDevice(element) {
    const DeviceAction = {
        action: action.toggle,
        id: element
    };
    socket.send(JSON.stringify(DeviceAction));
}

function printDeviceElement(device) {
    let newDevice = document.createElement("div");
    newDevice.setAttribute("id", device.id);
    newDevice.setAttribute("class", `device ${device.type}`);
    newDevice.innerHTML = `
        <span class='deviceName'>${device.name}</span>
        <span><b>Type:</b> ${device.type}</span>
        <span>${deviceStatusText(device)}</span>
        <span><b>Comments:</b> ${device.description}</span>
        <span class='removeDevice'><a href='#' onclick=removeDevice(${device.id})>Remove device</a></span>`;

    document
        .getElementById("content")
        .appendChild(newDevice);
}

function deviceStatusText(device) {
    const toggleDeviceText = device.status === "On" ? "Turn off" : "Turn on";
    return `<b>Status:</b> ${device.status} (<a href="#" onclick=toggleDevice(${device.id})>${toggleDeviceText}</a>)`;
}

function showForm() {
    document.getElementById("addDeviceForm").style.display = "block";
}

function hideForm() {
    document.getElementById("addDeviceForm").style.display = "none";
}

function formSubmit() {
    const form = document.getElementById("addDeviceForm");
    const name = form.elements["device_name"].value;
    const type = form.elements["device_type"].value;
    const description = form.elements["device_description"].value;
    hideForm();
    document.getElementById("addDeviceForm").reset();
    addDevice(name, type, description);
}

const action = {
    add: "add",
    toggle: "toggle",
    remove: "remove"
};