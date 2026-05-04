package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.service.TournoiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/tournois")
public class TournoiServlet extends HttpServlet {
    private final TournoiService tournoiService = TournoiService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("tournois", tournoiService.getTournois());
        req.getRequestDispatcher("/pages/tournois.jsp").forward(req, resp);
    }
}