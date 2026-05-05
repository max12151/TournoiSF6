package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.service.InscriptionTournoiService;
import be.technifutur.tournoisf6.service.JoueurService;
import be.technifutur.tournoisf6.service.MatchTournoiService;
import be.technifutur.tournoisf6.service.TournoiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/inscriptions-tournoi")
public class InscriptionTournoiServlet extends HttpServlet {

    private final InscriptionTournoiService inscriptionTournoiService = new InscriptionTournoiService();
    private final TournoiService tournoiService = new TournoiService();
    private final JoueurService joueurService = new JoueurService();
    private final MatchTournoiService matchTournoiService = new MatchTournoiService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long tournoiId = Long.parseLong(req.getParameter("tournoiId"));
        Long joueurId = Long.parseLong(req.getParameter("joueurId"));

        try {
            inscriptionTournoiService.inscrire(tournoiId, joueurId);
            resp.sendRedirect(req.getContextPath() + "/tournois?id=" + tournoiId);
        } catch (Exception e) {
            req.setAttribute("erreur", e.getMessage());
            req.setAttribute("tournoi", tournoiService.findById(tournoiId));
            req.setAttribute("joueurs", joueurService.findAll());
            req.setAttribute("inscriptions", inscriptionTournoiService.findByTournoi(tournoiId));
            req.setAttribute("matchs", matchTournoiService.findByTournoi(tournoiId));
            req.getRequestDispatcher("/pages/tournoi-detail.jsp").forward(req, resp);
        }
    }
}