package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.service.MatchTournoiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/matchs")
public class MatchServlet extends HttpServlet {

    private final MatchTournoiService matchTournoiService = new MatchTournoiService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tournoiIdParam = req.getParameter("tournoiId");

        if (tournoiIdParam != null && !tournoiIdParam.isBlank()) {
            Long tournoiId = Long.parseLong(tournoiIdParam);
            req.setAttribute("matchs", matchTournoiService.findByTournoi(tournoiId));
        }

        req.getRequestDispatcher("/pages/matchs.jsp").forward(req, resp);
    }
}