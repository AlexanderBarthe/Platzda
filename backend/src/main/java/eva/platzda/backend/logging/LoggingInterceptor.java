package eva.platzda.backend.logging;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static final String START_TIME = "startTime";

    private final LogService logService;

    @Autowired
    public LoggingInterceptor(LogService logService) {
        this.logService = logService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long start = System.nanoTime()/1000;
        request.setAttribute(START_TIME, start);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long start = (Long) request.getAttribute(START_TIME);
        long duration = System.nanoTime()/1000 - start;
        int status = response.getStatus();

        LoggedEvent loggedEvent;

        if (ex != null) {
            logger.warn("Request {} {} failed after {} ms with exception: {}", request.getMethod(), request.getRequestURI(), (float)duration/1000, ex.getMessage(), ex);
            loggedEvent = new LoggedEvent(request.getRequestURI(), request.getMethod(), response.getStatus(), duration, ex.getMessage());
        } else {
            logger.info("Request {} {} completed with status {} in {} ms", request.getMethod(), request.getRequestURI(), status, (float)duration/1000);
            loggedEvent = new LoggedEvent(request.getRequestURI(), request.getMethod(), response.getStatus(), duration, "");
        }

        logService.addLoggedEvent(loggedEvent);
    }
}
