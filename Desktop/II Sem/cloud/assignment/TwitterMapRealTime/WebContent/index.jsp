<!DOCTYPE html>

<%@page import="twitter4j.TwitterException"%>
<%@page import="tweetget.TweetGet"%>
<html>
<head>
<meta charset="utf-8">
<title>Heatmaps</title>
<style>
html, body, #map-canvas {
height: 100%;
margin: 0px;
padding: 0px
}
#panel {
position: absolute;
top: 5px;
left: 50%;
    margin-left: -180px;
    z-index: 5;
    background-color: #fff;
padding: 5px;
border: 1px solid #999;
}
</style>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=visualization"></script>

<script src="https://sdk.amazonaws.com/js/aws-sdk-2.0.22.min.js"></script>
<script>

var map, pointarray, heatmap;

var coords = [];

function startTimer() {
    var fiveMinutes = 5,
        display = document.getElementById("time"),
        mins, seconds;
    setInterval(function() {
        
        fiveMinutes--;

        if (fiveMinutes < 0) {
            fiveMinutes =  5;
        }
    }, 1000);
    return;
}

function OnChange(dropdown)
{
	
	console.log("timer refresh")
    var myindex  = dropdown.selectedIndex
    var SelValue = dropdown.options[myindex].value
    console.log(SelValue);
    clearMarkers();
    initialize(SelValue);
    <%@page language="java" import="java.util.*"%>
    <%@page import="twitter4j.*" %>
  		
    	<%System.out.println("Start tweetget!");%>
    	<%TweetGet tw = new TweetGet();%>
    	<%tw.startTweet();%>
   
    
    return true;
}


function initialize(displayKeyword)
{
    var mapProp = {
    center:new google.maps.LatLng(0, 0),
    zoom:3,
    mapTypeId:google.maps.MapTypeId.HYBRID
    };
    
    map = new google.maps.Map(document.getElementById('map-canvas'),
                              mapProp);
    
    
    AWS.config.update({accessKeyId: '**********', secretAccessKey: '****************'});
    AWS.config.region = 'us-east-1';
    var db = new AWS.DynamoDB();
    db.listTables( function (err, data) {
                  
                  if (err){
                  alert(err);
                  }
                  else {
                  console.log(data.TableNames);
                  
                  }
        });
    
    var params = {
    TableName: 'TweetDatabase_8',
    AttributesToGet: ['TwitterId', 'KeyWord', 'Latitude', 'Longitude', 'Time'],
    Limit: 354,
        //Exclusive Start Key - Wasn't able to set this from the cookie.
    }
    
    labels=[];
    iterator = 0;
    iterator_coords = 0;
    db.scan(params, function(err, data) {
            
            if (err)
            console.log(err);
            else
            console.log("last count: " + data.Count);
            for (var dat_item in data.Items) {
            var curTweet = data.Items[dat_item];
            var temp_latLng= new google.maps.LatLng(parseFloat(curTweet.Latitude.S), parseFloat(curTweet.Longitude.S));
            var temp_label = curTweet.KeyWord.S
            //var temp_sentiment = curTweet.sentiment.S
            if(temp_label == displayKeyword) {
            
            coords.push(temp_latLng);
            labels.push(temp_label);
            //sentiments.push(temp_sentiment);
            console.log("Coordinate" + coords[iterator_coords]);
            
            }
            iterator_coords++;
            }
            console.log("Coords length: " + coords.length);
            
            var i = 0;
            
            //clearMarkers();
            
            pointarray=new google.maps.MVCArray(coords);
            heatmap = new google.maps.visualization.HeatmapLayer({
                                                                 data: pointarray
                                                                 });
            heatmap.setMap(map);
            });
}

function clearMarkers() {
    coords=[];
}

function toggleHeatmap() {
    heatmap.setMap(heatmap.getMap() ? null : map);
}

function changeGradient() {
    var gradient = [
                    'rgba(0, 255, 255, 0)',
                    'rgba(0, 255, 255, 1)',
                    'rgba(0, 191, 255, 1)',
                    'rgba(0, 127, 255, 1)',
                    'rgba(0, 63, 255, 1)',
                    'rgba(0, 0, 255, 1)',
                    'rgba(0, 0, 223, 1)',
                    'rgba(0, 0, 191, 1)',
                    'rgba(0, 0, 159, 1)',
                    'rgba(0, 0, 127, 1)',
                    'rgba(63, 0, 91, 1)',
                    'rgba(127, 0, 63, 1)',
                    'rgba(191, 0, 31, 1)',
                    'rgba(255, 0, 0, 1)'
                    ]
    heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
}



function changeRadius() {
    heatmap.set('radius', heatmap.get('radius') ? null : 20);
}

function changeOpacity() {
    heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
}

google.maps.event.addDomListener(window, 'load', initialize);

</script>
</head>

<body>
<div id="panel">
<button onclick="toggleHeatmap()">Toggle Heatmap</button>
<button onclick="changeGradient()">Change gradient</button>
<button onclick="changeRadius()">Change radius</button>
<button onclick="changeOpacity()">Change opacity</button>

<form>
<select name=option onchange='OnChange(this.form.option);'>
<option>Choose keyword...</option>
<option>apple</option>
<option>football</option>
<option>country</option>
<option>movie</option>
<option>cricket</option>
<option>food</option>
<option>place</option>
<option>healthcare</option>
</select>
</form>
</div>

<div id="map-canvas"></div>
</body>
</html>

