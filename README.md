# KuKuHarmony

KuKuHarmony is a SmartApp give users the ability to control each device by Logitech Harmony.
It works based on '[Harmony API](https://github.com/maddox/harmony-api)'

With KuKuHarmony based on 'Harmony API', you can turn on and turn off devices individually.
The device's other commands are also support. 
- ex) volume up/down, channel up/down, FanSpeed, Menu and so on


## Features

* Provide a device's Virtual device
* List Hubs
* List Devices
* List Commands
* Turn on/off each device(not a activity)
* Control other commands also
* Multiple Hubs
* State synchronization by Smart Plugs power or Contact Sensors state

## Setup
[English]
- Server Work
1) Download docker image and run contrainer

    [X86 Platform]
    
    Install 'Harmony API' on server following Harmony API's install instruction
    https://github.com/maddox/harmony-api

    OR

    [ARM Platform]
    
    Pre-build Docker image for Raspberry-pie platform
    - Download Image
    docker search turlvo/harmony-api
    docker pull turlvo/harmony-api

    - Make a container and run
    docker run -ti --net=host --name harmony-api turlvo/harmony-api:latest /home/harmony-api/harmony-api/script/server        
    (If harmony-api's container is exit by before step, just start a container using 'docker start' command)

    - Make a container restarting when rebooting
    ```
        # sudo vim /etc/systemd/system/harmony-api.service

        <harmony-api.service File content>
        -----------------------------------------------------------------------------            
        [Unit]
        Description=Harmony API container
        Requires=docker.service
        After=docker.service

        [Service]
        Restart=always
        ExecStart=/usr/bin/docker start -a harmony-api
        ExecStop=/usr/bin/docker stop -t 2 harmony-api

        [Install]
        WantedBy=multi-user.target
        -----------------------------------------------------------------------------

        # sudo systemctl enable /etc/systemd/system/harmony-api.service
    ```


- Web IDE Work
2) Add 'KuKu Harmony' DTH (Default/TV/Roboking/Aircon/Fan) and add 'KuKu Harmony' SmartApp
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/harmony11-1024x464.jpg">
3) Publish 'KuKu Harmony' DTH and 'KuKu Harmony' SmartApp for me
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/harmony22-1024x464.jpg">

- SmartThings Application Work(Installation)    
4) 'Automation' -> 'SmartApps' -> 'Add a SmartApp' -> 'My SmartApp' -> Select 'KuKu Harmony'
    (If you are using an older version, you need to remove 'KuKu Harmony (Connect)' and 'KuKu Harmony (Child)'.)
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/smartapp_install1.jpg">
5) Input server's private IP address 'Harmony API' is installed (ex. 192.168.1.210:8282) and Save
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/smartapp_install2.jpg">

- SmartThings Application Work(Configuration)
6) 'Automation' -> 'SmartApps' -> 'KuKu Harmony'
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/smartapp_setup1.jpg">
7) 'Add a device...' and select a hub and device
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/smartapp_setup2.jpg">
8) Select a Device type
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/smartapp_setup3.jpg">
9) Select a Commands and 'Save' and check added device
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/smartapp_setup4.jpg">
10) Check added Thing's tile and detail
<img src="http://kuku.pe.kr/wordpress/wp-content/uploads/2017/04/smartapp_setup6-1024x1024.jpg">



[Korean]
Following below blog's instruction.

-First Install: http://kuku.pe.kr/?p=6313

-Update v0.1.4: http://kuku.pe.kr/?p=6388

-Update v0.1.5: http://kuku.pe.kr/?p=6616

-'Harmony API' Server Auto Start(Rasp): http://kuku.pe.kr/?p=6440

-'Harmony API' Server Auto Start(Synology): http://kuku.pe.kr/?p=6584
    
## 🙋‍♂️ Support

💙 If you like this project, give it a ⭐ and share it with friends!

<a href="https://www.buymeacoffee.com/turlvo" target="_blank" title="buymeacoffee">
  <img src="https://iili.io/JoQ1HUQ.md.png"  alt="buymeacoffee-violet-badge" style="width: 130px;">
</a>
