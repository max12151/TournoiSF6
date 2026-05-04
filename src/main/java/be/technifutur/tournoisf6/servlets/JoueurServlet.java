package be.technifutur.tournoisf6.servlets;

import be.technifutur.tournoisf6.service.TournoiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/joueurs")
public class JoueurServlet extends HttpServlet {
    private final TournoiService tournoiService = TournoiService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("joueurs", tournoiService.getJoueurs());
        req.getRequestDispatcher("/pages/joueurs.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String pseudo = req.getParameter("pseudo");
        String personnage = req.getParameter("personnage");
        String pays = req.getParameter("pays");

        if (pseudo != null && !pseudo.isBlank()
                && personnage != null && !personnage.isBlank()
                && pays != null && !pays.isBlank()) {
            tournoiService.ajouterJoueur(pseudo.trim(), personnage.trim(), pays.trim());
        }

        resp.sendRedirect(req.getContextPath() + "/joueurs");
    }
}