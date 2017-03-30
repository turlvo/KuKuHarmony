# KuKuHarmony

KuKuHarmony is a SmartApp give users the ability to control each device by Logitech Harmony.
It must work based on '[Harmony API](https://github.com/maddox/harmony-api)'

With KuKuHarmony based on 'Harmony API', you can turn on and turn off devices individually.
Other device's commands are not supported not yet. 
- ex) volume up/down, channel up/down, FanSpeed, Menu and so on


## Features

* Provide a device's Simulated Switch
* List devices.
* Turn on/off each device

## Setup

    - Server Work
    1) Install 'Harmony API' on server following Harmony API's install instruction
    
    - Web IDE Work
    2) Add 'KuKu Harmony' DTH and 'KuKu Harmony (Connect)'/'KuKu Harmony (Child)' SmartApp
    3) Publish 'KuKu Harmony' DTH and 'KuKu Harmony (Connect)' SmartApp for me
    
    - SmartThings Application Work(Installation)
    4) 'Automation' -> 'SmartApps' -> 'Add a SmartApp' -> 'My SmartApp' -> Select 'KuKu Harmony (Connect)'
    5) Input server's private IP address 'Harmony API' is installed (ex. 192.168.1.210:8282)
    6) Select a one Hub to control
    7) Done
    
    - SmartThings Application Work(Configuration)
    8) 'Automation' -> 'SmartApps' -> 'KuKu Harmony (Connect)'
    9) 'Add a device...' and select a device to add as a Simulated Switch
    10) Select a turn-on command turn-off command
    
   
