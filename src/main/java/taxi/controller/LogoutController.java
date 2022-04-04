package taxi.controller;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogoutController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(LogoutController.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Long driverId = (Long) req.getSession().getAttribute("driverId");
        req.getSession().invalidate();
        logger.info("Driver with id {} logged out.", driverId);
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
