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
 
definition(
    name: "KuKu Harmony (Child)", 
    namespace: "turlvo",
    author: "KuKu",
    description: "This is a SmartApp that support to control Harmony's device!",
    category: "My Apps",

    parent: "turlvo:KuKu Harmony (Connect)",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    page(name: "addDevicePage")
}

def addDevicePage() {
    return dynamicPage(name: "addDevicePage", title: "Add Device", refreshInterval: 5, uninstall: true, install: true) {
    	if (atomicState.selectedHub == null 
        	|| atomicState.selectedHub == []) {        
    		log.debug "atomicState.selectedHub is null... get a data"
            atomicState.selectedHub = parent.getSelectedHub()
            log.debug "atomicState.selectedHub is $atomicState.selectedHub"
        }
        if (selectedDevice != null
        	&& (atomicState.deviceCommands == null
        	|| atomicState.deviceCommands == [])) {
        	log.debug "atomicState.deviceCommands is null... get a data"
        	atomicState.deviceCommands = parent.getCommandsOfDevice()
            log.debug "atomicState.deviceCommands is $atomicState.deviceCommands"
        }
        log.debug "selectedHub: $atomicState.selectedHub"            
        section("Select Device") {
            def devices = parent.getHubDevices()                    
            def labelOfDevice = parent.getLabelsOfDevices(devices)
            input name: "selectedDevice", type: "enum", title: "Devices", multiple: false, options: labelOfDevice, submitOnChange: true, required: true
            parent.discoverCommandsOfDevice(selectedDevice)
        }
        
        if (selectedDevice) {
            section("Select Device Type") {                              
                def deviceType = ["Default", "Aircon", "TV/STB", "Roboking", "Fan"]
                input name: "selectedDeviceType", type: "enum", title: "Device Type", multiple: false, options: deviceType, submitOnChange: true, required: true
                parent.discoverCommandsOfDevice(selectedDevice)
            }
        }  
		
        if (selectedDeviceType && atomicState.deviceCommands) {    
        	atomicState.selectedDeviceType = selectedDeviceType
            switch (selectedDeviceType) {
            case "Aircon":
                addAirconDevice()
                break
            case "TV":
            	addTvDevice()
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
    }

    if (selectedDevice && selectedDeviceType) {
        addDeviceDone(selectedDeviceType)
    }
}

// Add device page for Default On/Off device
def addDefaultDevice() {
    def labelOfCommand = parent.getLabelsOfCommands(atomicState.deviceCommands)

    section("Commands") {            
        input name: "selectedPowerOn", type: "enum", title: "Power On", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedPowerOff", type: "enum", title: "Power Off", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
    }
}

// Add device page for Default On/Off device
def addFanDevice() {
    def labelOfCommand = parent.getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]  

    section("Commands") {            
        input name: "selectedPower", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedSpeed", type: "enum", title: "Speed", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedSwing", type: "enum", title: "Swing", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedTimer", type: "enum", title: "Timer", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
    }
    
    state.selectedCommands["power"] = selectedPower
	state.selectedCommands["speed"] = selectedSpeed
    state.selectedCommands["swing"] = selectedSwing
    state.selectedCommands["timer"] = selectedTimer
}

// Add device page for Aircon
def addAirconDevice() {
    def labelOfCommand = parent.getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands") {            
        input name: "selectedPowerToggle", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedTempUp", type: "enum", title: "Temperature Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedMode", type: "enum", title: "Mode", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedJetCool", type: "enum", title: "JetCool", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedTempDown", type: "enum", title: "Temperature Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedSpeed", type: "enum", title: "Fan Speed", options: labelOfCommand, submitOnChange: true, multiple: false, required: false       
    }
    
    state.selectedCommands["power"] = selectedPowerToggle
	state.selectedCommands["tempup"] = selectedTempUp
    state.selectedCommands["mode"] = selectedMode
    state.selectedCommands["jetcool"] = selectedJetCool
    state.selectedCommands["tempdown"] = selectedTempDown
    state.selectedCommands["speed"] = selectedSpeed

}

// Add device page for TV
def addTvDevice() {
    def labelOfCommand = parent.getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands") {            
        input name: "selectedPowerToggle", type: "enum", title: "Power Toggle", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
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
    
    state.selectedCommands["power"] = selectedPowerToggle
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
    def labelOfCommand = parent.getLabelsOfCommands(atomicState.deviceCommands)
    state.selectedCommands = [:]    

    section("Commands") {            
        input name: "selectedStart", type: "enum", title: "Start", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedStop", type: "enum", title: "Stop", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
        input name: "selectedUp", type: "enum", title: "Up", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedDown", type: "enum", title: "Down", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
        input name: "selectedLeft", type: "enum", title: "Left", options: labelOfCommand, submitOnChange: true, multiple: false, required: false    
        input name: "selectedRight", type: "enum", title: "Right", options: labelOfCommand, submitOnChange: true, multiple: false, required: false
        input name: "selectedHome", type: "enum", title: "Home", options: labelOfCommand, submitOnChange: true, multiple: false, required: false  
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
def addDeviceDone(devicetype) {
    //def devices = getDevices()    
    log.debug "addDeviceDone: $selectedDevice"
    app.updateLabel("$selectedDevice")

    def device = []    
    device = parent.getDeviceByName("$selectedDevice")
    log.debug "addDeviceDone>> device: $device"    

    def deviceId = device.id
    def existing = getChildDevice(deviceId)
    if (!existing) {
        def childDevice = addChildDevice("turlvo", "KuKu Harmony_${atomicState.selectedDeviceType}", deviceId, null, ["label": device.label])
    } else {
        log.debug "Device already created"
    }
}

def installed() {
    //atomicState.isInstalled = true    
    initialize()
    return true
}

def updated() {
    //unsubscribe()
    initialize()
    return true
}

def initialize() {
	log.debug "initialize()"
    //parent ? mainPage() : addDevicePage()
    addDeviceDone()
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}


private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}


// For child Device
def command(child, command) {
	def device = parent.getDeviceByName("$selectedDevice")
    
	log.debug "childApp parent command(child)>>  $selectedDevice, command: $command, changed Command: ${state.selectedCommands[command]}"
    def commandSlug = parent.getSlugOfCommandByLabel(atomicState.deviceCommands, state.selectedCommands[command])
    log.debug "childApp parent command(child)>>  commandSlug : $commandSlug"
    
    def result
    result = parent.sendCommandToDevice(device.slug, commandSlug)
    if (result && result.message != "ok") {
        parent.sendCommandToDevice(device.slug, commandSlug)
    }
}

def commandValue(child, command) {
	def device = parent.getDeviceByName("$selectedDevice")
    
	log.debug "childApp parent commandValue(child)>>  $selectedDevice, command: $command"
    
    def result
    result = parent.sendCommandToDevice(device.slug, command)
    if (result && result.message != "ok") {
        parent.sendCommandToDevice(device.slug, command)
    }
}




def on(child) {
	log.debug "childApp parent on()>>  $selectedDevice, $selectedPowerOn"
    def device = []
    device = parent.getDeviceByName("$selectedDevice")
    log.debug "on>> device : $device"
    
    def deviceCommands = parent.getCommandsOfDevice()
    def commandSlug = parent.getSlugOfCommandByLabel(deviceCommands, selectedPowerOn)
    log.debug "childApp parent on() >>  $selectedPowerOffFunction, $commandSlug"
    
    def result
    result = parent.sendCommandToDevice(device.slug, commandSlug)
    if (result && result.message != "ok") {
   		parent.sendCommandToDevice(device.slug, commandSlug)
    }
}

def off(child) {
	log.debug "childApp parent off()>>  $selectedDevice, $selectedPowerOff"
    def device = parent.getDeviceByName("$selectedDevice")
    log.debug "off>> device : $device"

	def deviceCommands = parent.getCommandsOfDevice()    
    def commandSlug = parent.getSlugOfCommandByLabel(deviceCommands, selectedPowerOff)
    log.debug "childApp parent off() >>  $selectedPowerOffFunction, $commandSlug"
    
    def result
    result = parent.sendCommandToDevice(device.slug, commandSlug)
    if (result && result.message != "ok") {
        parent.sendCommandToDevice(device.slug, commandSlug)
    }
}