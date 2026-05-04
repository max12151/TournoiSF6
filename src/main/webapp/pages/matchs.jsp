<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="be.technifutur.tournoisf6.models.Match" %>
<%
    List<Match> matchs = (List<Match>) request.getAttribute("matchs");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>SF6 Tournoi - Matchs</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #0b1120; color: #e5e7eb; }
        header { background: #172033; padding: 20px; }
        nav a { color: #7dd3fc; margin-right: 18px; text-decoration: none; font-weight: bold; }
        main { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
        .card { background: #172033; border-radius: 12px; padding: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; border-bottom: 1px solid #2b3956; text-align: left; }
    </style>
</head>
<body>
<header>
    <h1>Matchs du tournoi</h1>
    <nav>
        <a href="${pageContext.request.contextPath}/">Accueil</a>
        <a href="${pageContext.request.contextPath}/joueurs">Joueurs</a>
        <a href="${pageContext.request.contextPath}/tournois">Tournois</a>
        <a href="${pageContext.request.contextPath}/matchs">Matchs</a>
    </nav>
</header>
<main>
    <section class="card">
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Round</th>
                <th>Joueur 1</th>
                <th>Joueur 2</th>
                <th>Score</th>
                <th>Vainqueur</th>
            </tr>
            </thead>
            <tbody>
            <% for (Match match : matchs) { %>
            <tr>
                <td><%= match.getId() %></td>
                <td><%= match.getRound() %></td>
                <td><%= match.getJoueur1() %></td>
                <td><%= match.getJoueur2() %></td>
                <td><%= match.getScore() %></td>
                <td><%= match.getVainqueur() %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </section>
</main>
</body>
</html>