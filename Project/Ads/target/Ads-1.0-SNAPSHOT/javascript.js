var wsUri = "ws://" + document.location.host + "/Ads/" + "adsendpoint";
var websocket = new WebSocket(wsUri);

websocket.onerror = function(evt) { onError(evt) };

websocket.onmessage = function(evt) { onMessage(evt) };

function onError(evt) {
}

function sendText(json) {
    console.log("sending text: " + json);
    websocket.send(json);
}

function onMessage(message) {
    var messageReceived = message.data;
    console.log("received text: " + messageReceived);
    var messageSplit = messageReceived.split(" ");
    switch(messageSplit[0]){
        case "login":
            if(messageSplit[1] == "true"){
                document.cookie = "email=" + messageSplit[2];
                alert("You are now logged");
            }
            else{
                alert("Impossible to login");
            }
            break;
        case "create":
            if(messageSplit[1] == "true"){
                alert("The account has been created");
            }
            else{
                alert("Impossible to create the account");
            }
            break;
        case "ad":
            if(messageSplit[1] == "true"){
                alert("The advertisement has been added");
            }
            else{
                alert("Impossible to add the advertisement");
            }
            break;
        case "list":
            var dataComplete = "";
            for(i = 1; i < messageSplit.length; i++){
                dataComplete += messageSplit[i];
            }
            var list = dataComplete;
            var adData = list.split("[");
            var nbAds = adData.length;
            var toRemove = document.getElementById("table");
            if(toRemove != null){
                toRemove.outerHTML = "";
                delete toRemove;    
            }
            var body = document.getElementsByTagName('body')[0];
            var table = document.createElement('table');
            table.setAttribute("id", "table");
            var thead = document.createElement('thead');
            var trh = document.createElement('tr');
            var th = document.createElement('th');
            th.appendChild(document.createTextNode("Title"));
            trh.appendChild(th);
            var th = document.createElement('th');
            th.appendChild(document.createTextNode("Price"));
            trh.appendChild(th);
            var th = document.createElement('th');
            th.appendChild(document.createTextNode("Category"));
            trh.appendChild(th);
            var th = document.createElement('th');
            th.appendChild(document.createTextNode("Location"));
            trh.appendChild(th);
            var th = document.createElement('th');
            th.appendChild(document.createTextNode("Advertisement number"));
            trh.appendChild(th);
            thead.appendChild(trh);
            table.appendChild(thead);
            table.setAttribute("border", "1");
            var tbody = document.createElement('tbody');
            for(var i = 1; i < nbAds; i++){
                var tr = document.createElement('tr');
                var row = adData[i].substring(0, adData[i].length - 1);
                var rowSplit = row.split("{");
                console.log(row);
                var id;
                for(var j = 1; j < rowSplit.length; j++){
                    if(j != 2 && j != 5){
                        var valueData = rowSplit[j].substring(0, rowSplit[j].length - 1);
                        console.log(valueData);
                        if(j == rowSplit.length - 1){
                            id = valueData;
                        }
                        var td = document.createElement('td');
                        td.appendChild(document.createTextNode(valueData));
                        tr.appendChild(td);
                    }  
                }
                tr.setAttribute("style", "cursor: pointer");
                tr.setAttribute("onclick", "document.location = './ad.html?id=" + id + "';");
                tbody.appendChild(tr);
            }
            table.appendChild(tbody);
            body.appendChild(table)
            break;
        case "get":
            var dataComplete = "";
            for(i = 1; i < messageSplit.length; i++){
                dataComplete += messageSplit[i];
            }
            var list = dataComplete;
            var rowSplit = list.split("{");
            var toRemove = document.getElementById("table");
            if(toRemove != null){
                toRemove.outerHTML = "";
                delete toRemove;    
            }
            var body = document.getElementsByTagName('body')[0];
            var table = document.createElement('table');
            table.setAttribute("id", "table");
            table.setAttribute("border", "1");
            var tbody = document.createElement('tbody');
            for(var j = 1; j < rowSplit.length; j++){
                var tr = document.createElement('tr');
                var valueData = rowSplit[j].substring(0, rowSplit[j].length - 1);
                console.log(valueData);
                if(j == rowSplit.length - 1){
                    id = valueData;
                }
                var td = document.createElement('td');
                if(j == 1){
                    td.appendChild(document.createTextNode("Title"));    
                }
                else if (j == 2){
                    td.appendChild(document.createTextNode("Description"));    
                }
                else if (j == 3){
                    td.appendChild(document.createTextNode("Price"));    
                }
                else if (j == 4){
                    td.appendChild(document.createTextNode("Category"));    
                }
                else if (j == 5){
                    td.appendChild(document.createTextNode("Seller"));    
                }
                else if (j == 6){
                    td.appendChild(document.createTextNode("Location"));    
                }
                else if (j == 7){
                    td.appendChild(document.createTextNode("Advertisement number"));    
                }
                tr.appendChild(td);
                var td = document.createElement('td');
                td.appendChild(document.createTextNode(valueData));
                tr.appendChild(td);
                tbody.appendChild(tr);
            }
            table.appendChild(tbody);
            body.appendChild(table)
            break;
    }
}

function addAd(event){
    if(getUser() == ""){
        alert("You must be logged to add an advertisement");
    }
    else{
        title = document.getElementById("title").value;
        description = document.getElementById("description").value;
        price = document.getElementById("price").value;
        picture = document.getElementById("picture").value;
        category = document.getElementById("category").value;
        adLocation = document.getElementById("adLocation").value;
        user = getUser();
        var json = JSON.stringify({
            "type": "ad",
            "title": title,
            "description": description,
            "price": price,
            "category": category,
            "user": user,
            "adLocation": adLocation
        });
        sendText(json);
    }
    return false;
}

function getUser(){
    return getCookie("email");
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function login(email, password){
    var json = JSON.stringify({
        "type": "login",
        "email": email,
        "password": password
    });
    sendText(json);
    return false;
}
                
function create(email, password){
    var json = JSON.stringify({
        "type": "create",
        "email": email,
        "password": password
    });
    sendText(json);
    return false;
}

function logout(event){
    document.cookie = "email=; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
    alert("You are now unlogged");
    return false;
}

function getList(){
    var json = JSON.stringify({
        "type": "list"
    });
    sendText(json);
    return false;
}

function getAd(){
    var json = JSON.stringify({
        "type": "get",
        "id": window.location.search.substr(4)
    });
    sendText(json);
    return false;
}

function getAdNumber(){
    var hNumber = document.getElementById("adNumber");
    var id = window.location.search.substr(4);
    hNumber.innerHTML = "Advertisement " + id;
}