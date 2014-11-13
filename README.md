ForcefulJsonConvertor
=====================
This custom mediator tested in WSO2 ESB 4.7.0 and 4.8.0. 

<b>Reason for writing the custom mediator</b>

There is <a href="https://docs.wso2.com/display/ESB481/Script+Mediator">Script Mediator</a> in ESB which facilitates 
to use scripting languages in synapse configs. When using javascripts, JsonPayload can be accessed , if the incoming
message is JSON. But there may be a requirements to manipulate the payload as JSON inside the script mediator.

This mediator provides that capabily.

<b>How to Use</b>


1. Build the Jar and copy it to CARBON_HOME/repository/components/lib.
2. Change the Builder and Formatter for application/json in CARBON_HOME/repository/conf/axis2/axis2.xml to
org.apache.axis2.json.JSONStreamFormatter
org.apache.axis2.json.JSONStreamBuilder
```xml
   <class name="org.wso2.carbon.esb.forceful.json.ForcefulJsonConvertor"/>
   <script language="js">
```
   ```javascript
          var json=mc.getProperty("FORCE_BUILT_JSON");
          
          //manipulate the json 
          //
           mc.setPayloadJSON(json);
  ```
   ```xml
   </script>
    
   
```
   
   


