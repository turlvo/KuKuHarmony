/**
 *  KuKu Harmony - Virtual Switch for Logitech Harmony
 *
 *  Copyright 2017 KuKu <turlvo@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

metadata {
	definition (name: "KuKu Harmony_Default", namespace: "turlvo", author: "KuKu") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
        command "virtualOn"
        command "virtualOff"
        
        command "reboot"
        
        //attribute   "needUpdate", "string"
	}

    preferences {
        input name: "momentaryOn", type: "bool",title: "Enable Momentary on (for garage door controller)", required: false
        input name: "momentaryOnDelay", type: "num",title: "Enable Momentary on dealy time(default 5 seconds)", required: false
    }

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.off", nextState:"turningOn"
                attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#00A0DC", icon: "st.switches.switch.on", nextState:"turningOff"
				attributeState "turningOn", label:'${name}', action:"switch.off", backgroundColor:"#00A0DC", icon: "st.switches.switch.off", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.on", nextState:"turningOn"
			}
        }
                standardTile("zoomOut", "device.zoomSupported", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      		state "yes", label: "zoom out", action: "zoomOut", icon: "st.custom.buttons.subtract-icon"
            state "no", label: "zoom unavail", action: "", icon: "st.custom.buttons.subtract-icon"
    	}
        
    }

	main(["switch"])
	details(["switch"])
}

def installed() {
	log.debug "installed()"
	configure()
}

def configure() {
        
}

def updated()
{
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def momentaryOnHandler() {
	log.debug "momentaryOnHandler()"
	sendEvent(name: "switch", value: "off")
}


def on() {
    log.debug "child on()"

    log.debug "on>> ${device.currentState("switch")?.value}"
    def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
        log.debug "Already turned on, skip ON command"
    } else {
        parent.command(this, "power-on")
        sendEvent(name: "switch", value: "on")

        if (momentaryOn) {
            if (settings.momentaryOnDelay == null || settings.momentaryOnDelay == "" ) settings.momentaryOnDelay = 5
            log.debug "momentaryOnHandler() >> time : " + settings.momentaryOnDelay
            runIn(Integer.parseInt(settings.momentaryOnDelay), momentaryOnHandler, [overwrite: true])
        }
    }
}

def off() {
    log.debug "child off"    

    log.debug "off>> ${device.currentState("switch")?.value}"
    def currentState = device.currentState("switch")?.value

    if (currentState == "on") {
        parent.command(this, "power-off")
        sendEvent(name: "switch", value: "off")

    } else {
        log.debug "Already turned off, skip OFF command"
    }
}

def virtualOn() {
	log.debug "child on()"	
    sendEvent(name: "switch", value: "on")
}

def virtualOff() {
	log.debug "child off"	
    sendEvent(name: "switch", value: "off")
}

def generateEvent(Map results) {
    results.each { name, value ->
		log.debug "generateEvent>> name: $name, value: $value"
        def currentState = device.currentValue("switch")
		log.debug "generateEvent>> currentState: $currentState"
        if (currentState != value) {
        	log.debug "generateEvent>> changed to $value"
        	sendEvent(name: "switch", value: value)
        } else {
        	log.debug "generateEvent>> not change"
        }
    }
    return null
}

def poll() {
	log.debug "poll()"
}

def parseEventData(Map results) {
    results.each { name, value ->
        //Parse events and optionally create SmartThings events
    }
}