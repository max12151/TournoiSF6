<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="be.technifutur.tournoisf6.models.Joueur" %>
<%@ page import="be.technifutur.tournoisf6.models.enums.RankEnum" %>
<%
    Joueur joueurConnecte = (Joueur) session.getAttribute("joueurConnecte");
    if (joueurConnecte == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>SF6 Tournoi - Mon profil</title>
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
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 12px;
        }
        header h1 { color: #f59e0b; font-size: 1.4rem; margin-bottom: 10px; }
        nav a {
            color: #7dd3fc;
            margin-right: 18px;
            text-decoration: none;
            font-weight: bold;
        }
        nav a:hover { text-decoration: underline; }
        .header-auth {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .user-badge {
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 8px;
            padding: 8px 14px;
            font-size: 14px;
            color: #94a3b8;
        }
        .user-badge strong { color: #f59e0b; }
        .btn-logout {
            background: #ef4444;
            color: white;
            border-radius: 8px;
            padding: 8px 16px;
            font-size: 14px;
            font-weight: bold;
            text-decoration: none;
        }
        .btn-logout:hover { background: #dc2626; }

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

        /* Infos non modifiables */
        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
            margin-bottom: 28px;
        }
        .info-item {
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 10px;
            padding: 12px 14px;
        }
        .info-label {
            font-size: 11px;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 4px;
        }
        .info-value {
            font-size: 15px;
            color: #e2e8f0;
            font-weight: bold;
        }

        /* Séparateur */
        .separator {
            border: none;
            border-top: 1px solid #334155;
            margin: 4px 0 24px;
        }
        .section-title {
            font-size: 13px;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 16px;
        }

        /* Formulaire */
        .form-group { margin-bottom: 18px; }
        label { display: block; margin-bottom: 6px; color: #94a3b8; font-size: 14px; }
        select {
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
        select:focus { border-color: #f59e0b; }
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
        .succes {
            background: #14532d;
            border: 1px solid #22c55e;
            color: #86efac;
            padding: 10px 14px;
            border-radius: 8px;
            margin-bottom: 18px;
            font-size: 14px;
        }
        .lien-bas {
            text-align: center;
            margin-top: 20px;
            color: #64748b;
            font-size: 14px;
        }
        .lien-bas a { color: #7dd3fc; text-decoration: none; }
        .lien-bas a:hover { text-decoration: underline; }
    </style>
</head>
<body>

<header>
    <div>
        <h1>Street Fighter 6 Tournament Manager</h1>
        <nav>
            <a href="${pageContext.request.contextPath}/">Accueil</a>
            <a href="${pageContext.request.contextPath}/joueurs">Joueurs</a>
            <a href="${pageContext.request.contextPath}/tournois">Tournois</a>
            <a href="${pageContext.request.contextPath}/matchs">Matchs</a>
        </nav>
    </div>
    <div class="header-auth">
        <div class="user-badge">
            👤 Connecté : <strong><%= joueurConnecte.getPseudo() %></strong>
        </div>
        <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Déconnexion</a>
    </div>
</header>

<div class="page-center">
    <div class="card">
        <h2>⚙️ Mon profil</h2>

        <%-- Messages --%>
        <% if (request.getAttribute("erreur") != null) { %>
        <div class="erreur">${erreur}</div>
        <% } %>
        <% if (request.getAttribute("succes") != null) { %>
        <div class="succes">✅ ${succes}</div>
        <% } %>

        <%-- Infos non modifiables --%>
        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Pseudo</div>
                <div class="info-value"><%= joueurConnecte.getPseudo() %></div>
            </div>
            <div class="info-item">
                <div class="info-label">Email</div>
                <div class="info-value" style="font-size:13px;word-break:break-all;">
                    <%= joueurConnecte.getEmail() %>
                </div>
            </div>
            <div class="info-item">
                <div class="info-label">Pays</div>
                <div class="info-value"><%= joueurConnecte.getPays() %></div>
            </div>
            <div class="info-item">
                <div class="info-label">Rôle</div>
                <div class="info-value" style="color:#7dd3fc;">
                    <%= joueurConnecte.getRole() %>
                </div>
            </div>
        </div>

        <hr class="separator">
        <p class="section-title">Modifier mon personnage &amp; rang</p>

        <%-- Formulaire de modification --%>
        <form method="post" action="${pageContext.request.contextPath}/profil">

            <div class="form-group">
                <label for="personnagePrincipal">Personnage principal</label>
                <select id="personnagePrincipal" name="personnagePrincipal">
                    <option value="Aucun" <%= "Aucun".equals(joueurConnecte.getPersonnagePrincipal()) ? "selected" : "" %>>
                        -- Aucun --
                    </option>
                    <%
                        String[] persos = {
                                "Ryu","Ken","Luke","Chun-Li","Guile","Zangief","Dhalsim",
                                "Blanka","E. Honda","Dee Jay","Cammy","Juri","Kimberly",
                                "Rashid","Marisa","Lily","Manon","JP","A.K.I.","Akuma",
                                "M. Bison","Ed","Terry","Mai","Elena"
                        };
                        for (String p : persos) {
                    %>
                    <option value="<%= p %>" <%= p.equals(joueurConnecte.getPersonnagePrincipal()) ? "selected" : "" %>>
                        <%= p %>
                    </option>
                    <% } %>
                </select>
            </div>

            <div class="form-group">
                <label for="rank">Rang actuel</label>
                <select id="rank" name="rank">
                    <% for (RankEnum r : RankEnum.values()) {
                        boolean selected = r == joueurConnecte.getRank();
                    %>
                    <option value="<%= r.name() %>" <%= selected ? "selected" : "" %>>
                        <%= r.name().replace("_", " ") %>
                    </option>
                    <% } %>
                </select>
            </div>

            <button type="submit" class="btn">💾 Sauvegarder les modifications</button>
        </form>

        <div class="lien-bas">
            <a href="${pageContext.request.contextPath}/">← Retour à l'accueil</a>
        </div>
    </div>
</div>
</body>
</html>