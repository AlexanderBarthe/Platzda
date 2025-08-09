package eva.platzda.backend.logging;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long start = System.currentTimeMillis();
        request.setAttribute(START_TIME, start);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long start = (Long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - start;
        int status = response.getStatus();

        if (ex != null) {
            logger.warn("Request {} {} failed after {} ms with exception: {}", request.getMethod(), request.getRequestURI(), duration, ex.getMessage(), ex);
        } else {
            logger.info("Request {} {} completed with status {} in {} ms", request.getMethod(), request.getRequestURI(), status, duration);
        }
    }
}
