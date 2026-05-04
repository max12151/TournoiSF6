<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="be.technifutur.tournoisf6.models.Joueur" %>
<%
    List<Joueur> joueurs = (List<Joueur>) request.getAttribute("joueurs");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>SF6 Tournoi - Joueurs</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #0f172a; color: #e2e8f0; }
        header { background: #1e293b; padding: 20px; }
        nav a { color: #7dd3fc; margin-right: 18px; text-decoration: none; font-weight: bold; }
        main { max-width: 1100px; margin: 30px auto; padding: 0 20px; }
        .grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
        .card { background: #1e293b; border-radius: 12px; padding: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; border-bottom: 1px solid #334155; text-align: left; }
        input { width: 100%; padding: 10px; margin-bottom: 12px; border-radius: 8px; border: 1px solid #475569; background: #0f172a; color: white; }
        button { background: #f59e0b; color: #111827; border: none; padding: 12px 16px; border-radius: 8px; font-weight: bold; cursor: pointer; }
    </style>
</head>
<body>
<header>
    <h1>Gestion des joueurs</h1>
    <nav>
        <a href="${pageContext.request.contextPath}/">Accueil</a>
        <a href="${pageContext.request.contextPath}/joueurs">Joueurs</a>
        <a href="${pageContext.request.contextPath}/tournois">Tournois</a>
        <a href="${pageContext.request.contextPath}/matchs">Matchs</a>
    </nav>
</header>
<main>
    <div class="grid">
        <section class="card">
            <h2>Liste des joueurs</h2>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Pseudo</th>
                    <th>Main</th>
                    <th>Pays</th>
                </tr>
                </thead>
                <tbody>
                <% for (Joueur joueur : joueurs) { %>
                <tr>
                    <td><%= joueur.getId() %></td>
                    <td><%= joueur.getPseudo() %></td>
                    <td><%= joueur.getPersonnagePrincipal() %></td>
                    <td><%= joueur.getPays() %></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </section>

        <section class="card">
            <h2>Ajouter un joueur</h2>
            <form method="post" action="${pageContext.request.contextPath}/joueurs">
                <label for="pseudo">Pseudo</label>
                <input id="pseudo" name="pseudo" required>

                <label for="personnage">Personnage principal</label>
                <input id="personnage" name="personnage" required>

                <label for="pays">Pays</label>
                <input id="pays" name="pays" required>

                <button type="submit">Ajouter</button>
            </form>
        </section>
    </div>
</main>
</body>
</html>