package org.wso2.carbon.esb.forceful.json;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPBody;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.sun.phobos.script.javascript.RhinoScriptEngineFactory;

import java.util.Iterator;

/***
 * Reason for this mediator: When an XML payload is returning from the Backend , users cannot retrieve the 
 * JSON payload by using mc.getPayloadJSON() in the script mediator.
 *IMPORTANT: Build the jar, and copy to {carbon.home}/repository/components/lib and restart the server.
 * Use this before script mediator . This has been tested with ESB 4.7.0 and ESB 4.8.1 only.
 *         <class name="org.wso2.carbon.esb.forceful.json.ForcefulJsonConvertor"/>
 *         <script .....
 To solve this problem this mediator was developed
 * Here this converts the XML payload to JSON and set it as a property of the message context with the name 
 * "FORCE_BUILT_JSON", to retrive the payload and 
 * 
 * var jsonPayload=mc.getProperty("FORCE_BUILT_JSON");
 * 
 * and to set it back as the message payload to be delivered to the end user
 * 
 * mc.setPayloadJSON(jsonPayload);
 * 
 * @author asankad@wso2.com
 *
 */

public class ForcefulJsonConvertor extends AbstractMediator {

	public static final String DEFAULT_JSON_ROOT_ELEMENT_NAME = "jsonObject";
	private boolean isDebugEnabled;
	public boolean mediate(MessageContext context) {
		// TODO Implement your mediation logic here
        isDebugEnabled=log.isDebugEnabled();
		log.debug("Inside forceful json convertor mediator");
		org.apache.axiom.soap.SOAPBody soapBody = context.getEnvelope().getBody();
		OMElement body=null;
		if(isArray(soapBody)){//check whether incoming payload is an array
			OMElement rootElement= OMAbstractFactory.getOMFactory().createOMElement(new QName(DEFAULT_JSON_ROOT_ELEMENT_NAME));
			Iterator<OMElement> bodyElements=soapBody.getChildElements();
			while (bodyElements.hasNext()){
				rootElement.addChild(bodyElements.next());
			}
			body=rootElement;

		}else{
			body = soapBody.getFirstElement();
		}

        if(isDebugEnabled) {
            log.debug("Message body retireved:" + body.toString());
        }
		JSONObject jsonObj = null;
		try {
			jsonObj = XML.toJSONObject(body.toString());// convert to JSON
														// Object
			if(jsonObj.has(DEFAULT_JSON_ROOT_ELEMENT_NAME)){//to Deal with the jsonObject element added in some builders
				JSONObject innerObject =(JSONObject) jsonObj.get(DEFAULT_JSON_ROOT_ELEMENT_NAME);
				if(innerObject!=null){
					jsonObj=innerObject;
				}
			}

			log.debug("JSON Object created:" + jsonObj.toString());

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			log.fatal("Error occured during XML to JSON conversion");
			return false;

		}
		ScriptEngineManager manager = new ScriptEngineManager();
		manager.registerEngineExtension("jsEngine",
				new RhinoScriptEngineFactory());
		ScriptEngine se = manager.getEngineByExtension("jsEngine");// get script
																	// engine
																	// for json
																	// evaluation
		log.debug("ScripEngine intialized");

		Object nativeObj = null;// to hold the NativeObject from json evaluation by
							// the script engine
		String jsonString = jsonObj.toString(); // json string of the pay load

		try {

			nativeObj = se.eval("(" + jsonString + ")");// this returns a scriptable
													// json object that is
													// compatible to use within
													// script mediator
			log.debug("Script mediator compatible Json object created");

		} catch (Exception e) {
			// TODO: handle exception
			log.fatal("Error occured during converting JSONObject to Scriptable JSON Object");
			return false;

		}
		context.setProperty("FORCE_BUILT_JSON", nativeObj);// setting the force build
														// object to message
														// context , so that
														// this can be taken in
														// the script mediator
														// to use the json
														// payload

		log.debug("Exiting the ForcefulJsonConvertor ....");
		return true;
	}

	private boolean isArray(SOAPBody body){
		log.debug("Entering isArray mehtod");
		int count=0;
		Iterator<OMElement> elements = body.getChildElements();
		while(elements.hasNext()){
			OMElement tmp=elements.next();
			count++;
			if(count>1){
				log.debug("Incoming payload is a JSON Array");
				return true;
			}

		}
		log.debug("Incoming payload is NOT a JSON Array");
		return false;

	}
}
