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
def version() {	return "v0.1.5.002" }
/*
 *	03/28/2017 >>> v0.1.0.000 - Release first KuKu Harmony supports only on/off command for each device
 *  04/13/2017 >>> v0.1.3.000 - Added Aircon, Fan, Roboking device type
 *  04/14/2017 >>> v0.1.4.000 - Added TV device type
 *  04/21/2017 >>> v0.1.4.100 - changed DTH's default state to 'Off'
 *  04/21/2017 >>> v0.1.4.150 - update on/off state routine and slide
 *  04/22/2017 >>> v0.1.4.170 - changed 'addDevice' page's refreshInterval routine and change all device's power on/off routine
 *  04/22/2017 >>> v0.1.4.181 - changed routine of discovering hub and added checking hub's state
 *  05/16/2017 >>> v0.1.5.000 - support multiple Harmony hubs
 *  05/19/2017 >>> v0.1.5.002 - fixed 'STB' device type crash bug and changed refresh interval
 */

definition(
    name: "KuKu Harmony${parent ? " - Device" : ""}",
    namespace: "turlvo",
    author: "KuKu",
    description: "This is a SmartApp that support to control Harmony's device!",
    category: "My Apps",
    parent: parent ? "turlvo.KuKu Harmony" : null,
    singleInstance: true,
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	page(name: "parentOrChildPage")
    
    page(name: "mainPage")
    page(name: "installPage")
    page(name: "mainChildPage")
    
}

// ------------------------------
// Pages related to Parent

def parentOrChildPage() {
	parent ? mainChildPage() : mainPage()
}

// mainPage
// seperated two danymic page by 'isInstalled' value 
def mainPage() {
    if (!atomicState?.isInstalled) {
        return dynamicPage(name: "installPage", title: "", install: true) {
            section("Enter the Harmony-API Server IP address :") {
                input name: "harmonyHubIP", type: "text", required: true, title: "IP address?"
            }
        } 	    
    } else {    	                    
        return dynamicPage(name: "mainPage", title: "", uninstall: true) {
            //getHubStatus()
            section("Harmony-API Server IP Address :") {
                paragraph "${harmonyHubIP}"
            }

            section("") {
                app( name: "harmonyDevices", title: "Add a device...", appName: "KuKu Harmony", namespace: "turlvo", multiple: true, uninstall: false)
            }

            section("KuKu Harmony Version :") {
                paragraph "${version()}"
            }
        }
    }
}

def initializeParent() {
    atomicState.isInstalled = true
    atomicState.harmonyApiServerIP = harmonyHubIP
    atomicState.hubStatus = "online"
}

def getHarmonyApiServerIP() {
	return atomicState.harmonyApiServerIP
}

// ------------------------------
// Pages realted to Child App
def mainChildPage() {
    def interval
    if (atomicState.discoverdHubs && atomicState.deviceCommands && atomicState.device) {
        interval = 60
    } else {
        interval = 3
    }
    return dynamicPage(name: "mainChildPage", title: "Add Device", refreshInterval: interval, uninstall: true, install: true) {    	
        log.debug "mainChildPage>> parent's atomicState.harmonyApiServerIP: ${parent.getHarmonyApiServerIP()}"
        atomicState.harmonyApiServerIP = parent.getHarmonyApiServerIP()
        
        log.debug "installHubPage>> $atomicState.discoverdHubs"        
        if (atomicState.discoverdHubs == null) {
            discoverHubs(atomicState.harmonyApiServerIP)
            section() {            
                paragraph "Discovering Harmony Hub.  Please wait..."
            }
        } else {
            section("Hub :") {                
                //def hubs = getHubs(harmonyHubIP)                    
                input name: "selectHub", type: "enum", title: "Select Hub", options: atomicState.discoverdHubs, submitOnChange: true, required: true
                log.debug "mainChildPage>> selectHub: $selectHub"
                if (selectHub) {
                    discoverDevices(selectHub)
                    atomicState.hub = selectHub
                }                
            }
        }    

        def foundDevices = getHubDevices()
        if (atomicState.hub && foundDevices) {
            section("Device :") {                
                def labelOfDevice = getLabelsOfDevices(foundDevices)
                input name: "selectedDevice", type: "enum",  title: "Select Device", multiple: false, options: labelOfDevice, submitOnChange: true, required: true
                if (selectedDevice) {
                	discoverCommandsOfDevice(selectedDevice)
                    atomicState.device = selectedDevice
                }
            }

            if (selectedDevice) {
                section("Device Type :") {
                    def deviceType = ["Default", "Aircon", "TV", "Roboking", "Fan"]
                    input name: "selectedDeviceType", type: "enum", title: "Select Device Type", multiple: false, options: deviceType, submitOnChange: true, required: true                    
                }
            }  


            atomicState.deviceCommands = getCommandsOfDevice()
            if (selectedDeviceType && atomicState.deviceCommands) {    
                atomicState.selectedDeviceType = selectedDeviceType
                switch (selectedDeviceType) {
                    case "Aircon":
                    addAirconDevice()
                    break
                    case "TV":
                    case "STB":
                    addTvDeviceTV()
                    break
                    case "STB":
                    break
                    case "Roboking":
                    addRobokingDevice()
                    break
                    case "Fan":
                    addFanDevice()
                    break
                    default:
                        log.debug "selectedDeviceType>> default"
                    addDefaultDevice()
                }
            } else if (selectedDeviceType && atomicState.deviceCommands == null) {
                // log.debug "addDevice()>> selectedDevice: $selectedDevice, commands : $commands"
                section("") {
                    paragraph "Loading selected device's command.  This can take a few seconds. Please wait..."
                }
            }
        } else if (atomicState.hub) {
            section() {
                paragraph "Discovering devices.  Please wait..."
            }
        }
    }
}

// Add device page for Default On/Off device
def addDefaultDevice() {
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
	state.selectedCommands = [:]    
    
    section("Commands :") {            
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
    }
    state.selectedCommands["power-on"] = selectedPowerOn
	state.selectedCommands["power-off"] = selectedPowerOff
}

// Add device page for Fan device
def addFanDevice() {
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]  

    section("Commands :") {            
       // input name: "selectedPower", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedSpeed", type: "enum", title: "Speed", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedSwing", type: "enum", title: "Swing", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedTimer", type: "enum", title: "Timer", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
    }
    //state.selectedCommands["power"] = selectedPower
    state.selectedCommands["power-on"] = selectedPowerOn
	state.selectedCommands["power-off"] = selectedPowerOff    
	state.selectedCommands["speed"] = selectedSpeed
    state.selectedCommands["swing"] = selectedSwing
    state.selectedCommands["timer"] = selectedTimer
}

// Add device page for Aircon
def addAirconDevice() {
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands :") {            
        //input name: "selectedPowerToggle", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedTempUp", type: "enum", title: "Temperature Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedMode", type: "enum", title: "Mode", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedJetCool", type: "enum", title: "JetCool", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedTempDown", type: "enum", title: "Temperature Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedSpeed", type: "enum", title: "Fan Speed", options: labelOfCommand, submitOnChange: true, multiple: false, required: false       
    }
    
    //state.selectedCommands["power"] = selectedPowerToggle
    state.selectedCommands["power-on"] = selectedPowerOn
    state.selectedCommands["power-off"] = selectedPowerOff    
	state.selectedCommands["tempup"] = selectedTempUp
    state.selectedCommands["mode"] = selectedMode
    state.selectedCommands["jetcool"] = selectedJetCool
    state.selectedCommands["tempdown"] = selectedTempDown
    state.selectedCommands["speed"] = selectedSpeed

}

// Add device page for TV
def addTvDeviceTV() {
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands :") {            
        //input name: "selectedPowerToggle", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedVolumeUp", type: "enum", title: "Volume Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedChannelUp", type: "enum", title: "Channel Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedMute", type: "enum", title: "Mute", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedVolumeDown", type: "enum", title: "Volume Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedChannelDown", type: "enum", title: "Channel Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false      
        input name: "selectedMenu", type: "enum", title: "Menu", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedHome", type: "enum", title: "Home", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedInput", type: "enum", title: "Input", options: labelOfCommand, submitOnChange: true, multiple: false, required: false              
        input name: "selectedBack", type: "enum", title: "Back", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
    }
    
    //state.selectedCommands["power"] = selectedPowerToggle
    state.selectedCommands["power-on"] = selectedPowerOn
    state.selectedCommands["power-off"] = selectedPowerOff  
	state.selectedCommands["volup"] = selectedVolumeUp
    state.selectedCommands["chup"] = selectedChannelUp
    state.selectedCommands["mute"] = selectedMute
    state.selectedCommands["voldown"] = selectedVolumeDown
    state.selectedCommands["chdown"] = selectedChannelDown
    state.selectedCommands["menu"] = selectedMenu
    state.selectedCommands["home"] = selectedHome
    state.selectedCommands["input"] = selectedInput
    state.selectedCommands["back"] = selectedBack

}
// Add device page for Aircon
def addRobokingDevice() {
    def labelOfCommand = getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands :") {
        input name: "selectedStart", type: "enum", title: "Start", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedHome", type: "enum", title: "Home", options: labelOfCommand, submitOnChange: true, multiple: false, required: true  
        input name: "selectedStop", type: "enum", title: "Stop", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedUp", type: "enum", title: "Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedDown", type: "enum", title: "Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedLeft", type: "enum", title: "Left", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedRight", type: "enum", title: "Right", options: labelOfCommand, submitOnChange: true, multiple: false, required: false        
        input name: "selectedMode", type: "enum", title: "Mode", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedTurbo", type: "enum", title: "Turbo", options: labelOfCommand, submitOnChange: true, multiple: false, required: false   
    }

	state.selectedCommands["start"] = selectedStart
    state.selectedCommands["stop"] = selectedStop
    state.selectedCommands["up"] = selectedUp
    state.selectedCommands["down"] = selectedDown
    state.selectedCommands["left"] = selectedLeft
    state.selectedCommands["right"] = selectedRight
    state.selectedCommands["home"] = selectedHome
    state.selectedCommands["mode"] = selectedMode
    state.selectedCommands["turbo"] = selectedTurbo

}

// Install child device
def initializeChild(devicetype) {
    //def devices = getDevices()    
    log.debug "addDeviceDone: $selectedDevice"
    app.updateLabel("$selectedDevice")

    def device = []    
    device = getDeviceByName("$selectedDevice")
    log.debug "addDeviceDone>> device: $device"    

    def deviceId = device.id
    def existing = getChildDevice(deviceId)
    if (!existing) {
        def childDevice = addChildDevice("turlvo", "KuKu Harmony_${atomicState.selectedDeviceType}", deviceId, null, ["label": device.label])
    } else {
        log.debug "Device already created"
    }
}

// For child Device
def command(child, command) {
	def device = getDeviceByName("$selectedDevice")
    
	log.debug "childApp parent command(child)>>  $selectedDevice, command: $command, changed Command: ${state.selectedCommands[command]}"
    def commandSlug = getSlugOfCommandByLabel(atomicState.deviceCommands, state.selectedCommands[command])
    log.debug "childApp parent command(child)>>  commandSlug : $commandSlug"
    
    def result
    result = sendCommandToDevice(device.slug, commandSlug)
    if (result && result.message != "ok") {
        sendCommandToDevice(device.slug, commandSlug)
    }
}

def commandValue(child, command) {
	def device = getDeviceByName("$selectedDevice")
    
	log.debug "childApp parent commandValue(child)>>  $selectedDevice, command: $command"
    
    def result
    result = sendCommandToDevice(device.slug, command)
    if (result && result.message != "ok") {
        sendCommandToDevice(device.slug, command)
    }
}



// ------------------------------------
// ------- Default Common Method -------
def installed() {    
    initialize()
}

def updated() {
    //unsubscribe()
    initialize()
}

def initialize() {
	log.debug "initialize()"
   parent ? initializeChild() : initializeParent()
}


def uninstalled() {
	parent ? null : removeChildDevices(getChildDevices())
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
    sendHubCommand(setHubAction(atomicState.harmonyApiServerIP, "/hubs/$atomicState.hub/devices/$device/commands/$command", "sendCommandToDevice_response"))
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
    log.debug "getHubStatus"
    sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$atomicState.hub/status", "getHubStatus_response"))
    if (atomicState.getHubStatusWatchdog == true) {
    	atomicState.hubStatus = "offline"
    }
    atomicState.getHubStatusWatchdog = true        
}

def getHubStatus_response(resp) {
   	def result = []
    atomicState.getHubStatusWatchdog = false
    
    if (resp.description != null && parseLanMessage(resp.description).body) {
    	log.debug "getHubStatus_response>> response: $resp.description"
    	def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
	
        if(body && body.off != null) {            	
            log.debug "getHubStatus_response>> $body.off"
            if (body.off == false) {
            	atomicState.hubStatus = "online"
            }
        } else {
            log.debug "getHubStatus_response>> $body.off"
            atomicState.hubStatus = "offline"
        }
    } else {
    	log.debug "getHubStatus_response>> Status error"
        atomicState.hubStatus = "offline"
    }
}

// discoverCommandsOfDevice
// parameter : 
// - name : name of device searching command
// return : 'discoverCommandsOfDevice_response()' method callback
def discoverCommandsOfDevice(name) {
	device = getDeviceByName(name)
    log.debug "discoverCommandsOfDevice>> name:$name, device:$device"
    
    sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$atomicState.hub/devices/${device.slug}/commands", "discoverCommandsOfDevice_response"))

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
	sendHubCommand(getHubAction(atomicState.harmonyApiServerIP, "/hubs/$hubname/devices", "discoverDevices_response"))
}

def discoverDevices_response(resp) {
	def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("discoverHubs_response >> $body.devices")
	
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
	
    if(body && body.hubs != null) {            	
        body.hubs.each {
            log.debug "discoverHubs_response: $it"
            result.add(it)
        }
        atomicState.discoverdHubs = result
    } else {
    	atomicState.discoverdHubs = null
    }    
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