Run this to install:

```
git clone https://github.com/AnthimosKouroutsidis/DemoStorage.git
cd DemoStorage
docker-compose -f ./docker-compose.yaml up -d
```

Open localhost:18888/storage/ANY_KEY to see the application running
(PUT any JSON to the same address, and it will be stored)

The application has a fictional "capacity" limit, with each stored JSON counting towards it. (where the "capacity" of a JSON translates to the amount of key-value pairs it has)

Open localhost:18888/storage/available_capacity to see the "available capacity"

Storing a JSON will fail if its "capacity" (number of keys) exceeds the "available capacity"

localhost:18888/storage/list shows the keys that have been stored


Example XHR to store some data:
```
var xhr = new XMLHttpRequest();
xhr.open("PUT", self.location.href, true);
xhr.setRequestHeader('Content-type','application/json; charset=utf-8');
xhr.onload = function () {
    console.log(xhr.responseText);
}
xhr.send(JSON.stringify({hi: 'hello', how: 'are you'})); // capacity of 2
```