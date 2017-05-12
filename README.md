# KuKuHarmony

KuKuHarmony is a SmartApp give users the ability to control each device by Logitech Harmony.
It must work based on '[Harmony API](https://github.com/maddox/harmony-api)'

With KuKuHarmony based on 'Harmony API', you can turn on and turn off devices individually.
The device's other commands are also support. 
- ex) volume up/down, channel up/down, FanSpeed, Menu and so on


## Features

* Provide a device's Simulated Switch
* List Hubs
* List Devices
* List Commands
* Turn on/off each device(not a activity)
* Control other commands also

## Setup
    [English]
    - Server Work
    1) Install 'Harmony API' on server following Harmony API's install instruction
    OR
    1) Pre-build Docker image fpr Raspberry-pie platform
        - Download Image
        docker search turlvo/harmony-api
        docker pull turlvo/harmony-api
        
        - Make a container and run
        docker run -ti --net=host --name harmony-api turlvo/harmony-api:latest /home/harmony-api/harmony-api/script/server        
        (If harmony-api's container is exit by before step, just start a container using 'docker start' command)
   
    
    - Web IDE Work
    2) Add 'KuKu Harmony' DTH (Default/TV/Roboking/Aircon/Fan)
    and add 'KuKu Harmony (Connect)' firstly, after that, add 'KuKu Harmony (Child)' SmartApp
    3) Publish 'KuKu Harmony' DTH and 'KuKu Harmony (Connect)' SmartApp for me(do not need publish child SmartApp
    
    
    - SmartThings Application Work(Installation)
    4) 'Automation' -> 'SmartApps' -> 'Add a SmartApp' -> 'My SmartApp' -> Select 'KuKu Harmony (Connect)'
    5) Input server's private IP address 'Harmony API' is installed (ex. 192.168.1.210:8282)
    6) Select a one Hub to control
    7) Done
    
    - SmartThings Application Work(Configuration)
    8) 'Automation' -> 'SmartApps' -> 'KuKu Harmony (Connect)'
    9) 'Add a device...' and select a device to add a Virtual device.
    10) Select a turn-on command turn-off command and so on.
    
    [Korean]
    Following below blog's instruction.
    http://kuku.pe.kr/?p=6313
    
   
