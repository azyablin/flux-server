const {
    RSocketClient,
    JsonSerializer,
    IdentitySerializer
} = require('rsocket-core');
const RSocketWebSocketClient = require('rsocket-websocket-client').default;

const toDataURL = url => fetch(url)
.then(response => response.blob())
.then(blob => new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onloadend = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(blob)
}))

// Create an instance of a client
const client = new RSocketClient({
    // send/receive objects instead of strings/buffers
    serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
    },
    setup: {
        // ms btw sending keepalive to server
        keepAlive: 60000,
        // ms timeout if no keepalive response
        lifetime: 180000,
        // format of `data`
        dataMimeType: 'application/json',
        // format of `metadata`
        metadataMimeType: 'message/x.rsocket.routing.v0',
    },
    transport: new RSocketWebSocketClient({url: 'ws://localhost:8096/rsocket'}),
});

var subscription = null;
var paused = false;

const pause = () => {
    paused = true;
}

const suspend = () => {
    if (paused) {
        paused = false;
        if (subscription) {
            subscription.request(1);
        }
    }
}

// Open the connection
client.connect().subscribe({
    onError: error => console.error(error),
    onSubscribe: cancel => {/* call cancel() to abort */
    },
    onComplete: socket => {
        socket.requestStream({
            data: null,
            metadata: String.fromCharCode("operations".length) + "operations"
        })
        .subscribe({
            onComplete: () => console.log("requestStream done"),
            onError: error => {
                console.log("got error with requestStream");
                console.error(error);
            },
            onNext: value => {
                document.getElementById(
                    "rego").innerText = "Operation ID: "
                    + value.data.id;
                if (!paused) {
                    subscription.request(1);
                }
            },
            onSubscribe: sub => {
                subscription = sub;
                subscription.request(1);
            }
        });
    }
});

window.pause = pause;
window.suspend = suspend;