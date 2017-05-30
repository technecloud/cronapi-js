package cronapi;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import cronapi.rest.CallBlocklyREST;
import org.springframework.stereotype.Component;

import cronapi.i18n.Messages;

@Component
public class CronapiFilter implements Filter {
    @Override
    public void init(FilterConfig chain) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        Messages.set(req.getLocale());
        try {
          chain.doFilter(req, resp);
        } finally {
          CallBlocklyREST.CLIENT_COMMANDS.remove();
        }
    }

    @Override
    public void destroy() {

    }
}
