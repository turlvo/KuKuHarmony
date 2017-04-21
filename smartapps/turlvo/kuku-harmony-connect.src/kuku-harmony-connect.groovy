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
 *
 *  Version history
 */
def version() {	return "v0.1.4.1" }
/*
 *	03/28/2017 >>> v0.1.0 - Release first KuKu Harmony supports only on/off command for each device
 *  04/13/2017 >>> v0.1.3 - Added Aircon, Fan, Roboking device type
 *  04/13/2017 >>> v0.1.4 - Added TV device type
 *  04/13/2017 >>> v0.1.4.1 - changed DTH's default state to 'Off'
 */

definition(
    name: "KuKu Harmony (Connect)",
    namespace: "turlvo",
    author: "KuKu",
    description: "This is a SmartApp that support to control Harmony's device!",
    category: "My Apps",
    singleInstance: true,
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    page(name: "mainPage")
    page(name: "addDevicePage")
    page(name: "installPage")
    page(name: "installHubPage")
}

// mainPage
// seperated two danymic page by 'isInstalled' value 
def mainPage() {
    if (!atomicState?.isInstalled) {
        installPage()
    } else {
        dynamicPage(name: "mainPage", title: "", uninstall: true) {
            if (installHub) {             	
            	//getHubStatus()
                section("Harmony-API IP Address :") {
                    paragraph "${harmonyHubIP}"                    
                }
                
                section("Harmony-Hub Name :") {
                    paragraph "${installHub}"                    
                }                

                section("") {
                    app( name: "harmonyDevices", title: "Add a device...", appName: "KuKu Harmony (Child)", namespace: "turlvo", multiple: true, uninstall: false)
                }
                
                section("KuKu Harmony Version :") {
					paragraph "${version()}"
                }
            }
        }
    }
}

// installPage
// entering 'Harmony-API' server ip address
def installPage() {
	return dynamicPage(name: "installPage", title: "", nextPage:"installHubPage") {
    	section("Enter the Harmony-API IP address :") {
        	input name: "harmonyHubIP", type: "text", required: true, title: "IP address?"
        }
    }  	
    
}

// installHubPage
// select hub name in searched hub list
def installHubPage() {
	return dynamicPage(name: "installHubPage", title: "", refreshInterval: 3, install: true) {
        if (harmonyHubIP) {
        	atomicState.hubIP = harmonyHubIP
            if (atomicState.discoverdHubs == null) {
        		discoverHubs(harmonyHubIP)
            }
            //log.debug "harmonyHubIP: $harmonyHubIP"            
            section("Harmony-API IP Address :") {
                paragraph "${harmonyHubIP}"
            }
            

            section("Harmony Hub :") {
                if (atomicState.discoverdHubs) {
                    //def hubs = getHubs(harmonyHubIP)                    
                    input name: "installHub", type: "enum", title: "Select Hub", options: atomicState.discoverdHubs, submitOnChange: true, required: true
                    log.debug "installHubPage>> installHub: $installHub"
                    if (installHub) {
                    	discoverDevices(installHub)
                        atomicState.hub = installHub
                    }
                } else {                    
                    paragraph "Discovering Harmony Hub.  Please wait..."
                }
            }
        }    
    }
}


// ------------------------------
// ------- Default Method -------
def installed() {
    atomicState.isInstalled = true    
    initialize()
}

def updated() {
    //unsubscribe()
    initialize()
}

def initialize() {
   // addDeviceDone()
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}


// ---------------------------
// ------- Hub Command -------

// getSelectedHub
// return : Installed hub name
def getSelectedHub() {
	return atomicState.hub
}

// getLabelsOfDevices
// parameter :
// - devices : List of devices in Harmony Hub {label, slug}
// return : Array of devices's label value
def getLabelsOfDevices(devices) {
	def labels = []
    devices.each { 
        //log.debug "labelOfDevice: $it"
        labels.add(it.label)
    }
    
    return labels

}

// getLabelsOfCommands
// parameter :
// - cmds : List of some device's commands {label, slug}
// return : Array of commands's label value
def getLabelsOfCommands(cmds) {
	def labels = []
    log.debug "getLabelsOfCommands>> cmds"
    cmds.each {
    	//log.debug "getLabelsOfCommands: it.label : $it.label, slug : $it.slug"
    	labels.add(it.label)
    }
    
    return labels
}

// getCommandsOfDevice
// return : result of 'discoverCommandsOfDevice(device)' method. It means that recently requested device's commands
def getCommandsOfDevice() {
    //log.debug "getCommandsOfDevice>> $atomicState.foundCommandOfDevice"
    
    return atomicState.foundCommandOfDevice

}

// getSlugOfCommandByLabel
// parameter :
// - commands : List of device's command
// - label : name of command
// return : slug value same with label in the list of command
def getSlugOfCommandByLabel(commands, label) {
	//def commands = []
    def slug
    
    commands.each {    	
    	if (it.label == label) {
        	//log.debug "it.label : $it.label, device : $device"
        	log.debug "getSlugOfCommandByLabel>> $it"
        	//commands = it.commands
            slug = it.slug
        }
    }
    return slug
}

// getDeviceByName
// parameter :
// - name : device name searching
// return : device matched by name in Harmony Hub's devices
def getDeviceByName(name) {
	def device = []    
	atomicState.devices.each {
    	//log.debug "getDeviceByName>> $it.label, $name"
    	if (it.label == name) {
    		log.debug "getDeviceByName>> $it"
            device = it
        }
	}
    
    return device
}
 
// getHubDevices
// return : searched list of device in Harmony Hub when installed
def getHubDevices() {
	return atomicState.devices
}


// --------------------------------
// ------- HubAction Methos -------
// sendCommandToDevice
// parameter : 
// - device : target device
// - command : sending command
// return : 'sendCommandToDevice_response()' method callback
def sendCommandToDevice(device, command) {
    sendHubCommand(setHubAction(atomicState.hubIP, "/hubs/$atomicState.hub/devices/$device/commands/$command", "sendCommandToDevice_response"))
}

def sendCommandToDevice_response(resp) {
    def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("sendCommandToDevice_response >> $body")
}

// getHubStatus
// parameter : 
// return : 'getHubStatus_response()' method callback
def getHubStatus() {
    sendHubCommand(getHubAction(atomicState.hubIP, "/hubs/$atomicState.hub/status", "getHubStatus_response"))
}

def getHubStatus_response(resp) {
   	def result = []
    
    if (parseLanMessage(resp.description).body) {
    	log.debug "getHubStatus_response>> $resp.description"
    	def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
	
        if(body) {            	
            log.debug "getHubStatus_response>> $body.off"
            if (body.off == false) {
            	atomicState.hubStatus = "online"
            }
        }
    } else {
    	log.debug "getHubStatus_response>> Status error"
    }
}

// discoverCommandsOfDevice
// parameter : 
// - name : name of device searching command
// return : 'discoverCommandsOfDevice_response()' method callback
def discoverCommandsOfDevice(name) {
	device = getDeviceByName(name)
    log.debug "discoverCommandsOfDevice>> name:$name, device:$device"
    
    sendHubCommand(getHubAction(atomicState.hubIP, "/hubs/$atomicState.hub/devices/${device.slug}/commands", "discoverCommandsOfDevice_response"))

}

def discoverCommandsOfDevice_response(resp) {
   	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
	
    if(body) {            	
        body.commands.each {            
            def command = ['label' : it.label, 'slug' : it.slug]
            //log.debug "getCommandsOfDevice_response>> command: $command"
            result.add(command)            
        }
    }
    
    atomicState.foundCommandOfDevice = result
}

// discoverDevices
// parameter : 
// - hubname : name of hub searching devices
// return : 'discoverDevices_response()' method callback
def discoverDevices(hubname) {
	log.debug "discoverDevices>> $hubname"
	sendHubCommand(getHubAction(atomicState.hubIP, "/hubs/$hubname/devices", "discoverDevices_response"))
}

def discoverDevices_response(resp) {
	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("discoverHubs_response >> $body.hubs")
	
    if(body) {            	
        body.devices.each {
            //log.debug "getHubDevices_response: $it.id, $it.label, $it.slug"
            def device = ['id' : it.id, 'label' : it.label, 'slug' : it.slug]
            result.add(device)
        }
    }            
    atomicState.devices = result

}


// discoverHubs
// parameter : 
// - host : ip address searching hubs
// return : 'discoverHubs_response()' method callback
def discoverHubs(host) {
	log.debug("discoverHubs")
    return sendHubCommand(getHubAction(host, "/hubs", "discoverHubs_response"))
}

def discoverHubs_response(resp) {
	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("discoverHubs_response >> $body.hubs")
	
    if(body) {            	
        body.hubs.each {
            log.debug "discoverHubs_response: $it"
            result.add(it)
        }
    }            
    atomicState.discoverdHubs = result
}

// -----------------------------
// -------Hub Action API -------
// getHubAction
// parameter :
// - host : target address to send 'GET' action
// - url : target url
// - callback : response callback method name
def getHubAction(host, url, callback) {
	log.debug "getHubAction>> $host, $url, $callback"
    return new physicalgraph.device.HubAction("GET ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n",
            physicalgraph.device.Protocol.LAN, "${host}", [callback: callback])
}

// setHubAction
// parameter :
// - host : target address to send 'POST' action
// - url : target url
// - callback : response callback method name
def setHubAction(host, url, callback) {
	log.debug "getHubAction>> $host, $url, $callback"
    return new physicalgraph.device.HubAction("POST ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n",
            physicalgraph.device.Protocol.LAN, "${host}", [callback: callback])
}