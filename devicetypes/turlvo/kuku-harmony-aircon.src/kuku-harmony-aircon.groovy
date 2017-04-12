/**
 *  KuKu Harmony
 *
 *  Copyright 2017 KuKu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "KuKu Harmony_Aircon", namespace: "turlvo", author: "KuKu") {
        capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
        
        command "power"
        command "tempup"
        command "mode"
        command "jetcool"
        command "tempdown"
        command "speed"
        command "setRangedLevel", ["number"]
	}

	tiles (scale: 2){      
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.switches.switch.on", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.off", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", backgroundColor:"#79b821", icon: "st.switches.switch.off", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", icon: "st.switches.switch.on", nextState:"turningOn"
			}
        }

        valueTile("power", "device.power", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "POWER", action: "power"
            state "no", label: "unavail", action: ""
        }
        valueTile("tempup", "device.tempup", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "∧\nTEMP", action: "tempup"
            state "no", label: "unavail", action: ""
        }
        valueTile("mode", "device.mode", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "MODE", action: "mode"
            state "no", label: "unavail", action: ""
        }
        
        valueTile("jetcool", "device.jetcool", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "JET\nMODE", action: "jetcool"
            state "no", label: "unavail", action: ""
        }
        valueTile("tempdown", "device.tempdown", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "TEMP\n∨", action: "tempdown"
            state "no", label: "unavail", action: ""
        }
        valueTile("speed", "device.speed", width: 2, height: 2, decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
            state "yes", label: "FAN SPEED", action: "speed"
            state "no", label: "unavail", action: ""
        }
        controlTile("tempSliderControl", "device.level", "slider", range:"(18..30)", height: 2, width: 6) {
            state "level", action:"setRangedLevel"
        }

    }

	main(["switch"])
	details(["power", "tempup", "mode",
            "jetcool", "tempdown", "speed", "tempSliderControl"])
}

def installed() {
	log.debug "installed()"
	//configure()
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def power() {
    log.debug "child power()"
    parent.command(this, "power")
}

def tempup() {
    log.debug "child tempup()"
    parent.command(this, "tempup")
}

def mode() {
    log.debug "child mode()"
    parent.command(this, "mode")
}

def jetcool() {
    log.debug "child jetcool()"
    parent.command(this, "jetcool")
}

def tempdown() {
    log.debug "child tempdown()"
    parent.command(this, "tempdown")
}

def speed() {
    log.debug "child speed()"
    parent.command(this, "speed")
    
}

def setRangedLevel(value) {
	log.debug "setting ranged level to $value"
	parent.commandValue(this, value)
}

def on() {
	log.debug "child on()"
	parent.commandValue(this, "power-on")
    sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "child off"
	parent.commandValue(this, "power-off")
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