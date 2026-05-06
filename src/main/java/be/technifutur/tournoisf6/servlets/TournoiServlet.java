package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.models.MatchTournoi;
import be.technifutur.tournoisf6.models.Tournoi;
import be.technifutur.tournoisf6.models.enums.BracketTypeEnum;
import be.technifutur.tournoisf6.models.enums.EtatTournoiEnum;
import be.technifutur.tournoisf6.models.enums.FormatTournoiEnum;
import be.technifutur.tournoisf6.models.enums.RankEnum;
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
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@WebServlet("/tournois")
public class TournoiServlet extends HttpServlet {

    private final TournoiService tournoiService = new TournoiService();
    private final JoueurService joueurService = new JoueurService();
    private final InscriptionTournoiService inscriptionTournoiService = new InscriptionTournoiService();
    private final MatchTournoiService matchTournoiService = new MatchTournoiService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");

        if (idParam != null && !idParam.isBlank()) {
            try {
                Long id = Long.parseLong(idParam);
                Tournoi tournoi = tournoiService.findById(id);

                if (tournoi == null) {
                    req.setAttribute("erreur", "Tournoi introuvable.");
                    req.setAttribute("tournois", tournoiService.findAll());
                    req.setAttribute("ranks", RankEnum.values());
                    req.getRequestDispatcher("/pages/tournois.jsp").forward(req, resp);
                    return;
                }

                List<MatchTournoi> matchs = matchTournoiService.findByTournoi(id);

                // Recherche du champion : gagnant du dernier match GRAND_FINAL terminé
                // (soit GF R2 si bracket reset joué, soit GF R1 si le joueur WB a gagné directement)
                matchs.stream()
                        .filter(m -> m.getBracketType() == BracketTypeEnum.GRAND_FINAL
                                && Boolean.TRUE.equals(m.getTermine())
                                && m.getGagnant() != null)
                        .max(Comparator.comparingInt(MatchTournoi::getRoundNumber))
                        .map(MatchTournoi::getGagnant)
                        .ifPresent(c -> req.setAttribute("champion", c));

                req.setAttribute("tournoi", tournoi);
                req.setAttribute("joueurs", joueurService.findAll());
                req.setAttribute("inscriptions", inscriptionTournoiService.findByTournoi(id));
                req.setAttribute("matchs", matchs);

                req.getRequestDispatcher("/pages/tournoi-detail.jsp").forward(req, resp);
                return;

            } catch (Exception e) {
                req.setAttribute("erreur", e.getMessage());
                req.setAttribute("tournois", tournoiService.findAll());
                req.setAttribute("ranks", RankEnum.values());
                req.getRequestDispatcher("/pages/tournois.jsp").forward(req, resp);
                return;
            }
        }

        req.setAttribute("tournois", tournoiService.findAll());
        req.setAttribute("ranks", RankEnum.values());
        req.getRequestDispatcher("/pages/tournois.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                String nom = req.getParameter("nom");
                LocalDate dateDebut = LocalDate.parse(req.getParameter("dateDebut"));
                LocalDate dateFin = LocalDate.parse(req.getParameter("dateFin"));
                Integer nombreJoueursMax = Integer.parseInt(req.getParameter("nombreJoueursMax"));
                RankEnum rankMaxAutorise = RankEnum.valueOf(req.getParameter("rankMaxAutorise"));

                Tournoi tournoi = new Tournoi(
                        nom,
                        dateDebut,
                        dateFin,
                        nombreJoueursMax,
                        FormatTournoiEnum.DOUBLE_ELIMINATION,
                        EtatTournoiEnum.EN_ATTENTE,
                        rankMaxAutorise
                );

                tournoiService.save(tournoi);
                resp.sendRedirect(req.getContextPath() + "/tournois");
                return;
            }

            if ("lancer".equals(action)) {
                Long tournoiId = Long.parseLong(req.getParameter("tournoiId"));
                matchTournoiService.lancerTournoi(tournoiId);
                resp.sendRedirect(req.getContextPath() + "/tournois?id=" + tournoiId);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/tournois");

        } catch (Exception e) {
            req.setAttribute("erreur", e.getMessage());
            req.setAttribute("tournois", tournoiService.findAll());
            req.setAttribute("ranks", RankEnum.values());
            req.getRequestDispatcher("/pages/tournois.jsp").forward(req, resp);
        }
    }
}