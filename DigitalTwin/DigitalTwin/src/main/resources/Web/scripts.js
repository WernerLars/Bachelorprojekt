var twinUrl = 'http://localhost:8080/rest/twin/';
var historyUrl = 'http://localhost:8080/rest/history/';
var settingsUrl = 'http://localhost:8080/rest/settings';

function openModalNext() {
    var modal = document.getElementById("modalNext");
    modal.style.display = "block";
}

function closeModalNext() {
    var modal = document.getElementById("modalNext");
    modal.style.display = "none";
}

function openModalNextSelect() {
    var modal = document.getElementById("modalNextSelect");
    modal.style.display = "block";
}

function closeModalNextSelect() {
    var modal = document.getElementById("modalNextSelect");
    modal.style.display = "none";
}

function openModalLast() {
    var modal = document.getElementById("modalLastSelect");
    modal.style.display = "block";
}

function closeModalLast() {
    var modal = document.getElementById("modalLastSelect");
    modal.style.display = "none";
}

function openModalSettings() {
    var modal = document.getElementById("modalSettings");
    modal.style.display = "block";
}

function closeModalSettings() {
    var modal = document.getElementById("modalSettings");
    modal.style.display = "none";
}

function showValues(weather) {
    var childGrid = document.createElement("p");
    childGrid.innerHTML = weather.gridno;
    var element = document.getElementById("gridno");
    element.appendChild(childGrid);

    var childLat = document.createElement("p");
    childLat.innerHTML = weather.latitude;
    var element = document.getElementById("latitude");
    element.appendChild(childLat);

    var childLon = document.createElement("p");
    childLon.innerHTML = weather.longitude;
    var element = document.getElementById("longitude");
    element.appendChild(childLon);

    var childAlt = document.createElement("p");
    childAlt.innerHTML = weather.altitude;
    var element = document.getElementById("altitude");
    element.appendChild(childAlt);

    var childDay = document.createElement("p");
    childDay.innerHTML = weather.day;
    var element = document.getElementById("day");
    element.appendChild(childDay);

    var childMax = document.createElement("p");
    childMax.innerHTML = weather.tempmax;
    var element = document.getElementById("tempmax");
    element.appendChild(childMax);

    var childMin = document.createElement("p");
    childMin.innerHTML = weather.tempmin;
    var element = document.getElementById("tempmin");
    element.appendChild(childMin);

    var childAvg = document.createElement("p");
    childAvg.innerHTML = weather.tempavg;
    var element = document.getElementById("tempavg");
    element.appendChild(childAvg);

    var childWin = document.createElement("p");
    childWin.innerHTML = weather.windspeed;
    var element = document.getElementById("windspeed");
    element.appendChild(childWin);

    var childVap = document.createElement("p");
    childVap.innerHTML = weather.vapourpressure;
    var element = document.getElementById("vapourpressure");
    element.appendChild(childVap);

    var childRad = document.createElement("p");
    childRad.innerHTML = weather.radiation;
    var element = document.getElementById("radiation");
    element.appendChild(childRad);

    var childSno = document.createElement("p");
    childSno.innerHTML = weather.snowdepth;
    var element = document.getElementById("snowdepth");
    element.appendChild(childSno);
    var date = new Date();
    document.getElementById("lastUpdate").innerHTML = "<i>Updated on : " + date.toLocaleString() + "</i>";
}

function removeChildren() {
    var node;
    node = document.getElementById("gridno");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("latitude");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("longitude");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("altitude");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("day");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("tempmax");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("tempmin");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("tempavg");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("windspeed");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("vapourpressure");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("radiation");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }

    node = document.getElementById("snowdepth");
    while(node.lastChild) {
        node.removeChild(node.lastChild);
    }
}

function getNextEntry() {
    closeModalNext();
    var area = document.getElementById("areaCodeNext").value;
    var url = twinUrl + area;
    fetch(url)
        .then(response => {
            return response.json();
        })
        .then(weather => {
            removeChildren();
            for(var i = 0; i < weather.length; i++) {
                showValues(weather[i]);
            }
        })
        .catch(err => {
            console.log(err.message);
        })
    return false;
}

function getSelectionNext() {
    closeModalNextSelect();
    var i;
    var count = 0;
    var append = "/";
    var area = document.getElementById("areaCodeSelect").value;
    var selection = document.getElementsByName("userSelectionNext");
    for(i = 0; i < selection.length; i++) {
        if(selection[i].checked) {
            append = append + selection[i].value + ',';
            count ++;
        }
    }
    if(count === 0) {
        return false;
    }
    var url = twinUrl + area + append;
    fetch(url)
        .then(response => {
            return response.json();
        })
        .then(weather => {
            removeChildren();
            showValues(weather);
        })
        .catch(err => {
            console.log(err.message);
        })
    return false;
}

function getLastTen() {
    var filter = '?limit=10';
    fetch(historyUrl + filter)
        .then(response => {
            return response.json();
        })
        .then(weather => {
            removeChildren();
            for(var i = 0; i < weather.length; i++) {
                showValues(weather[i]);
            }
        })
        .catch(err => {
            console.log(err.message);
        })
    return false;
}

function refresh() {
    var filter = '?limit=1';
    fetch(historyUrl + filter)
        .then(response => {
            return response.json();
        })
        .then(weather => {
            removeChildren();
            for(var i = 0; i < weather.length; i++) {
                showValues(weather[i]);
            }
        })
        .catch(err => {
            console.log(err.message);
        })
    return false;
}

function getSelectionLast() {
    closeModalLast();
    var i;
    var appends = "";
    var form = document.forms["selectedValuesLast"];
    var column = form.elements["userSelectionLast"];
    for(i = 0; i < column.length; i++) {
        if(column[i].checked) {
            appends = appends + column[i].value + ',';
        }
    }
    var append = appends.slice(0, -1);

    var filter = "";
    var filterText = document.getElementById("filterText").value;
    if(filterText.length > 0) {
        filter = filter + "?" + filterText;
        var symbol = form.elements["filterRadio"];
        for(i = 0; i < symbol.length; i++) {
            if(symbol[i].checked) {
                filter = filter + "=" + symbol[i].value;
            }
        }
        var filterVal = document.getElementById("filterTextVal").value;
        filter = filter + filterVal;
    }

    var filterLimit = document.getElementById("filterLimit").value;
    if(filterLimit.length > 0) {
        if(filter.length === 0) {
            filter = filter + "?" + "limit=" + filterLimit;
        }
        else {
            filter = filter + "&" + "limit=" + filterLimit;
        }
    }

    var filterOffset = document.getElementById("filterOffset").value;
    if(filterOffset.length > 0) {
        if(filter.length === 0) {
            filter = filter + "?" + "offset=" + filterOffset;
        }
        else {
            filter = filter + "&" + "offset=" + filterOffset;
        }
    }

    if(append.length === 0 && filter.length === 0) {
        return false;
    }

    fetch(historyUrl + append + filter)
        .then(response => {
            return response.json();
        })
        .then(weather => {
            removeChildren();
            for(var i = 0; i < weather.length; i++) {
                showValues(weather[i]);
            }
        })
        .catch(err => {
            console.log(err.message);
        })
    return false;
}

async function getSettings() {
    fetch(settingsUrl)
        .then(response => {
            return response.json();
        })
        .then(settings => {
            document.getElementById("hour").value = settings.updateDataPerHour;
            document.getElementById("records").value = settings.recordsToSend;
        })
        .catch(err => {
            console.log(err.message);
        })
    return false;
}

async function setSettings() {
    closeModalSettings();
    var updateDataPerHour = parseInt(document.getElementById("updatesHour").value, 10);
    var recordsToSend = parseInt(document.getElementById("recordsAtOnce").value, 10);
    var settings = {"updateDataPerHour": updateDataPerHour, "recordsToSend": recordsToSend};
    fetch(settingsUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        mode: 'no-cors',
        body: JSON.stringify(settings)
    })
        .then(response => {
            console.log(response.json());
            return response.json();
        })
        .catch(err => {
            console.log(err.message);
        })
    await getSettings();
    return false;
}
(async function() {
    await getSettings();

    //getNextEntry();
    refresh();
    var refreshTimeout = calculateInMilliseconds(parseInt(document.getElementById("hour").value,10));
    setTimeout(arguments.callee, refreshTimeout);
    var date = new Date(Date.now() + refreshTimeout);
    document.getElementById("nextUpdate").innerHTML = "<i>Next update on : " + date.toLocaleString() + "</i>";
})();

// we have to calculate how many times the function should be called in milliseconds
function calculateInMilliseconds(refreshPerHour) {
    //we have to find out in how many minutes the function should be called
    var everyMinute = 60 / refreshPerHour;
    return (everyMinute * 60 * 1000); // in milliseconds
}
