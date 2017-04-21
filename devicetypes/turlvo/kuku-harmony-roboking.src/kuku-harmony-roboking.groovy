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
	definition (name: "KuKu Harmony_Roboking", namespace: "turlvo", author: "KuKu") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
        
        command "start"
        command "stop"
        command "home"
        command "mode"
        command "up"
        command "down"
        command "left"
        command "right"
        command "turbo"
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.off", nextState:"turningOn"
				attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.switches.switch.on", nextState:"turningOff"				
				attributeState "turningOn", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.switches.switch.off", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.on", nextState:"turningOn"
			}
        }

        valueTile("start", "device.start", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "START", action: "start"
            state "no", label: "unavail", action: ""
        }
        valueTile("up", "device.up", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "∧", action: "up"
            state "no", label: "unavail", action: ""
        }
        valueTile("home", "device.home", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "HOME", action: "home"
            state "no", label: "unavail", action: ""
        }
        
        valueTile("left", "device.left", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "<", action: "left"
            state "no", label: "unavail", action: ""
        }
        valueTile("stop", "device.stop", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "STOP", action: "stop"
            state "no", label: "unavail", action: ""
        }
        valueTile("right", "device.right", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: ">", action: "right"
            state "no", label: "unavail", action: ""
        }
        
        valueTile("mode", "device.mode", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "MODE", action: "mode"
            state "no", label: "unavail", action: ""
        }
        valueTile("down", "device.down", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "∨", action: "down"
            state "no", label: "unavail", action: ""
        }
        valueTile("turbo", "device.turbo", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "TURBO", action: "turbo"
            state "no", label: "unavail", action: ""
        }

    }

	main(["switch"])
	details(["start", "up", "home",
            "left", "stop", "right",
            "mode", "down", "turbo"])
}

def installed() {
	log.debug "installed()"
	//configure()
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def start() {
    log.debug "child start()"
    parent.command(this, "start")
}

def stop() {
    log.debug "child stop()"
    parent.command(this, "stop")
}

def home() {
    log.debug "child home()"
    parent.command(this, "home")
}

def mode() {
    log.debug "child mode()"
    parent.command(this, "mode")
}

def turbo() {
    log.debug "child turbo()"
    parent.command(this, "turbo")
}

def up() {
    log.debug "child up()"
    parent.command(this, "up")
}

def down() {
    log.debug "child down()"
    parent.command(this, "down")
}

def left() {
    log.debug "child left()"
    parent.command(this, "left")
}

def right() {
    log.debug "child right()"
    parent.command(this, "right")
    
}

def on() {
	log.debug "child on()"
	parent.command(this, "start")
    sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "child off"
	parent.command(this, "home")
    sendEvent(name: "switch", value: "off")
}

def poll() {
	log.debug "poll()"
}

def parseEventData(Map results) {
    results.each { name, value ->
        //Parse events and optionally create SmartThings events
    }
}
