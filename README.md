# VehicleTracking
Thinking of how cool it would be if we are able to track a BMTC bus just the way we track a Ola or Uber cab.
I made it possible with this VehicleTracking App. 

There are two interfaces
1. Driver side app
2. Customer side app

Driver logins and for every 2sec his coordinates (lat, lon) gets updated in the database.

On the user side , user searches for 356cw then all the nearest ( <5km ) will be displayed on the map fragment
These maps fetches updated data in every 2sec from the database so that we can see the tracking of the bus in real time.
