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
            if (selectedHub) {                
                section("Harmony-API Server") {
                    paragraph "${harmonyHubIP}"                    
                }
                
                section("Harmony-Hub") {
                    paragraph "${selectedHub}"                    
                }
                
                initHarmonyDevInfo(selectedHub)
                //getHubDevicesFunctions(selectedHub, 'lg-tv')
                section("Devices") {
                    app( name: "harmonyDevices", title: "Add a device...", appName: "KuKu Harmony (Child)", namespace: "turlvo", multiple: true, uninstall: false)
                }
            }

			// ToDo: make a DTH with selected command
            //if (selectedFunctions) {
            //    log.debug("selectedFunctions: $selectedFunctions")
            //    addDeviceDone()
            //}
        //    if (selectedDevice) {
         //       log.debug("selectedDevice: $selectedDevice")
         //       addDeviceDone()
          //  }
            
        }
    }
}

def installPage() {
	return dynamicPage(name: "installPage", title: "", nextPage:"installHubPage") {
    	section("Enter the Harmony-API IP address") {
        	input name: "harmonyHubIP", type: "text", required: true, title: "IP address?"
        }
    }  	
    
}

def installHubPage() {
	return dynamicPage(name: "installHubPage", title: "", install: true) {
        if (harmonyHubIP) {
            //log.debug "harmonyHubIP: $harmonyHubIP"            
            section("Harmony-API Server") {
                paragraph "${harmonyHubIP}"
            }
            section("Harmony Hub") {
                def hubs = getHubs(harmonyHubIP)                    
                input name: "selectedHub", type: "enum", title: "Select Hub", options: hubs, submitOnChange: true, required: false
            }
        }    
    }
}

def getSelectedHub() {
	return atomicState.hub
}

def initHarmonyDevInfo(hubname) {	
	atomicState.hub = hubname
    atomicState.devices = getHubDevices(hubname)
   
   	// ToDo: make a DTH with selected command
   	def devCommands = [] 
    atomicState.devices.each {
    	//log.debug "initHarmonyInformation: device : $it"
        def commands = getHubDevicesCommands(hubname, it.slug)
        //log.debug "initHarmonyInformation: commands : $commands"
    	devCommands.add(["label":it.label, "commands":commands])
    }
    atomicState.commands = devCommands
    //atomicState.commands.each {
    //	log.debug "initHarmonyInformation: all commands : $it.label"
    //    log.debug "initHarmonyInformation: all commands : $it.commands"
    //}
}

def getLabelsOfDevices(devices) {
	def labels = []
    devices.each { 
        //log.debug "labelOfDevice: $it"
        labels.add(it.label)
    }
    
    return labels

}

def getLabelsOfCommands(commands) {

	def labels = []
    commands.each {
    	//log.debug "getLabelsOfCommands: it.label : $it.label, slug : $it.slug"
    	labels.add(it.label)
    }
    
    return labels
}

def getCommandsOfDevice(device) {
	def commands = []
	atomicState.commands.each {    	
    	if (it.label == device) {
        	//log.debug "it.label : $it.label, device : $device"
        	//log.debug "it.commands : $it.commands"
        	commands = it.commands
        }
    }
    return commands
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
    	log.debug "getDeviceByName>> $it.label, $name"
    	if (it.label == name) {
    		log.debug "getDeviceByName>> $it"
            device = it
        }
	}
    
    return device
}

def installed() {
    atomicState.isInstalled = true    
    initialize()
}

def updated() {
    //unsubscribe()
    initialize()
}



def getHubUri() {
	return "http://kuku.pe.kr:8282"
}

def getHubs(address) {
	log.debug "getHubs(), address: $address"
	def params = [
        uri: "http://$address",
        path: '/hubs'
    ]
    //log.debug "uri : $params"
    def result = []
    try {
        httpGet(params) {resp ->
            //log.debug "resp data: ${resp.data}"
            if(resp.data) {            	
                resp.data.hubs.each {
                    //log.debug "getHubDevices: $it"
                    result.add(it)
                }
            }            
        }
    } catch (e) {
        log.error "error: $e"        
    }
    
    log.debug "result: $result"
    return result
    
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

def getHubActivities(hubName) {
	def params = [
        uri: getHubUri(),
        path: "/hubs/" + "$hubName" + "/activities"
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

def getHubDevices(hubname) {
	def params = [
        uri: "http://$harmonyHubIP",
        path: "/hubs/$hubname/devices"
    ]
    //log.debug "uri : $params"
	
    def result = []
    try {
        httpGet(params) {resp ->
            //log.debug "resp data: ${resp.data}"
            //result = ${resp.data.devices}
            if(resp.data) {            	
                resp.data.devices.each {
                    //log.debug "getHubDevices: $it.id, $it.label, $it.slug"
                    def device = ['id' : it.id, 'label' : it.label, 'slug' : it.slug]
                    result.add(device)
                }
            }
        }
    } catch (e) {
        log.error "error: $e"        
    }
    return result
}

def getHubDevicesCommands(hubname, deviceslug) {
	def params = [
        uri: "http://$harmonyHubIP",
        path: "/hubs/$hubname/devices/$deviceslug/commands"
    ]
    //log.debug "getHubDevicesFunctions: $param"
	def result = []
    def commands = []
    try {
        httpGet(params) {resp ->
            //log.debug "resp data: ${resp.data}"
            if(resp.data) {            	
                resp.data.commands.each {
                    //log.debug "getHubDevicesFunctions: $it"
                    def command = ['label' : it.label, 'slug' : it.slug]    
                    result.add(command)
                }
            }
        }
    } catch (e) {
        log.error "error: $e"        
    }
    //log.debug "getHubDevicesFunctions: commands: $commands"
    //result.add(commands)
    //log.debug "getHubDevicesFunctions: $result"
    return result
}


def sendCommandToDevice(device, command) {
	def params = [
        uri: "http://$harmonyHubIP",
        path: "/hubs/" + "${atomicState.hub}" + "/devices/" + "$device" + "/commands/" + "$command" 
    ]
    log.debug "sendCommandToDevice >> uri : $params"
    
    def result
    try {
        httpPost(params) {resp ->
            log.debug "resp data: ${resp.data}"
            result = resp.data
        }
    } catch (e) {
        log.error "error: $e"        
    }
    return result
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