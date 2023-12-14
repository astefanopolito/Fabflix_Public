import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> allowedEmployeeURIs = new ArrayList<>();
    private final ArrayList<String> dashURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        request.getServletContext().log("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        if (this.isDash(httpRequest.getRequestURI())) {
            if (httpRequest.getSession().getAttribute("employee") == null) {
                httpResponse.sendRedirect("employee_login.html");
                return;
            }
            else {
                httpResponse.sendRedirect("dashboard.html");
                return;
            }
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            httpResponse.sendRedirect("login.html");
        } else {
            if (!this.isUrlAllowedWithoutEmployeeLogin(httpRequest.getRequestURI())) {
                chain.doFilter(request, response);
            }
            else {
                if (httpRequest.getSession().getAttribute("employee") == null) {
                    httpResponse.sendRedirect("employee_login.html");
                }
                else {
                    chain.doFilter(request, response);
                }
            }
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }
    private boolean isUrlAllowedWithoutEmployeeLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedEmployeeURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }
    private boolean isDash(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return dashURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("movies_index.html");
        allowedURIs.add("movies_index.js");
        allowedURIs.add("api/movies");
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("employee_login.html");
        allowedURIs.add("employee_login.js");
        allowedURIs.add("api/employeelogin");
        allowedURIs.add("navbar.html");
        allowedURIs.add("navbar.js");
        allowedURIs.add("navbar_login.js");
        allowedURIs.add("api/session");
        allowedEmployeeURIs.add("api/dashboard");
        allowedEmployeeURIs.add("dashboard.html");
        allowedEmployeeURIs.add("dashboard.js");
        allowedEmployeeURIs.add("api/dashdata");
        dashURIs.add("_dashboard");

    }

    public void destroy() {
        // ignored.
    }

}