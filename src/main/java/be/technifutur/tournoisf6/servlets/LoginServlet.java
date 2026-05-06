package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.models.Joueur;
import be.technifutur.tournoisf6.service.JoueurService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final JoueurService joueurService = new JoueurService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Si déjà connecté → accueil
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("joueurConnecte") != null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        req.getRequestDispatcher("/pages/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String email      = req.getParameter("email");
        String motDePasse = req.getParameter("motDePasse");

        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            req.setAttribute("erreur", "Veuillez remplir tous les champs.");
            req.getRequestDispatcher("/pages/login.jsp").forward(req, resp);
            return;
        }

        Joueur joueur = joueurService.authentifier(email.trim(), motDePasse);

        if (joueur == null) {
            req.setAttribute("erreur", "Email ou mot de passe incorrect.");
            req.setAttribute("emailSaisi", email);
            req.getRequestDispatcher("/pages/login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("joueurConnecte", joueur);
        session.setMaxInactiveInterval(3600); // 1 heure

        resp.sendRedirect(req.getContextPath() + "/");
    }
}