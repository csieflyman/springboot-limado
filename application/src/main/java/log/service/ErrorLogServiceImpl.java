package log.service;

import base.dto.response.BatchResponse;
import base.exception.BadRequestException;
import base.util.Json;
import base.util.Subject;
import base.util.ThreadLocalUtils;
import base.util.db.EbeanTransactional;
import com.google.common.collect.Sets;
import log.dao.ErrorLogDao;
import log.model.ErrorLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import play.libs.Json;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

/**
 * @author csieflyman
 */
@Slf4j
@Service("errorLogService")
public class ErrorLogServiceImpl implements ErrorLogService {

    @Autowired
    private ErrorLogDao errorLogDao;

    @Transactional
    @Override
    public void create(HttpServletRequest request, Throwable e) {
        ErrorLog errorLog = new ErrorLog(getSubject(), e);
        errorLog.setApi(request.getMethod() + " " + request.getRequestURI());
        if(e instanceof BadRequestException) {
            errorLog.setBody(((BadRequestException) e).getBody());
        }
        if(errorLog.getBody() == null) {
            if(hasBodyMethod(request) && request.getContentType() != null && request.getContentType().equals("application/json")) {
                try {
                    errorLog.setBody(IOUtils.toString(request.getReader()));
                } catch (IOException e1) {
                    errorLog.setBody("Unknown");
                }
            }
        }
        create(errorLog);
    }

    private static final Set<String> hasBodyMethods = Sets.newHashSet("POST", "PUT", "PATCH");

    private boolean hasBodyMethod(HttpServletRequest request) {
        return hasBodyMethods.stream().anyMatch(m -> request.getMethod().equalsIgnoreCase(m));
    }

    @Override
    public void create(BatchResponse batchResponse, String api, String body) {
        ErrorLog errorLog = new ErrorLog(getSubject());
        errorLog.setApi(api);
        errorLog.setErrorMsg(Json.toJsonString(batchResponse));
        errorLog.setBody(body);
        create(errorLog);
    }

    @Override
    public void create(String api, Throwable e) {
        create(api, null, e);
    }

    @Override
    public void create(String api, String body, Throwable e) {
        ErrorLog errorLog = new ErrorLog(getSubject(), e);
        errorLog.api = api;
        errorLog.body = body;
        create(errorLog);
    }

    private void create(ErrorLog errorLog) {
        saveToDB(errorLog);
    }

    private Subject getSubject() {
        Subject subject = null;
        try {
            subject = ThreadLocalUtils.getSubject();
        } catch (Throwable ee) {
            // 還沒成功登入驗證時，會抓不到當前的 subject
        }
        return subject;
    }

    @EbeanTransactional
    public void saveToDB(ErrorLog errorLog) {
        try {
            errorLogDao.create(errorLog);
        } catch (Throwable ee) {
            logger.error("fail to save ErrorLog", ee);
        }
    }
}
