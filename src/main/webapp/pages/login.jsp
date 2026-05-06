<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>SF6 Tournoi - Connexion</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: Arial, sans-serif;
            background: #0f172a;
            color: #e2e8f0;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        header {
            background: #111827;
            padding: 20px 24px;
            border-bottom: 1px solid #1f2937;
        }
        header h1 { color: #f59e0b; font-size: 1.4rem; margin-bottom: 10px; }
        nav a {
            color: #7dd3fc;
            margin-right: 18px;
            text-decoration: none;
            font-weight: bold;
        }
        nav a:hover { text-decoration: underline; }
        .page-center {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }
        .card {
            background: #1e293b;
            border-radius: 14px;
            padding: 36px;
            width: 100%;
            max-width: 440px;
            box-shadow: 0 8px 30px rgba(0,0,0,0.3);
        }
        .card h2 {
            color: #f59e0b;
            margin-bottom: 24px;
            font-size: 1.5rem;
            text-align: center;
        }
        .form-group { margin-bottom: 18px; }
        label { display: block; margin-bottom: 6px; color: #94a3b8; font-size: 14px; }
        input[type="email"], input[type="password"] {
            width: 100%;
            padding: 11px 14px;
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 8px;
            color: #e2e8f0;
            font-size: 15px;
            outline: none;
            transition: border-color 0.2s;
        }
        input:focus { border-color: #f59e0b; }
        .btn {
            width: 100%;
            padding: 12px;
            background: #f59e0b;
            color: #0f172a;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            margin-top: 8px;
            transition: background 0.2s;
        }
        .btn:hover { background: #fbbf24; }
        .erreur {
            background: #7f1d1d;
            border: 1px solid #ef4444;
            color: #fca5a5;
            padding: 10px 14px;
            border-radius: 8px;
            margin-bottom: 18px;
            font-size: 14px;
        }
        .lien-bas { text-align: center; margin-top: 20px; color: #64748b; font-size: 14px; }
        .lien-bas a { color: #7dd3fc; text-decoration: none; }
        .lien-bas a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<header>
    <h1>Street Fighter 6 Tournament Manager</h1>
    <nav>
        <a href="${pageContext.request.contextPath}/">Accueil</a>
        <a href="${pageContext.request.contextPath}/joueurs">Joueurs</a>
        <a href="${pageContext.request.contextPath}/tournois">Tournois</a>
        <a href="${pageContext.request.contextPath}/matchs">Matchs</a>
    </nav>
</header>

<div class="page-center">
    <div class="card">
        <h2>🎮 Connexion</h2>

        <% if (request.getAttribute("erreur") != null) { %>
        <div class="erreur">${erreur}</div>
        <% } %>

        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email"
                       value="${emailSaisi}" placeholder="ton@email.com" required>
            </div>
            <div class="form-group">
                <label for="motDePasse">Mot de passe</label>
                <input type="password" id="motDePasse" name="motDePasse"
                       placeholder="••••••••" required>
            </div>
            <button type="submit" class="btn">Se connecter</button>
        </form>

        <div class="lien-bas">
            Pas encore de compte ?
            <a href="${pageContext.request.contextPath}/register">S'inscrire</a>
        </div>
    </div>
</div>
</body>
</html>