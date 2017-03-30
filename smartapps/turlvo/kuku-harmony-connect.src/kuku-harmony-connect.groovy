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

def mainPage() {
    if (!atomicState?.isInstalled) {
        installPage()
    } else {
        dynamicPage(name: "mainPage", title: "", uninstall: true) {
            if (installHub) {                
                section("Harmony-API IP Address :") {
                    paragraph "${harmonyHubIP}"                    
                }
                
                section("Harmony-Hub :") {
                    paragraph "${installHub}"                    
                }
                

                section("Devices") {
                    app( name: "harmonyDevices", title: "Add a device...", appName: "KuKu Harmony (Child)", namespace: "turlvo", multiple: true, uninstall: false)
                }
            }
        }
    }
}

def installPage() {
	return dynamicPage(name: "installPage", title: "", nextPage:"installHubPage") {
    	section("Enter the Harmony-API IP address :") {
        	input name: "harmonyHubIP", type: "text", required: true, title: "IP address?"
        }
    }  	
    
}

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

def addDeviceDone() {
    //def devices = getDevices()    
    log.debug "addDeviceDone: $selectedDevice"
    app.updateLabel("$selectedDevice")
    //log.debug "addDeviceDone: $selectedFunctions"

    //addChildDevice("kukuharmony", "KuKu Harmony", "asdfasfd12312", "kuku", [ "label": "Sonoff Wifi Switch"])

    def device = []
    
    device = parent.getDeviceByName("$selectedDevice")
    log.debug "addDeviceDone>> device: $device"    

    def deviceId = device.id
    def existing = getChildDevice(deviceId)
    if (!existing) {
        def childDevice = addChildDevice("kukuharmony", "KuKu Harmony", deviceId, null, ["label": device.label])
    } else {
        log.debug "Device already created"
    }
}

// Default Method
def installed() {
    atomicState.isInstalled = true    
    //initHarmonyDevInfo(harmonyHubIP, installHub)
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


// Hub Command
def getSelectedHub() {
	return atomicState.hub
}

def initHarmonyDevInfo(hubip, hubname) {	
	atomicState.hub = hubname
    atomicState.hubIP = hubip
}

def getLabelsOfDevices(devices) {
	def labels = []
    devices.each { 
        //log.debug "labelOfDevice: $it"
        labels.add(it.label)
    }
    
    return labels

}

def getLabelsOfCommands(cmds) {
	def labels = []
    log.debug "getLabelsOfCommands>> cmds"
    cmds.each {
    	log.debug "getLabelsOfCommands: it.label : $it.label, slug : $it.slug"
    	labels.add(it.label)
    }
    
    return labels
}

def getCommandsOfDevice() {
    //log.debug "getCommandsOfDevice>> $atomicState.foundCommandOfDevice"
    
    return atomicState.foundCommandOfDevice

}

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
 

def getHubStatus(hubName) {
	def params = [
        uri: getHubUri(),
        path: "/hubs/" + "$hubName" + "/status"
    ]
    //log.debug "uri : $params"
    
    try {
        httpGet(params) {resp ->
            log.debug "resp data: ${resp.data}"
            //result = ${resp.data}
        }
    } catch (e) {
        log.error "error: $e"        
    }
    //return result
}

def getHubDevices() {
	return atomicState.devices
}

def getHubDevicesCommands() {
	atomicState.commands.each {
    	log.debug "getHubDevicesCommands>> $it"
    }
	return atomicState.commands
}


// HubAction Methos
def sendCommandToDevice(device, command) {
    sendHubCommand(setHubAction(atomicState.hubIP, "/hubs/$atomicState.hub/devices/$device/commands/$command", "sendCommandToDevice_response"))
}

def sendCommandToDevice_response(resp) {
    def result = []
    def body = new groovy.json.JsonSlurper().parseText(parseLanMessage(resp.description).body)
    log.debug("discoverHubs_response >> $body")
}

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

def getHubAction(host, url, callback) {
	log.debug "getHubAction>> $host, $url, $callback"
    return new physicalgraph.device.HubAction("GET ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n",
            physicalgraph.device.Protocol.LAN, "${host}", [callback: callback])
}

def setHubAction(host, url, callback) {
	log.debug "getHubAction>> $host, $url, $callback"
    return new physicalgraph.device.HubAction("POST ${url} HTTP/1.1\r\nHOST: ${host}\r\n\r\n",
            physicalgraph.device.Protocol.LAN, "${host}", [callback: callback])
}