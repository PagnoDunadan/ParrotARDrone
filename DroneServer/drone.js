var express = require('express');
var app = express();
var bodyParser = require("body-parser");
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

var arDrone = require('ar-drone');
var client  = arDrone.createClient();

/*
client.takeoff();

client
  .after(5000, function() {
    this.clockwise(0.5);
  })
  .after(3000, function() {
    this.stop();
    this.land();
  });
*/

app.get('/start', function (req, res) {
    client.takeoff();
    res.end();
    console.log("Start");
});

app.get('/stop', function (req, res) {
    client.stop();
    client.land();
    res.end();
    console.log("Stop");
});

app.get('/move', function (req, res) {
	front = req.query.front;
	left = req.query.left;
	
    client.front(front);
    client.counterClockwise(left);
    res.end();
    console.log("Move");
});



app.get('/up', function (req, res) {
	
    client.up(0.05);
    res.end();
    console.log("Up/down");
});

app.get('/down', function (req, res) {
	
    client.down(0.05);
    res.end();
    console.log("Up/down");
});

// Start server
app.listen(3000, function () {
  console.log("Node.js server for Parrot AR.Drone 2.0 started!")
});