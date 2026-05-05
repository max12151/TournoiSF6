package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.service.MatchTournoiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/matchs")
public class AdminMatchServlet extends HttpServlet {

    private final MatchTournoiService matchTournoiService = new MatchTournoiService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Long matchId = Long.parseLong(req.getParameter("matchId"));
            Long tournoiId = Long.parseLong(req.getParameter("tournoiId"));
            int scoreJoueur1 = Integer.parseInt(req.getParameter("scoreJoueur1"));
            int scoreJoueur2 = Integer.parseInt(req.getParameter("scoreJoueur2"));

            matchTournoiService.encoderResultat(matchId, scoreJoueur1, scoreJoueur2);

            resp.sendRedirect(req.getContextPath() + "/tournois?id=" + tournoiId);
        } catch (Exception e) {
            Long tournoiId = Long.parseLong(req.getParameter("tournoiId"));
            resp.sendRedirect(req.getContextPath() + "/tournois?id=" + tournoiId + "&erreur=" + e.getMessage());
        }
    }
}