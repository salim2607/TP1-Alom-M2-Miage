package fr.lille.alom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class RequestAnalyzer {
    private FileRequestHandler fileHandler = new FileRequestHandler();
    private ServletRequestHandler servletHandler = new ServletRequestHandler();
    private BasicLogger l = new BasicLogger();

    public void handleRequest(Request r) throws IOException {
        r.in = r.s.getInputStream();
        String rq = new LineNumberReader(new InputStreamReader(r.in)).readLine();
        if (l != null)
            l.log(rq);
        
        if (rq == null) {
            throw new CanNotCompleteTheRequestException("Empty request");
        }
        
        String[] requestParts = rq.split(" ");
        if(requestParts.length < 3) {
            throw new CanNotCompleteTheRequestException("Invalid request: " + rq);
        }
        
        try {
            RequestType type = RequestType.valueOf(requestParts[0]);
            r.type = type;
            
            String url = requestParts[1];
            if(!url.startsWith("/")) {
                throw new CanNotCompleteTheRequestException("URL must start with /: " + url);
            }
            
            r.url = url.substring(1); // Remove the leading slash
            
            // Check if it's a servlet request (starts with servlet name)
            String[] urlParts = r.url.split("/");
            if (urlParts.length > 0 && ServletFactory.getInstance().getServletByName(urlParts[0]) != null) {
                servletHandler.handleRequest(r);
            } else {
                fileHandler.handleRequest(r);
            }
            
        } catch (IllegalArgumentException e) {
            throw new CanNotCompleteTheRequestException("Invalid request type: " + requestParts[0]);
        }
    }
}