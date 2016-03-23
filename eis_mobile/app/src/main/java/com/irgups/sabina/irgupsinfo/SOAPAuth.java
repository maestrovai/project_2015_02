package com.irgups.sabina.irgupsinfo;

import org.apache.commons.codec.digest.UnixCrypt;
import org.ksoap2.HeaderProperty;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by eugine on 21.07.15.
 */
public class SOAPAuth {
    public String getAccess(String systemId, String sessionId) {
        if (MainActivity.secretToken.length() > 10 && !MainActivity.userId.equals("")) {
            SoapObject Request = new SoapObject(SOAP.NAMESPACE, "asu_SetQRRequestForSession");
            Request.addProperty("user_id", MainActivity.userId);
            Request.addProperty("system_id", systemId);
            Request.addProperty("session_id", sessionId);
            Request.addProperty("code", UnixCrypt.crypt(sessionId, MainActivity.secretToken));
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER10);
            soapEnvelope.setOutputSoapObject(Request);
            soapEnvelope.bodyOut = Request;
            TrustManagerManipulator.allowAllSSL();
            HttpTransportSE aht = new HttpTransportSE(SOAP.URL);
            //HttpsTransportSE aht = new HttpsTransportSE(SOAP.URL,443,"/eis/soap/soap.php",1000);
            aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            aht.debug = true;
            try {
                List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
                headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode("mob_info:password".getBytes())));
                aht.call(SOAP.SOAP_ACTION, soapEnvelope,headerList);
            } catch (Exception e) {
                return e.toString();
            }
        } else {
            return "Не настроен секретный ключ";
        }
        return "";
    }
    public String getDocument(String systemId, String sessionId, String docId) {
        if (MainActivity.secretToken.length() > 10 && !MainActivity.userId.equals("")) {
            SoapObject Request = new SoapObject(SOAP.NAMESPACE, "asu_SetQRRequestForDocument");
            Request.addProperty("user_id", MainActivity.userId);
            Request.addProperty("system_id", systemId);
            Request.addProperty("session_id", sessionId);
            Request.addProperty("code", docId);
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER10);
            soapEnvelope.setOutputSoapObject(Request);
            soapEnvelope.bodyOut = Request;
            HttpTransportSE aht = new HttpTransportSE(SOAP.URL);
            //HttpsTransportSE aht = new HttpsTransportSE(SOAP.URL,443,"/eis/soap/soap.php",1000);
            aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            aht.debug = true;
            try {
                List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
                headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode("mob_info:password".getBytes())));
                aht.call(SOAP.SOAP_ACTION, soapEnvelope,headerList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return "Не настроен секретный ключ";
        }
        return "";
    }
}
