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
                        case MASTER: return 0;

                        case DIAMOND_V: return 1;
                        case DIAMOND_IV: return 2;
                        case DIAMOND_III: return 3;
                        case DIAMOND_II: return 4;
                        case DIAMOND_I: return 5;

                        case PLATINUM_V: return 6;
                        case PLATINUM_IV: return 7;
                        case PLATINUM_III: return 8;
                        case PLATINUM_II: return 9;
                        case PLATINUM_I: return 10;

                        case GOLD_V: return 11;
                        case GOLD_IV: return 12;
                        case GOLD_III: return 13;
                        case GOLD_II: return 14;
                        case GOLD_I: return 15;

                        case SILVER_V: return 16;
                        case SILVER_IV: return 17;
                        case SILVER_III: return 18;
                        case SILVER_II: return 19;
                        case SILVER_I: return 20;

                        case BRONZE_V: return 21;
                        case BRONZE_IV: return 22;
                        case BRONZE_III: return 23;
                        case BRONZE_II: return 24;
                        case BRONZE_I: return 25;

                        case IRON_V: return 26;
                        case IRON_IV: return 27;
                        case IRON_III: return 28;
                        case IRON_II: return 29;
                        case IRON_I: return 30;

                        case ROOKIE_V: return 31;
                        case ROOKIE_IV: return 32;
                        case ROOKIE_III: return 33;
                        case ROOKIE_II: return 34;
                        case ROOKIE_I: return 35;

                        default: return Integer.MAX_VALUE;
                    }
                })
                .thenComparing(
                        j -> j.getPseudo() == null ? "" : j.getPseudo(),
                        String.CASE_INSENSITIVE_ORDER
                )
        );
    }

    String erreur = (String) request.getAttribute("erreur");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des joueurs</title>
    <style>
        :root {
            --bg: #0f172a;
            --card: rgba(17, 24, 39, 0.92);
            --card-light: #1f2937;
            --text: #f8fafc;
            --muted: #cbd5e1;
            --accent: #22c55e;
            --accent-hover: #16a34a;
            --secondary: #334155;
            --secondary-hover: #475569;
            --border: rgba(255,255,255,0.08);
            --shadow: 0 10px 30px rgba(0,0,0,0.25);
            --radius: 18px;
        }

        * { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            font-family: "Segoe UI", Roboto, Arial, sans-serif;
            background: linear-gradient(135deg, #0f172a, #1e293b);
            color: var(--text);
            min-height: 100vh;
            padding: 32px 16px;
        }

        .container { max-width: 1100px; margin: 0 auto; }

        .header-top {
            display: flex; justify-content: space-between;
            align-items: center; gap: 16px; flex-wrap: wrap;
            margin-bottom: 24px;
        }

        .title { font-size: 2.2rem; font-weight: 700; }

        .home-link {
            display: inline-block; text-decoration: none;
            background: var(--secondary); color: white;
            padding: 12px 18px; border-radius: 12px;
            font-weight: 600; transition: 0.2s ease;
        }

        .home-link:hover {
            background: var(--secondary-hover);
            transform: translateY(-1px);
        }

        .card {
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            padding: 24px;
            margin-bottom: 24px;
        }

        .error {
            background: rgba(239, 68, 68, 0.12);
            color: #fecaca;
            border: 1px solid rgba(239, 68, 68, 0.35);
            padding: 14px 16px;
            border-radius: 12px;
            margin-bottom: 20px;
            font-weight: 600;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 14px;
        }

        .form-grid input,
        .form-grid select {
            width: 100%;
            padding: 12px 14px;
            border-radius: 12px;
            border: 1px solid var(--border);
            background: var(--card-light);
            color: var(--text);
            outline: none;
        }

        .form-grid input:focus,
        .form-grid select:focus {
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.18);
        }

        .form-actions {
            margin-top: 18px;
            display: flex;
            justify-content: flex-end;
        }

        button {
            background: var(--accent); color: white;
            border: none; border-radius: 12px;
            padding: 12px 20px;
            font-size: 1rem; font-weight: 700;
            cursor: pointer;
        }

        button:hover { background: var(--accent-hover); }

        .table-wrapper { overflow-x: auto; }

        table {
            width: 100%; border-collapse: collapse;
            border-radius: 14px; overflow: hidden;
        }

        thead { background: rgba(255,255,255,0.06); }

        th, td {
            padding: 16px 14px;
            text-align: left;
        }

        th {
            font-size: 0.95rem;
            color: #86efac;
            text-transform: uppercase;
        }

        tbody tr { border-top: 1px solid var(--border); }

        tbody tr:hover { background: rgba(255,255,255,0.04); }

        td { color: var(--muted); }

        .rank-text { font-weight: 800; color: #93c5fd; }

        .empty {
            text-align: center;
            color: var(--muted);
            padding: 24px 0 8px;
        }
    </style>
</head>
<body>
<div class="container">

    <div class="header-top">
        <h1 class="title">Joueurs SF6</h1>
        <a class="home-link" href="${pageContext.request.contextPath}/">Retour à l'accueil</a>
    </div>

    <% if (erreur != null) { %>
    <div class="error"><%= erreur %></div>
    <% } %>

    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/joueurs">
            <div class="form-grid">
                <input type="text" name="pseudo" placeholder="Pseudo" required>
                <input type="email" name="email" placeholder="Email" required>
                <input type="text" name="motDePasse" placeholder="Mot de passe" required>
                <input type="date" name="dateNaissance">
                <input type="text" name="genre" placeholder="Genre" required>

                <select name="rank" required>
                    <option value="ROOKIE_I">Rookie I</option>
                    <option value="ROOKIE_II">Rookie II</option>
                    <option value="ROOKIE_III">Rookie III</option>
                    <option value="ROOKIE_IV">Rookie IV</option>
                    <option value="ROOKIE_V">Rookie V</option>

                    <option value="IRON_I">Iron I</option>
                    <option value="IRON_II">Iron II</option>
                    <option value="IRON_III">Iron III</option>
                    <option value="IRON_IV">Iron IV</option>
                    <option value="IRON_V">Iron V</option>

                    <option value="BRONZE_I">Bronze I</option>
                    <option value="BRONZE_II">Bronze II</option>
                    <option value="BRONZE_III">Bronze III</option>
                    <option value="BRONZE_IV">Bronze IV</option>
                    <option value="BRONZE_V">Bronze V</option>

                    <option value="SILVER_I">Silver I</option>
                    <option value="SILVER_II">Silver II</option>
                    <option value="SILVER_III">Silver III</option>
                    <option value="SILVER_IV">Silver IV</option>
                    <option value="SILVER_V">Silver V</option>

                    <option value="GOLD_I">Gold I</option>
                    <option value="GOLD_II">Gold II</option>
                    <option value="GOLD_III">Gold III</option>
                    <option value="GOLD_IV">Gold IV</option>
                    <option value="GOLD_V">Gold V</option>

                    <option value="PLATINUM_I">Platinum I</option>
                    <option value="PLATINUM_II">Platinum II</option>
                    <option value="PLATINUM_III">Platinum III</option>
                    <option value="PLATINUM_IV">Platinum IV</option>
                    <option value="PLATINUM_V">Platinum V</option>

                    <option value="DIAMOND_I">Diamond I</option>
                    <option value="DIAMOND_II">Diamond II</option>
                    <option value="DIAMOND_III">Diamond III</option>
                    <option value="DIAMOND_IV">Diamond IV</option>
                    <option value="DIAMOND_V">Diamond V</option>

                    <option value="MASTER">Master</option>
                </select>

                <input type="text" name="personnagePrincipal" placeholder="Main character" required>
                <input type="text" name="pays" placeholder="Pays" required>
            </div>

            <div class="form-actions">
                <button type="submit">Ajouter</button>
            </div>
        </form>
    </div>

    <div class="card table-wrapper">
        <table>
            <thead>
            <tr>
                <th>Pseudo</th>
                <th>Genre</th>
                <th>Main</th>
                <th>Pays</th>
                <th>Rank</th>
            </tr>
            </thead>
            <tbody>
            <% if (joueurs.isEmpty()) { %>
            <tr>
                <td colspan="5" class="empty">Aucun joueur trouvé.</td>
            </tr>
            <% } else { %>
            <% for (Joueur joueur : joueurs) { %>
            <tr>
                <td><%= joueur.getPseudo() %></td>
                <td><%= joueur.getGenre() %></td>
                <td><%= joueur.getPersonnagePrincipal() %></td>
                <td><%= joueur.getPays() %></td>
                <td class="rank-text"><%= joueur.getRank() %></td>
            </tr>
            <% } %>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>