package log;

import log.model.RequestLog;
import log.service.RequestLogService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author James Lin
 */
public class RequestLogFilter extends HttpFilter {

    @Autowired
    private RequestLogService requestLogService;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(request.getMethod().equals("GET")) {
            super.doFilter(request, response, chain);
        }
        else {
            RequestLog requestLog = new RequestLog();
            requestLog.setApi(request.getMethod() + " " + request.getRequestURI());
            requestLog.setReqTime(new Date());
            requestLog.setIp(request.getRemoteAddr());

            if(request.getContentType() != null && request.getContentType().equals("application/json")) {
                BodyCachingHttpServletRequestWrapper requestWrapper = new BodyCachingHttpServletRequestWrapper(request);
                requestLog.setReqBody(new String(requestWrapper.getBody(), Charset.forName("UTF-8")));
            }

            BodyCachingHttpServletResponseWrapper responseWrapper = new BodyCachingHttpServletResponseWrapper(response);

            super.doFilter(request, response, chain);

            if(request.getAttribute("identity") != null)
                requestLog.setIdentity((String)request.getAttribute("identity"));
            requestLog.setRspTime(new Date());
            requestLog.setRspStatus(String.valueOf(response.getStatus()));

            if(response.getContentType() != null && (response.getContentType().equals("application/json") || response.getContentType().equals("text/plain"))) {
                requestLog.setRspBody(new String(responseWrapper.getBody(), Charset.forName("UTF-8")));
            }

            requestLogService.create(requestLog);
        }
    }
}
