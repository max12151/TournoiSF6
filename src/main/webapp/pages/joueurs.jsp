<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="be.technifutur.tournoisf6.models.Joueur" %>
<%@ page import="be.technifutur.tournoisf6.models.enums.RankEnum" %>
<%
    List<Joueur> joueurs = (List<Joueur>) request.getAttribute("joueurs");
    if (joueurs == null) {
        joueurs = new ArrayList<>();
    } else {
        joueurs = new ArrayList<>(joueurs);
        Collections.sort(joueurs, Comparator
                .comparingInt((Joueur j) -> {
                    RankEnum rank = j.getRank();
                    if (rank == null) return Integer.MAX_VALUE;
                    switch (rank) {
                        case MASTER:      return 0;
                        case DIAMOND_V:   return 1;  case DIAMOND_IV:   return 2;
                        case DIAMOND_III: return 3;  case DIAMOND_II:   return 4;
                        case DIAMOND_I:   return 5;
                        case PLATINUM_V:  return 6;  case PLATINUM_IV:  return 7;
                        case PLATINUM_III:return 8;  case PLATINUM_II:  return 9;
                        case PLATINUM_I:  return 10;
                        case GOLD_V:      return 11; case GOLD_IV:      return 12;
                        case GOLD_III:    return 13; case GOLD_II:      return 14;
                        case GOLD_I:      return 15;
                        case SILVER_V:    return 16; case SILVER_IV:    return 17;
                        case SILVER_III:  return 18; case SILVER_II:    return 19;
                        case SILVER_I:    return 20;
                        case BRONZE_V:    return 21; case BRONZE_IV:    return 22;
                        case BRONZE_III:  return 23; case BRONZE_II:    return 24;
                        case BRONZE_I:    return 25;
                        case IRON_V:      return 26; case IRON_IV:      return 27;
                        case IRON_III:    return 28; case IRON_II:      return 29;
                        case IRON_I:      return 30;
                        case ROOKIE_V:    return 31; case ROOKIE_IV:    return 32;
                        case ROOKIE_III:  return 33; case ROOKIE_II:    return 34;
                        case ROOKIE_I:    return 35;
                        default: return Integer.MAX_VALUE;
                    }
                })
                .thenComparing(
                        j -> j.getPseudo() == null ? "" : j.getPseudo(),
                        String.CASE_INSENSITIVE_ORDER
                )
        );
    }

    Joueur joueurConnecte = (Joueur) session.getAttribute("joueurConnecte");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SF6 Tournoi - Joueurs</title>
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

        /* ---- HEADER ---- */
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
        nav a.active { color: #f59e0b; }

        .header-auth {
            display: flex;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
            padding-top: 4px;
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
            transition: background 0.2s;
        }
        .btn-logout:hover { background: #dc2626; }

        .btn-login {
            background: #f59e0b;
            color: #0f172a;
            border-radius: 8px;
            padding: 8px 16px;
            font-size: 14px;
            font-weight: bold;
            text-decoration: none;
            transition: background 0.2s;
        }
        .btn-login:hover { background: #fbbf24; }

        .btn-register {
            color: #7dd3fc;
            border: 1px solid #334155;
            border-radius: 8px;
            padding: 8px 16px;
            font-size: 14px;
            font-weight: bold;
            text-decoration: none;
            transition: background 0.2s;
        }
        .btn-register:hover { background: #1e293b; }

        .btn-profil {
            color: #a78bfa;
            border: 1px solid #334155;
            border-radius: 8px;
            padding: 8px 16px;
            font-size: 14px;
            font-weight: bold;
            text-decoration: none;
            transition: background 0.2s;
        }
        .btn-profil:hover { background: #1e293b; }

        /* ---- CONTENU ---- */
        main {
            max-width: 1150px;
            margin: 30px auto;
            padding: 0 20px 40px;
            width: 100%;
        }

        .page-title {
            color: #fbbf24;
            font-size: 1.6rem;
            margin-bottom: 20px;
        }

        .card {
            background: #1e293b;
            border-radius: 14px;
            padding: 22px;
            margin-bottom: 22px;
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.20);
        }

        /* Compteur */
        .stats-bar {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 18px;
        }
        .badge-count {
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 999px;
            padding: 5px 14px;
            font-size: 14px;
            color: #94a3b8;
        }
        .badge-count strong { color: #f59e0b; }

        /* Tableau */
        .table-wrapper { overflow-x: auto; }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead { background: #0f172a; }

        th {
            padding: 14px 16px;
            text-align: left;
            font-size: 12px;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            border-bottom: 1px solid #334155;
        }

        td {
            padding: 14px 16px;
            color: #cbd5e1;
            border-bottom: 1px solid #1e293b;
            font-size: 14px;
        }

        tbody tr:hover { background: rgba(255,255,255,0.03); }

        /* Rang coloré selon le tier */
        .rank-master   { color: #e879f9; font-weight: 800; }
        .rank-diamond  { color: #60a5fa; font-weight: 700; }
        .rank-platinum { color: #a78bfa; font-weight: 700; }
        .rank-gold     { color: #fbbf24; font-weight: 700; }
        .rank-silver   { color: #94a3b8; font-weight: 700; }
        .rank-bronze   { color: #d97706; font-weight: 700; }
        .rank-iron     { color: #6b7280; font-weight: 700; }
        .rank-rookie   { color: #64748b; font-weight: 600; }

        .pseudo-cell { color: #f8fafc; font-weight: bold; }

        .empty-state {
            text-align: center;
            padding: 48px 20px;
            color: #475569;
        }
        .empty-state p { font-size: 15px; margin-top: 8px; }

        .tag-perso {
            display: inline-block;
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 999px;
            padding: 3px 10px;
            font-size: 13px;
            color: #7dd3fc;
        }

        .tag-aucun {
            color: #475569;
            font-style: italic;
        }
    </style>
</head>
<body>

<header>
    <div>
        <h1>Street Fighter 6 Tournament Manager</h1>
        <nav>
            <a href="${pageContext.request.contextPath}/">Accueil</a>
            <a href="${pageContext.request.contextPath}/joueurs" class="active">Joueurs</a>
            <a href="${pageContext.request.contextPath}/tournois">Tournois</a>
            <a href="${pageContext.request.contextPath}/matchs">Matchs</a>
        </nav>
    </div>
    <div class="header-auth">
        <% if (joueurConnecte != null) { %>
        <div class="user-badge">
            👤 Connecté : <strong><%= joueurConnecte.getPseudo() %></strong>
        </div>
        <a href="${pageContext.request.contextPath}/profil" class="btn-profil">⚙️ Mon profil</a>
        <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Déconnexion</a>
        <% } else { %>
        <a href="${pageContext.request.contextPath}/login" class="btn-login">Connexion</a>
        <a href="${pageContext.request.contextPath}/register" class="btn-register">S'inscrire</a>
        <% } %>
    </div>
</header>

<main>
    <h2 class="page-title">⚔️ Classement des joueurs</h2>

    <div class="card">
        <div class="stats-bar">
            <div class="badge-count">
                Total : <strong><%= joueurs.size() %></strong> joueur<%= joueurs.size() > 1 ? "s" : "" %>
            </div>
        </div>

        <div class="table-wrapper">
            <table>
                <thead>
                <tr>
                    <th>#</th>
                    <th>Pseudo</th>
                    <th>Main</th>
                    <th>Pays</th>
                    <th>Genre</th>
                    <th>Rang</th>
                </tr>
                </thead>
                <tbody>
                <% if (joueurs.isEmpty()) { %>
                <tr>
                    <td colspan="6">
                        <div class="empty-state">
                            🎮
                            <p>Aucun joueur inscrit pour le moment.</p>
                        </div>
                    </td>
                </tr>
                <% } else { %>
                <% for (int i = 0; i < joueurs.size(); i++) {
                    Joueur j = joueurs.get(i);
                    String rankName = j.getRank() != null ? j.getRank().name() : "";
                    String rankClass = "rank-rookie";
                    if (rankName.startsWith("MASTER"))   rankClass = "rank-master";
                    else if (rankName.startsWith("DIAMOND"))  rankClass = "rank-diamond";
                    else if (rankName.startsWith("PLATINUM")) rankClass = "rank-platinum";
                    else if (rankName.startsWith("GOLD"))     rankClass = "rank-gold";
                    else if (rankName.startsWith("SILVER"))   rankClass = "rank-silver";
                    else if (rankName.startsWith("BRONZE"))   rankClass = "rank-bronze";
                    else if (rankName.startsWith("IRON"))     rankClass = "rank-iron";

                    String perso = j.getPersonnagePrincipal();
                    boolean aucunMain = perso == null || perso.isBlank() || perso.equalsIgnoreCase("Aucun");
                %>
                <tr>
                    <td style="color:#475569;"><%= i + 1 %></td>
                    <td class="pseudo-cell"><%= j.getPseudo() %></td>
                    <td>
                        <% if (aucunMain) { %>
                        <span class="tag-aucun">—</span>
                        <% } else { %>
                        <span class="tag-perso"><%= perso %></span>
                        <% } %>
                    </td>
                    <td><%= j.getPays() != null ? j.getPays() : "—" %></td>
                    <td style="color:#64748b;"><%= j.getGenre() != null ? j.getGenre() : "—" %></td>
                    <td class="<%= rankClass %>">
                        <%= rankName.replace("_", " ") %>
                    </td>
                </tr>
                <% } %>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>
</main>

</body>
</html>