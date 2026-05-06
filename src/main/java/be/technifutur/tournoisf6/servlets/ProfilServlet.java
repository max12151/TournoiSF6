package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.models.enums.RankEnum;
import be.technifutur.tournoisf6.service.JoueurService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/profil")
public class ProfilServlet extends HttpServlet {

    private final JoueurService joueurService = new JoueurService();

    /** Affiche la page de profil — réservé aux connectés */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("joueurConnecte") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/pages/profil.jsp").forward(req, resp);
    }

    /** Traite la modification du profil */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("joueurConnecte") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Joueur joueurConnecte = (Joueur) session.getAttribute("joueurConnecte");

        String personnage = req.getParameter("personnagePrincipal");
        String rankParam  = req.getParameter("rank");

        // Validation
        if (personnage == null || personnage.isBlank()) {
            req.setAttribute("erreur", "Le personnage principal ne peut pas être vide.");
            req.getRequestDispatcher("/pages/profil.jsp").forward(req, resp);
            return;
        }
        if (rankParam == null || rankParam.isBlank()) {
            req.setAttribute("erreur", "Veuillez choisir un rang.");
            req.getRequestDispatcher("/pages/profil.jsp").forward(req, resp);
            return;
        }

        RankEnum rank;
        try {
            rank = RankEnum.valueOf(rankParam);
        } catch (IllegalArgumentException e) {
            req.setAttribute("erreur", "Rang invalide.");
            req.getRequestDispatcher("/pages/profil.jsp").forward(req, resp);
            return;
        }

        joueurService.updateProfil(joueurConnecte.getId(), personnage.trim(), rank);

        // Mise à jour de la session avec les nouvelles valeurs
        Joueur joueurMisAJour = joueurService.findById(joueurConnecte.getId());
        session.setAttribute("joueurConnecte", joueurMisAJour);

        req.setAttribute("succes", "Profil mis à jour avec succès !");
        req.getRequestDispatcher("/pages/profil.jsp").forward(req, resp);
    }
}