<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="be.technifutur.tournoisf6.models.enums.RankEnum" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>SF6 Tournoi - Inscription</title>
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
            align-items: flex-start;
            justify-content: center;
            padding: 40px 20px;
        }
        .card {
            background: #1e293b;
            border-radius: 14px;
            padding: 36px;
            width: 100%;
            max-width: 520px;
            box-shadow: 0 8px 30px rgba(0,0,0,0.3);
        }
        .card h2 {
            color: #f59e0b;
            margin-bottom: 24px;
            font-size: 1.5rem;
            text-align: center;
        }
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 14px;
        }
        .form-group { margin-bottom: 16px; }
        label { display: block; margin-bottom: 6px; color: #94a3b8; font-size: 14px; }
        input[type="text"], input[type="email"], input[type="password"],
        input[type="date"], select {
            width: 100%;
            padding: 10px 13px;
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 8px;
            color: #e2e8f0;
            font-size: 14px;
            outline: none;
            transition: border-color 0.2s;
        }
        input:focus, select:focus { border-color: #f59e0b; }
        select option { background: #1e293b; }
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
        .hint { font-size: 12px; color: #64748b; margin-top: 4px; }
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
        <h2>⚔️ Créer un compte</h2>

        <% if (request.getAttribute("erreur") != null) { %>
        <div class="erreur">${erreur}</div>
        <% } %>

        <form method="post" action="${pageContext.request.contextPath}/register">

            <div class="form-row">
                <div class="form-group">
                    <label for="pseudo">Pseudo *</label>
                    <input type="text" id="pseudo" name="pseudo" placeholder="Ryu_SF6" required>
                </div>
                <div class="form-group">
                    <label for="email">Email *</label>
                    <input type="email" id="email" name="email" placeholder="ton@email.com" required>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="motDePasse">Mot de passe *</label>
                    <input type="password" id="motDePasse" name="motDePasse"
                           placeholder="Min. 6 caractères" required minlength="6">
                    <p class="hint">Minimum 6 caractères</p>
                </div>
                <div class="form-group">
                    <label for="confirmation">Confirmer *</label>
                    <input type="password" id="confirmation" name="confirmation"
                           placeholder="Répétez le mot de passe" required minlength="6">
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="dateNaissance">Date de naissance</label>
                    <input type="date" id="dateNaissance" name="dateNaissance">
                </div>
                <div class="form-group">
                    <label for="genre">Genre</label>
                    <select id="genre" name="genre">
                        <option value="">-- Choisir --</option>
                        <option>Homme</option>
                        <option>Femme</option>
                        <option>Autre</option>
                        <option>Non précisé</option>
                    </select>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="personnagePrincipal">Personnage principal *</label>
                    <select id="personnagePrincipal" name="personnagePrincipal" required>
                        <option value="">-- Choisir --</option>
                        <option>Ryu</option><option>Ken</option><option>Luke</option>
                        <option>Chun-Li</option><option>Guile</option><option>Zangief</option>
                        <option>Dhalsim</option><option>Blanka</option><option>E. Honda</option>
                        <option>Dee Jay</option><option>Cammy</option><option>Juri</option>
                        <option>Kimberly</option><option>Rashid</option><option>Marisa</option>
                        <option>Lily</option><option>Manon</option><option>JP</option>
                        <option>A.K.I.</option><option>Akuma</option><option>M. Bison</option>
                        <option>Ed</option><option>Terry</option><option>Mai</option>
                        <option>Elena</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="pays">Pays *</label>
                    <input type="text" id="pays" name="pays" placeholder="Belgique" required>
                </div>
            </div>

            <div class="form-group">
                <label for="rank">Rang SF6</label>
                <select id="rank" name="rank">
                    <% for (RankEnum r : RankEnum.values()) { %>
                    <option value="<%= r.name() %>"><%= r.name().replace("_", " ") %></option>
                    <% } %>
                </select>
            </div>

            <button type="submit" class="btn">Créer mon compte</button>
        </form>

        <div class="lien-bas">
            Déjà inscrit ? <a href="${pageContext.request.contextPath}/login">Se connecter</a>
        </div>
    </div>
</div>
</body>
</html>