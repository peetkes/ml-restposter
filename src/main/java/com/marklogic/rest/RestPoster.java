package com.marklogic.rest;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.MixedMultipartEntityBuilder;
import org.apache.http.entity.mime.content.MLStringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by pkester on 31/10/16.
 */
public class RestPoster {
    private static final Logger log = LogManager.getLogger(RestPoster.class);
    private static String mlHostName = "localhost";
    private static int mlPort = 8004;
    private static String mlUser = "admin";
    private static String mlPasswrd = "admin";
    private static String mlEndpoint = "/v1/documents";
    private final CloseableHttpClient httpSrc;

    public RestPoster() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(2);
        CredentialsProvider srcCredsProvider = new BasicCredentialsProvider();
        srcCredsProvider.setCredentials(
                new AuthScope(mlHostName, mlPort),
                new UsernamePasswordCredentials(mlUser, mlPasswrd));
        this.httpSrc = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCredentialsProvider(srcCredsProvider)
                .build();

    }

    public int post(List<MLPayLoad> payLoads, Date date, String[] collections) throws URISyntaxException, IOException {
        URI uriSrc = new URIBuilder()
                .setScheme("http")
                .setHost(mlHostName)
                .setPort(mlPort)
                .setPath(mlEndpoint)
                .build();
        JSONArray colls = new JSONArray();
        for (String collection : collections) {
            colls.add(collection);
        }
        JSONObject obj = new JSONObject();
        obj.put("collections",colls);
        String jsonCollection = JSONValue.toJSONString(obj);
        ContentBody inlineColls = new MLStringBody(jsonCollection, ContentType.create("text/json", "UTF-8"),null);

        List<ContentBody> contentBodies = new ArrayList<ContentBody>();

        for (MLPayLoad payLoad : payLoads) {
//			ContentType contentType = ContentType.create("text/json", "UTF-8");
            if (payLoad.hasMsg()) {
                contentBodies.add(new MLStringBody(payLoad.getMsg(), payLoad.getContentType(), payLoad.getUri()));
            } else {
                log.error("No payload for uri:".concat(payLoad.getUri()));
            }
        }
        if (contentBodies.isEmpty()) {
            return 0;
        } else {
            HttpEntity reqEntity = MixedMultipartEntityBuilder.create()
                    .addPart("inline; category=metadata", inlineColls)
                    .addParts("attachment",contentBodies)
                    .build();
            HttpPost postRequest = new HttpPost(uriSrc);
            postRequest.setEntity(reqEntity);
            CloseableHttpResponse response = httpSrc.execute(postRequest);
            try {
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
            return response.getStatusLine().getStatusCode();
        }
    }
    public int post(String uri, String msg, Date date) throws URISyntaxException, SAXException, ParserConfigurationException, IOException, TransformerException {
        URI uriSrc = new URIBuilder()
                .setScheme("http")
                .setHost(mlHostName)
                .setPort(mlPort)
                .setPath(mlEndpoint)
                .build();
        StringBody strBody = new MLStringBody(msg, ContentType.create("application/xml", "UTF-8"), uri);
        HttpEntity reqEntity = MixedMultipartEntityBuilder.create()
                .addPart("name", strBody)
                .build();
        HttpPost postRequest = new HttpPost(uriSrc);
        postRequest.setEntity(reqEntity);
        CloseableHttpResponse response = httpSrc.execute(postRequest);
        try {
            EntityUtils.consume(response.getEntity());
        } finally {
            response.close();
        }
        return response.getStatusLine().getStatusCode();
    }

    public static void stringToFile(String type, String source, String fileName)
            throws SAXException, ParserConfigurationException, IOException, TransformerException
    {
        if (type.equals("json")) {
            // Parse the given input
            File file = new File(fileName);
            FileUtils.write(file, source, "UTF-8");
        } else {
            // Parse the given input
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(source)));

            // Write the parsed document to an xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            StreamResult result =  new StreamResult(file);
            transformer.transform(domSource, result);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        log.info("Starting application RestPoster");
        RestPoster client = new RestPoster();
        try {
            String test = "<sv:MSG xmlns:sv=\"http://www.testcom/DA/CF_Main\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><content xmlns=\"\">Dit is content</content></sv:MSG>";
            client.post("/content/100100100.xml", test, new Date());
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("***** Exiting application RestPoster *****");
    }

}
