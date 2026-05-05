package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.models.enums.RankEnum;
import be.technifutur.tournoisf6.models.enums.RoleEnum;
import be.technifutur.tournoisf6.service.JoueurService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/joueurs")
public class JoueurServlet extends HttpServlet {

    private final JoueurService joueurService = new JoueurService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("joueurs", joueurService.findAll());
        req.getRequestDispatcher("/pages/joueurs.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String pseudo = req.getParameter("pseudo");
        String email = req.getParameter("email");
        String motDePasse = req.getParameter("motDePasse");
        String dateNaissance = req.getParameter("dateNaissance");
        String genre = req.getParameter("genre");
        String rankParam = req.getParameter("rank");
        String personnagePrincipal = req.getParameter("personnagePrincipal");
        String pays = req.getParameter("pays");

        RankEnum rank = (rankParam == null || rankParam.isBlank())
                ? RankEnum.ROOKIE_I
                : RankEnum.valueOf(rankParam);

        if (joueurService.emailExiste(email)) {
            req.setAttribute("erreur", "Cet email existe déjà.");
            req.setAttribute("joueurs", joueurService.findAll());
            req.getRequestDispatcher("/pages/joueurs.jsp").forward(req, resp);
            return;
        }

        if (joueurService.pseudoExiste(pseudo)) {
            req.setAttribute("erreur", "Ce pseudo existe déjà.");
            req.setAttribute("joueurs", joueurService.findAll());
            req.getRequestDispatcher("/pages/joueurs.jsp").forward(req, resp);
            return;
        }

        Joueur joueur = new Joueur(
                pseudo,
                email,
                motDePasse,
                (dateNaissance == null || dateNaissance.isBlank()) ? null : LocalDate.parse(dateNaissance),
                genre,
                rank,
                RoleEnum.JOUEUR,
                personnagePrincipal,
                pays
        );

        joueurService.save(joueur);
        resp.sendRedirect(req.getContextPath() + "/joueurs");
    }
}