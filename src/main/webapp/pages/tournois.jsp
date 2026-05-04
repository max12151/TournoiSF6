<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="be.technifutur.tournoisf6.models.Tournoi" %>
<%
    List<Tournoi> tournois = (List<Tournoi>) request.getAttribute("tournois");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>SF6 Tournoi - Tournois</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #111827; color: #f3f4f6; }
        header { background: #1f2937; padding: 20px; }
        nav a { color: #93c5fd; margin-right: 18px; text-decoration: none; font-weight: bold; }
        main { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
        .card { background: #1f2937; border-radius: 12px; padding: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; border-bottom: 1px solid #374151; text-align: left; }
    </style>
</head>
<body>
<header>
    <h1>Liste des tournois</h1>
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
                <th>Nom</th>
                <th>Format</th>
                <th>Date</th>
                <th>Nombre de joueurs</th>
            </tr>
            </thead>
            <tbody>
            <% for (Tournoi tournoi : tournois) { %>
            <tr>
                <td><%= tournoi.getId() %></td>
                <td><%= tournoi.getNom() %></td>
                <td><%= tournoi.getFormat() %></td>
                <td><%= tournoi.getDate() %></td>
                <td><%= tournoi.getNombreJoueurs() %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </section>
</main>
</body>
</html>