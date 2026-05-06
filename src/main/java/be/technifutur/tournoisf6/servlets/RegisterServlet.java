package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.models.enums.RankEnum;
import be.technifutur.tournoisf6.models.enums.RoleEnum;
import be.technifutur.tournoisf6.service.JoueurService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final JoueurService joueurService = new JoueurService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("joueurConnecte") != null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        req.getRequestDispatcher("/pages/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String pseudo       = req.getParameter("pseudo");
        String email        = req.getParameter("email");
        String motDePasse   = req.getParameter("motDePasse");
        String confirmation = req.getParameter("confirmation");
        String dateStr      = req.getParameter("dateNaissance");
        String genre        = req.getParameter("genre");
        String rankParam    = req.getParameter("rank");
        String personnage   = req.getParameter("personnagePrincipal");
        String pays         = req.getParameter("pays");

        // Validations
        if (pseudo == null || pseudo.isBlank()) {
            req.setAttribute("erreur", "Le pseudo est obligatoire.");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp); return;
        }
        if (email == null || email.isBlank()) {
            req.setAttribute("erreur", "L'email est obligatoire.");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp); return;
        }
        if (motDePasse == null || motDePasse.length() < 6) {
            req.setAttribute("erreur", "Le mot de passe doit faire au moins 6 caractères.");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp); return;
        }
        if (!motDePasse.equals(confirmation)) {
            req.setAttribute("erreur", "Les mots de passe ne correspondent pas.");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp); return;
        }
        if (joueurService.emailExiste(email.trim())) {
            req.setAttribute("erreur", "Cet email est déjà utilisé.");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp); return;
        }
        if (joueurService.pseudoExiste(pseudo.trim())) {
            req.setAttribute("erreur", "Ce pseudo est déjà pris.");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp); return;
        }
        if (personnage == null || personnage.isBlank()) {
            req.setAttribute("erreur", "Le personnage principal est obligatoire.");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp); return;
        }
        if (pays == null || pays.isBlank()) {
            req.setAttribute("erreur", "Le pays est obligatoire.");
            req.getRequestDispatcher("/pages/register.jsp").forward(req, resp); return;
        }

        RankEnum rank = (rankParam == null || rankParam.isBlank())
                ? RankEnum.ROOKIE_I : RankEnum.valueOf(rankParam);

        LocalDate dateNaissance = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr) : null;

        // Le hashage est fait automatiquement dans JoueurService.save()
        Joueur joueur = new Joueur(
                pseudo.trim(), email.trim(), motDePasse,
                dateNaissance, genre, rank, RoleEnum.JOUEUR,
                personnage.trim(), pays.trim()
        );

        joueurService.save(joueur);

        // Connexion automatique après inscription
        HttpSession session = req.getSession(true);
        session.setAttribute("joueurConnecte", joueurService.findByEmail(email.trim()));
        session.setMaxInactiveInterval(3600);

        resp.sendRedirect(req.getContextPath() + "/");
    }
}