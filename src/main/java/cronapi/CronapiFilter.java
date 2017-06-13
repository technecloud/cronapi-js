package cronapi;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import cronapi.i18n.Messages;

@Component
public class CronapiFilter implements Filter {
    public static ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();
    public static ThreadLocal<HttpServletResponse> RESPONSE = new ThreadLocal<>();

    @Override
    public void init(FilterConfig chain) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        REQUEST.set(request);
        RESPONSE.set(response);

        Messages.set(req.getLocale());
        try {
          chain.doFilter(req, resp);
        } finally {
          RestClient.removeClient();
          REQUEST.remove();
          RESPONSE.remove();
          Messages.remove();
        }
    }

    @Override
    public void destroy() {

    }
}
