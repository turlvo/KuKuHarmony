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
    return dynamicPage(name: "addDevicePage", title: "Add Device", refreshInterval: 3, uninstall: true, install: true) {
    	def selectedHub = parent.getSelectedHub()
        def deviceCommands = parent.getCommandsOfDevice()        
        log.debug "selectedHub: $selectedHub"            
        section("Select Device") {
            def devices = parent.getHubDevices()                    
            def labelOfDevice = parent.getLabelsOfDevices(devices)
            input name: "selectedDevice", type: "enum", title: "Devices", multiple: false, options: labelOfDevice, submitOnChange: true, required: true
            parent.discoverCommandsOfDevice(selectedDevice)
        }

        if (selectedDevice && deviceCommands) {                         
            def labelOfCommand = parent.getLabelsOfCommands(deviceCommands)
            section("Select Power-on command") {            
                input name: "selectedPowerOnFunction", type: "enum", title: "Functions", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
            }
            section("Select Power-off command") {            
                input name: "selectedPowerOffFunction", type: "enum", title: "Functions", options: labelOfCommand, submitOnChange: true, multiple: false, required: true
            }
        } else if (selectedDevice && deviceCommands == null) {
            // log.debug "addDevice()>> selectedDevice: $selectedDevice, commands : $commands"
            section("") {
                paragraph "Loading selected device's command.  This can take a minute. Please wait..."
            }
        }
    }
 // ToDo: make a DTH with selected command
        if (selectedPowerOnFunction && selectedPowerOffFunction) {
        	addDeviceDone()
        }
}

def addDeviceDone() {
    //def devices = getDevices()    
    log.debug "addDeviceDone: $selectedDevice"
    app.updateLabel("$selectedDevice")
    //log.debug "addDeviceDone: $selectedFunctions"


    def device = []
    //selectedDevice.each {
    device = parent.getDeviceByName("$selectedDevice")
    log.debug "addDeviceDone>> device: $device"    

    def deviceId = device.id
    def existing = getChildDevice(deviceId)
    if (!existing) {
        def childDevice = addChildDevice("turlvo", "KuKu Harmony", deviceId, null, ["label": device.label])
    } else {
        log.debug "Device already created"
    }
    //}

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
def on(child) {
	log.debug "childApp parent on()>>  $selectedDevice, $selectedPowerOnFunction"
    def device = []
    device = parent.getDeviceByName("$selectedDevice")
    log.debug "on>> device : $device"
    
    def deviceCommands = parent.getCommandsOfDevice()
    def commandSlug = parent.getSlugOfCommandByLabel(deviceCommands, selectedPowerOnFunction)
    log.debug "childApp parent on() >>  $selectedPowerOffFunction, $commandSlug"
    
    def result
    result = parent.sendCommandToDevice(device.slug, commandSlug)
    if (result && result.message != "ok") {
   		parent.sendCommandToDevice(device.slug, commandSlug)
    }
}

def off(child) {
	log.debug "childApp parent off()>>  $selectedDevice, $selectedPowerOffFunction"
    def device = parent.getDeviceByName("$selectedDevice")
    log.debug "off>> device : $device"

	def deviceCommands = parent.getCommandsOfDevice()    
    def commandSlug = parent.getSlugOfCommandByLabel(deviceCommands, selectedPowerOffFunction)
    log.debug "childApp parent off() >>  $selectedPowerOffFunction, $commandSlug"
    
    def result
    result = parent.sendCommandToDevice(device.slug, commandSlug)
    if (result && result.message != "ok") {
        parent.sendCommandToDevice(device.slug, commandSlug)
    }
}