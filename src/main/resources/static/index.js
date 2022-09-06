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

const client = new RSocketClient({
    serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
    },
    setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: 'application/json',
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

client.connect().subscribe({
    onError: error => console.error(error),
    onSubscribe: cancel => {
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
                    "operation").innerText = "Operation ID: "
                    + value.data.id + ". Customer: " + value.data.customer.name;
                //Управление обратным давлением вручную. Можно поставить на паузу (функция pause), можно возобновить чтение(функция suspend)
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