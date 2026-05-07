<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="be.technifutur.tournoisf6.models.Tournoi" %>
<%@ page import="be.technifutur.tournoisf6.models.Joueur" %>
<%@ page import="be.technifutur.tournoisf6.models.InscriptionTournoi" %>
<%@ page import="be.technifutur.tournoisf6.models.MatchTournoi" %>
<%@ page import="be.technifutur.tournoisf6.models.enums.EtatTournoiEnum" %>
<%@ page import="be.technifutur.tournoisf6.models.enums.BracketTypeEnum" %>
<%
    Tournoi tournoi = (Tournoi) request.getAttribute("tournoi");
    List<Joueur> joueurs = (List<Joueur>) request.getAttribute("joueurs");
    List<InscriptionTournoi> inscriptions = (List<InscriptionTournoi>) request.getAttribute("inscriptions");
    List<MatchTournoi> matchs = (List<MatchTournoi>) request.getAttribute("matchs");
    Joueur champion = (Joueur) request.getAttribute("champion");

    if (joueurs == null) joueurs = Collections.emptyList();
    if (inscriptions == null) inscriptions = Collections.emptyList();
    if (matchs == null) matchs = Collections.emptyList();

    String erreur = request.getParameter("erreur");

    List<MatchTournoi> matchsWinners = matchs.stream()
            .filter(m -> m.getBracketType() == BracketTypeEnum.WINNERS)
            .sorted(Comparator.comparingInt(MatchTournoi::getRoundNumber))
            .collect(Collectors.toList());

    List<MatchTournoi> matchsLosers = matchs.stream()
            .filter(m -> m.getBracketType() == BracketTypeEnum.LOSERS)
            .sorted(Comparator.comparingInt(MatchTournoi::getRoundNumber))
            .collect(Collectors.toList());

    List<MatchTournoi> matchsGF = matchs.stream()
            .filter(m -> m.getBracketType() == BracketTypeEnum.GRAND_FINAL)
            .sorted(Comparator.comparingInt(MatchTournoi::getRoundNumber))
            .collect(Collectors.toList());
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Détail tournoi</title>
    <style>
        :root {
            color-scheme: dark;
            --bg: #0a0f1f;
            --bg-alt: #0f172a;
            --surface: rgba(17, 24, 39, 0.84);
            --surface-2: rgba(30, 41, 59, 0.88);
            --line: rgba(148, 163, 184, 0.16);
            --line-strong: rgba(148, 163, 184, 0.28);
            --text: #f8fafc;
            --text-soft: #cbd5e1;
            --text-muted: #94a3b8;
            --primary: #22c55e;
            --primary-2: #16a34a;
            --primary-3: #15803d;
            --accent: #60a5fa;
            --danger: #ef4444;
            --danger-hover: #dc2626;
            --danger-active: #b91c1c;
            --danger-bg: rgba(127, 29, 29, 0.30);
            --danger-border: rgba(248, 113, 113, 0.30);
            --danger-text: #fecaca;
            --gold: #fde047;
            --gold-soft: #fef08a;
            --gold-bg: rgba(250, 204, 21, 0.07);
            --gold-border: rgba(250, 204, 21, 0.35);
            --winners-border: rgba(34, 197, 94, 0.30);
            --winners-bg: rgba(34, 197, 94, 0.05);
            --losers-border: rgba(239, 68, 68, 0.25);
            --losers-bg: rgba(239, 68, 68, 0.04);
            --gf-border: rgba(250, 204, 21, 0.30);
            --gf-bg: rgba(250, 204, 21, 0.05);
            --shadow-1: 0 10px 30px rgba(0, 0, 0, 0.18);
            --shadow-2: 0 20px 60px rgba(0, 0, 0, 0.32);
            --radius-sm: 12px;
            --radius-md: 18px;
            --radius-lg: 24px;
            --max-width: 1200px;
        }

        * { box-sizing: border-box; }

        html {
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
            scroll-behavior: smooth;
        }

        body {
            margin: 0;
            min-height: 100vh;
            font-family: Inter, "Segoe UI", Roboto, Arial, sans-serif;
            color: var(--text);
            background:
                    radial-gradient(circle at top left, rgba(34, 197, 94, 0.10), transparent 28%),
                    radial-gradient(circle at top right, rgba(96, 165, 250, 0.10), transparent 24%),
                    linear-gradient(180deg, var(--bg) 0%, #0b1120 45%, var(--bg-alt) 100%);
            padding: 32px 20px 48px;
        }

        .container { max-width: var(--max-width); margin: 0 auto; }

        h1 {
            margin: 0 0 20px;
            font-size: clamp(1.75rem, 1.3rem + 1.2vw, 2.5rem);
            line-height: 1.1;
            font-weight: 800;
            letter-spacing: -0.03em;
            color: var(--text);
        }

        h2 {
            margin: 0 0 18px;
            font-size: 1.15rem;
            line-height: 1.2;
            font-weight: 700;
            letter-spacing: -0.02em;
            color: var(--text);
        }

        p {
            margin: 0 0 12px;
            line-height: 1.6;
            color: var(--text-soft);
        }

        .card {
            position: relative;
            overflow: hidden;
            margin-bottom: 22px;
            padding: 24px;
            border-radius: var(--radius-lg);
            border: 1px solid var(--line);
            background:
                    linear-gradient(180deg, rgba(255,255,255,0.05), rgba(255,255,255,0.02)),
                    var(--surface);
            box-shadow: var(--shadow-2);
            backdrop-filter: blur(14px);
            -webkit-backdrop-filter: blur(14px);
        }

        .card::before {
            content: "";
            position: absolute;
            inset: 0;
            pointer-events: none;
            background: linear-gradient(
                    135deg,
                    rgba(255,255,255,0.08),
                    transparent 30%,
                    transparent 70%,
                    rgba(255,255,255,0.03)
            );
        }

        .card.winners { border-color: var(--winners-border); background: var(--winners-bg), var(--surface); }
        .card.losers  { border-color: var(--losers-border);  background: var(--losers-bg),  var(--surface); }
        .card.gf      { border-color: var(--gf-border);      background: var(--gf-bg),      var(--surface); }
        .card.champion-card {
            border-color: var(--gold-border);
            background: var(--gold-bg), var(--surface);
        }

        .bracket-label {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            font-size: 0.78rem;
            font-weight: 700;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            padding: 3px 10px;
            border-radius: 99px;
            margin-bottom: 12px;
        }
        .bracket-label.winners { background: rgba(34,197,94,0.15); color: #86efac; border: 1px solid rgba(34,197,94,0.3); }
        .bracket-label.losers  { background: rgba(239,68,68,0.15);  color: #fca5a5; border: 1px solid rgba(239,68,68,0.3); }
        .bracket-label.gf      { background: rgba(250,204,21,0.15); color: #fde047; border: 1px solid rgba(250,204,21,0.3); }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 14px;
        }

        input, select, button {
            width: 100%;
            min-height: 48px;
            padding: 12px 14px;
            border-radius: var(--radius-sm);
            font-size: 0.95rem;
            transition:
                    border-color 0.2s ease,
                    box-shadow 0.2s ease,
                    background-color 0.2s ease,
                    transform 0.18s ease,
                    color 0.2s ease;
        }

        input, select {
            border: 1px solid var(--line);
            background: var(--surface-2);
            color: var(--text);
            outline: none;
        }

        input::placeholder { color: var(--text-muted); }

        input:hover, select:hover {
            border-color: var(--line-strong);
            background: rgba(30, 41, 59, 0.98);
        }

        input:focus, select:focus {
            border-color: rgba(96, 165, 250, 0.75);
            box-shadow: 0 0 0 4px rgba(96, 165, 250, 0.15);
            background: rgba(30, 41, 59, 1);
        }

        input[disabled], select[disabled] { opacity: 0.7; cursor: not-allowed; }

        button {
            border: none;
            background: linear-gradient(135deg, var(--primary) 0%, var(--primary-2) 100%);
            color: #ffffff;
            font-weight: 700;
            letter-spacing: 0.01em;
            cursor: pointer;
            box-shadow: 0 12px 26px rgba(34, 197, 94, 0.28);
        }

        button:hover {
            transform: translateY(-1px);
            background: linear-gradient(135deg, var(--primary) 0%, var(--primary-3) 100%);
            box-shadow: 0 16px 32px rgba(34, 197, 94, 0.34);
        }

        button:active { transform: translateY(0); }

        button:focus {
            outline: none;
            box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.18), 0 16px 32px rgba(34, 197, 94, 0.34);
        }

        button.danger {
            background: linear-gradient(135deg, var(--danger) 0%, var(--danger-hover) 100%);
            box-shadow: 0 12px 26px rgba(239, 68, 68, 0.28);
        }

        button.danger:hover {
            background: linear-gradient(135deg, var(--danger-hover) 0%, var(--danger-active) 100%);
            box-shadow: 0 16px 32px rgba(239, 68, 68, 0.34);
        }

        button.danger:focus {
            box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.18), 0 16px 32px rgba(239, 68, 68, 0.34);
        }

        .error {
            margin-bottom: 18px;
            padding: 14px 16px;
            border-radius: 14px;
            border: 1px solid var(--danger-border);
            background: var(--danger-bg);
            color: var(--danger-text);
            font-weight: 600;
            box-shadow: var(--shadow-1);
        }

        table {
            width: 100%;
            margin-top: 12px;
            border-collapse: separate;
            border-spacing: 0;
            overflow: hidden;
            border-radius: 16px;
            background: rgba(255, 255, 255, 0.02);
        }

        thead th {
            padding: 16px 14px;
            text-align: left;
            font-size: 0.80rem;
            font-weight: 700;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            color: var(--text-soft);
            background: rgba(15, 23, 42, 0.96);
            border-bottom: 1px solid var(--line-strong);
            white-space: nowrap;
        }

        tbody td {
            padding: 16px 14px;
            text-align: left;
            color: var(--text);
            border-bottom: 1px solid rgba(148, 163, 184, 0.10);
            vertical-align: middle;
        }

        tbody tr { transition: background-color 0.18s ease; }
        tbody tr:hover { background: rgba(96, 165, 250, 0.06); }
        tbody tr:last-child td { border-bottom: none; }

        tbody tr.bye-row { opacity: 0.55; }
        tbody tr.bye-row td { font-style: italic; color: var(--text-muted); }

        tbody tr.reset-row td { color: var(--gold); }

        tbody td form {
            display: flex;
            gap: 8px;
            align-items: center;
        }

        tbody td form input {
            min-height: 38px;
            font-size: 0.88rem;
        }

        tbody td form button {
            min-height: 38px;
            font-size: 0.88rem;
            padding: 8px 12px;
            white-space: nowrap;
        }

        .badge {
            display: inline-block;
            padding: 2px 9px;
            border-radius: 99px;
            font-size: 0.78rem;
            font-weight: 600;
        }
        .badge.done    { background: rgba(34,197,94,0.15);  color: #86efac; border: 1px solid rgba(34,197,94,0.25); }
        .badge.pending { background: rgba(96,165,250,0.12); color: #93c5fd; border: 1px solid rgba(96,165,250,0.25); }
        .badge.bye     { background: rgba(148,163,184,0.10); color: var(--text-muted); border: 1px solid rgba(148,163,184,0.2); }
        .badge.waiting { background: rgba(250,204,21,0.10); color: #fde047; border: 1px solid rgba(250,204,21,0.2); }

        .champion-name {
            font-size: clamp(1.4rem, 1rem + 1.2vw, 2rem);
            font-weight: 800;
            color: var(--gold-soft);
            letter-spacing: -0.02em;
            margin: 6px 0 2px;
        }
        .champion-rank {
            font-size: 1rem;
            color: var(--gold);
            font-weight: 600;
        }

        a {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            color: #93c5fd;
            font-weight: 600;
            text-decoration: none;
            transition: color 0.2s ease;
        }

        a:hover { color: #bfdbfe; }
        a::before { content: "←"; transition: transform 0.2s ease; }
        a:hover::before { transform: translateX(-2px); }

        br { display: block; content: ""; margin-top: 4px; }

        @media (max-width: 900px) {
            body { padding: 22px 14px 36px; }
            .card { padding: 18px; border-radius: 20px; }
            table { display: block; overflow-x: auto; white-space: nowrap; }
            h1 { margin-bottom: 18px; }
            tbody td form { flex-direction: column; gap: 6px; }
        }

        @media (max-width: 560px) {
            .grid { grid-template-columns: 1fr; }
            input, select, button { min-height: 46px; }
            thead th, tbody td { padding: 13px 12px; }
        }
    </style>
</head>
<body>
<div class="container">

    <p><a href="${pageContext.request.contextPath}/tournois">Retour aux tournois</a></p>

    <% if (erreur != null && !erreur.isBlank()) { %>
    <div class="error"><%= erreur %></div>
    <% } %>

    <%-- BANNIÈRE CHAMPION --%>
    <% if (champion != null) { %>
    <div class="card champion-card">
        <h2 style="color: var(--gold); margin-bottom: 8px;">🏆 Champion du tournoi</h2>
        <div class="champion-name"><%= champion.getPseudo() %></div>
        <div class="champion-rank"><%= champion.getRank() %></div>
    </div>
    <% } %>

    <%-- INFOS TOURNOI --%>
    <div class="card">
        <h1><%= tournoi.getNom() %></h1>
        <p><strong>Date début :</strong> <%= tournoi.getDateDebut() %></p>
        <p><strong>Date fin :</strong> <%= tournoi.getDateFin() %></p>
        <p><strong>Format :</strong> <%= tournoi.getFormat() %></p>
        <p><strong>État :</strong> <%= tournoi.getEtat() %></p>
        <p><strong>Joueurs max :</strong> <%= tournoi.getNombreJoueursMax() %></p>
        <p><strong>Rank max autorisé :</strong> <%= tournoi.getRankMaxAutorise() %></p>

        <% if (tournoi.getEtat() == EtatTournoiEnum.EN_ATTENTE) { %>
        <form method="post" action="${pageContext.request.contextPath}/tournois" style="margin-top:16px;">
            <input type="hidden" name="action" value="lancer">
            <input type="hidden" name="tournoiId" value="<%= tournoi.getId() %>">
            <button type="submit">Lancer le tournoi</button>
        </form>
        <% } %>
    </div>

    <%-- INSCRIPTION JOUEUR --%>
    <div class="card">
        <h2>Inscrire un joueur</h2>
        <% if (tournoi.getEtat() == EtatTournoiEnum.EN_ATTENTE) { %>
        <form method="post" action="${pageContext.request.contextPath}/inscriptions-tournoi">
            <input type="hidden" name="tournoiId" value="<%= tournoi.getId() %>">
            <div class="grid">
                <select name="joueurId" required>
                    <option value="">-- Choisir un joueur --</option>
                    <% for (Joueur joueur : joueurs) { %>
                    <option value="<%= joueur.getId() %>">
                        <%= joueur.getPseudo() %> - <%= joueur.getRank() %>
                    </option>
                    <% } %>
                </select>
            </div>
            <br>
            <button type="submit">Inscrire</button>
        </form>
        <% } else { %>
        <p>Les inscriptions sont fermées.</p>
        <% } %>
    </div>

    <%-- JOUEURS INSCRITS --%>
    <div class="card">
        <h2>Joueurs inscrits</h2>
        <table>
            <thead>
            <tr>
                <th>Pseudo</th>
                <th>Email</th>
                <th>Rank</th>
                <th>Main</th>
                <th>Éliminé</th>
            </tr>
            </thead>
            <tbody>
            <% for (InscriptionTournoi inscription : inscriptions) { %>
            <tr>
                <td><%= inscription.getJoueur().getPseudo() %></td>
                <td><%= inscription.getJoueur().getEmail() %></td>
                <td><%= inscription.getJoueur().getRank() %></td>
                <td><%= inscription.getJoueur().getPersonnagePrincipal() %></td>
                <td><%= inscription.getElimine() ? "Oui" : "Non" %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>

    <%-- WINNERS BRACKET --%>
    <% if (!matchsWinners.isEmpty()) { %>
    <div class="card winners">
        <span class="bracket-label winners">🏅 Winners Bracket</span>
        <h2 style="margin-top:4px;">Winners Bracket</h2>
        <table>
            <thead>
            <tr>
                <th>ID</th><th>Round</th><th>Joueur 1</th><th>Joueur 2</th>
                <th>Score</th><th>Gagnant</th><th>État</th><th>Admin</th>
            </tr>
            </thead>
            <tbody>
            <% for (MatchTournoi match : matchsWinners) {
                if (match.getJoueur1() == null && match.getJoueur2() == null && !Boolean.TRUE.equals(match.getTermine())) continue;
                boolean isBye = Boolean.TRUE.equals(match.getTermine())
                        && (match.getJoueur1() == null || match.getJoueur2() == null);
            %>
            <tr class="<%= isBye ? "bye-row" : "" %>">
                <td><%= match.getId() %></td>
                <td>R<%= match.getRoundNumber() %></td>
                <td><%= match.getJoueur1() != null ? match.getJoueur1().getPseudo() : "-" %></td>
                <td>
                    <% if (match.getJoueur2() != null) { %>
                    <%= match.getJoueur2().getPseudo() %>
                    <% } else if (isBye) { %>
                    BYE
                    <% } else { %>
                    -
                    <% } %>
                </td>
                <td>
                    <%= match.getScoreJoueur1() != null ? match.getScoreJoueur1() : "-" %>
                    -
                    <%= match.getScoreJoueur2() != null ? match.getScoreJoueur2() : "-" %>
                </td>
                <td><%= match.getGagnant() != null ? match.getGagnant().getPseudo() : "-" %></td>
                <td>
                    <% if (isBye) { %>
                    <span class="badge bye">Bye</span>
                    <% } else if (Boolean.TRUE.equals(match.getTermine())) { %>
                    <span class="badge done">Terminé</span>
                    <% } else if (match.getJoueur1() != null && match.getJoueur2() != null) { %>
                    <span class="badge pending">À jouer</span>
                    <% } else { %>
                    <span class="badge waiting">En attente</span>
                    <% } %>
                </td>
                <td>
                    <% if (!Boolean.TRUE.equals(match.getTermine()) && match.getJoueur1() != null && match.getJoueur2() != null) { %>
                    <form method="post" action="${pageContext.request.contextPath}/admin/matchs">
                        <input type="hidden" name="matchId" value="<%= match.getId() %>">
                        <input type="hidden" name="tournoiId" value="<%= tournoi.getId() %>">
                        <input type="number" name="scoreJoueur1" min="0" placeholder="Score J1" required>
                        <input type="number" name="scoreJoueur2" min="0" placeholder="Score J2" required>
                        <button type="submit" class="danger">Valider</button>
                    </form>
                    <% } else { %>-<% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>

    <%-- LOSERS BRACKET --%>
    <% if (!matchsLosers.isEmpty()) { %>
    <div class="card losers">
        <span class="bracket-label losers">💀 Losers Bracket</span>
        <h2 style="margin-top:4px;">Losers Bracket</h2>
        <table>
            <thead>
            <tr>
                <th>ID</th><th>Round</th><th>Joueur 1</th><th>Joueur 2</th>
                <th>Score</th><th>Gagnant</th><th>État</th><th>Admin</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (MatchTournoi match : matchsLosers) {
                    boolean j1 = match.getJoueur1() != null;
                    boolean j2 = match.getJoueur2() != null;
                    boolean termine = Boolean.TRUE.equals(match.getTermine());
                    if (!j1 && !j2 && !termine) continue;
                    boolean isBye = termine && (!j1 || !j2);
            %>
            <tr class="<%= isBye ? "bye-row" : "" %>">
                <td><%= match.getId() %></td>
                <td>R<%= match.getRoundNumber() %></td>
                <td><%= j1 ? match.getJoueur1().getPseudo() : "-" %></td>
                <td>
                    <% if (j2) { %>
                    <%= match.getJoueur2().getPseudo() %>
                    <% } else if (isBye) { %>
                    BYE
                    <% } else { %>
                    -
                    <% } %>
                </td>
                <td>
                    <%= match.getScoreJoueur1() != null ? match.getScoreJoueur1() : "-" %>
                    -
                    <%= match.getScoreJoueur2() != null ? match.getScoreJoueur2() : "-" %>
                </td>
                <td><%= match.getGagnant() != null ? match.getGagnant().getPseudo() : "-" %></td>
                <td>
                    <% if (isBye) { %>
                    <span class="badge bye">Bye</span>
                    <% } else if (termine) { %>
                    <span class="badge done">Terminé</span>
                    <% } else if (j1 && j2) { %>
                    <span class="badge pending">À jouer</span>
                    <% } else { %>
                    <span class="badge waiting">En attente d'un adversaire</span>
                    <% } %>
                </td>
                <td>
                    <% if (!termine && j1 && j2) { %>
                    <form method="post" action="${pageContext.request.contextPath}/admin/matchs">
                        <input type="hidden" name="matchId" value="<%= match.getId() %>">
                        <input type="hidden" name="tournoiId" value="<%= tournoi.getId() %>">
                        <input type="number" name="scoreJoueur1" min="0" placeholder="Score J1" required>
                        <input type="number" name="scoreJoueur2" min="0" placeholder="Score J2" required>
                        <button type="submit" class="danger">Valider</button>
                    </form>
                    <% } else { %>-<% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>

    <%-- GRAND FINAL --%>
    <% if (!matchsGF.isEmpty()) { %>
    <div class="card gf">
        <span class="bracket-label gf">⭐ Grand Final</span>
        <h2 style="margin-top:4px;">Grand Final</h2>
        <table>
            <thead>
            <tr>
                <th>ID</th><th>Round</th><th>Joueur 1</th><th>Joueur 2</th>
                <th>Score</th><th>Gagnant</th><th>État</th><th>Admin</th>
            </tr>
            </thead>
            <tbody>
            <% for (MatchTournoi match : matchsGF) {
                boolean isReset = match.getRoundNumber() == 2
                        && match.getJoueur1() == null
                        && match.getJoueur2() == null
                        && !Boolean.TRUE.equals(match.getTermine());
                boolean isResetDesactive = match.getRoundNumber() == 2
                        && Boolean.TRUE.equals(match.getTermine())
                        && match.getGagnant() == null;
            %>
            <tr class="<%= (match.getRoundNumber() == 2) ? "reset-row" : "" %>">
                <td><%= match.getId() %></td>
                <td>
                    R<%= match.getRoundNumber() %>
                    <% if (match.getRoundNumber() == 2) { %>
                    <span style="font-size:0.75rem; color:var(--gold); margin-left:4px;">(Reset)</span>
                    <% } %>
                </td>
                <td><%= match.getJoueur1() != null ? match.getJoueur1().getPseudo() : "-" %></td>
                <td><%= match.getJoueur2() != null ? match.getJoueur2().getPseudo() : "-" %></td>
                <td>
                    <%= match.getScoreJoueur1() != null ? match.getScoreJoueur1() : "-" %>
                    -
                    <%= match.getScoreJoueur2() != null ? match.getScoreJoueur2() : "-" %>
                </td>
                <td><%= match.getGagnant() != null ? match.getGagnant().getPseudo() : "-" %></td>
                <td>
                    <% if (isReset) { %>
                    <span class="badge waiting">En attente (bracket reset)</span>
                    <% } else if (isResetDesactive) { %>
                    <span class="badge bye">Non joué</span>
                    <% } else if (Boolean.TRUE.equals(match.getTermine())) { %>
                    <span class="badge done">Terminé</span>
                    <% } else if (match.getJoueur1() != null && match.getJoueur2() != null) { %>
                    <span class="badge pending">À jouer</span>
                    <% } else { %>
                    <span class="badge waiting">En attente</span>
                    <% } %>
                </td>
                <td>
                    <% if (!Boolean.TRUE.equals(match.getTermine()) && match.getJoueur1() != null && match.getJoueur2() != null) { %>
                    <form method="post" action="${pageContext.request.contextPath}/admin/matchs">
                        <input type="hidden" name="matchId" value="<%= match.getId() %>">
                        <input type="hidden" name="tournoiId" value="<%= tournoi.getId() %>">
                        <input type="number" name="scoreJoueur1" min="0" placeholder="Score J1" required>
                        <input type="number" name="scoreJoueur2" min="0" placeholder="Score J2" required>
                        <button type="submit" class="danger">Valider</button>
                    </form>
                    <% } else { %>-<% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>

</div>
</body>
</html>