<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="be.technifutur.tournoisf6.models.Tournoi" %>
<%@ page import="be.technifutur.tournoisf6.models.enums.RankEnum" %>
<%
    List<Tournoi> tournois = (List<Tournoi>) request.getAttribute("tournois");
    if (tournois == null) tournois = Collections.emptyList();

    RankEnum[] ranks = (RankEnum[]) request.getAttribute("ranks");
    String erreur = (String) request.getAttribute("erreur");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tournois — TournoiSF6</title>
    <link href="https://api.fontshare.com/v2/css?f[]=satoshi@400,500,700,800&display=swap" rel="stylesheet">
    <style>
        :root {
            color-scheme: dark;
            --bg:            #080d1a;
            --bg-alt:        #0c1426;
            --surface:       rgba(15, 22, 40, 0.90);
            --surface-2:     rgba(22, 33, 58, 0.92);
            --surface-hover: rgba(28, 40, 68, 0.96);
            --line:          rgba(148, 163, 184, 0.14);
            --line-strong:   rgba(148, 163, 184, 0.26);
            --text:          #f1f5f9;
            --text-soft:     #cbd5e1;
            --text-muted:    #64748b;
            --primary:       #22c55e;
            --primary-2:     #16a34a;
            --primary-3:     #15803d;
            --primary-glow:  rgba(34, 197, 94, 0.22);
            --accent:        #60a5fa;
            --accent-glow:   rgba(96, 165, 250, 0.15);
            --danger-bg:     rgba(127, 29, 29, 0.28);
            --danger-border: rgba(248, 113, 113, 0.28);
            --danger-text:   #fca5a5;
            --radius-sm:  10px;
            --radius-md:  16px;
            --radius-lg:  22px;
            --shadow-md:  0 8px 24px rgba(0, 0, 0, 0.30);
            --transition: 180ms cubic-bezier(0.16, 1, 0.3, 1);
            --max-width:  1160px;
        }

        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        html {
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
            scroll-behavior: smooth;
        }

        body {
            min-height: 100vh;
            font-family: 'Satoshi', Inter, 'Segoe UI', sans-serif;
            font-size: 1rem;
            color: var(--text);
            background:
                    radial-gradient(ellipse 60% 40% at 10% 0%, rgba(34, 197, 94, 0.08) 0%, transparent 60%),
                    radial-gradient(ellipse 50% 35% at 90% 0%, rgba(96, 165, 250, 0.08) 0%, transparent 55%),
                    linear-gradient(180deg, var(--bg) 0%, var(--bg-alt) 100%);
            padding: 0 0 64px;
        }

        /* ── Topbar ── */
        .topbar {
            border-bottom: 1px solid var(--line);
            background: rgba(8, 13, 26, 0.85);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            padding: 14px 24px;
            position: sticky;
            top: 0;
            z-index: 100;
        }
        .topbar-inner {
            max-width: var(--max-width);
            margin: 0 auto;
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .btn-back {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 16px;
            border-radius: var(--radius-sm);
            border: 1px solid var(--line-strong);
            background: var(--surface-2);
            color: var(--text-soft);
            font-size: 0.875rem;
            font-weight: 500;
            text-decoration: none;
            transition: all var(--transition);
        }
        .btn-back::before { content: "←"; }
        .btn-back:hover {
            background: var(--surface-hover);
            border-color: var(--accent);
            color: var(--accent);
        }
        .topbar-title {
            font-size: 0.95rem;
            font-weight: 700;
            color: var(--text-muted);
            margin-left: auto;
            letter-spacing: 0.01em;
        }

        /* ── Container ── */
        .container {
            max-width: var(--max-width);
            margin: 0 auto;
            padding: 40px 24px 0;
        }

        /* ── Page header ── */
        .page-header { margin-bottom: 36px; }
        .page-header h1 {
            font-size: clamp(1.75rem, 1.2rem + 1.4vw, 2.75rem);
            font-weight: 800;
            letter-spacing: -0.035em;
            line-height: 1.08;
            background: linear-gradient(135deg, var(--text) 40%, var(--primary));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 6px;
        }
        .page-header p {
            color: var(--text-muted);
            font-size: 0.95rem;
        }

        /* ── Cards ── */
        .card {
            position: relative;
            overflow: hidden;
            margin-bottom: 24px;
            padding: 28px 28px 24px;
            border-radius: var(--radius-lg);
            border: 1px solid var(--line);
            background: var(--surface);
            box-shadow: var(--shadow-md);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
        }
        .card::after {
            content: "";
            position: absolute;
            inset: 0;
            pointer-events: none;
            border-radius: inherit;
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.04) 0%, transparent 40%);
        }
        .card-title {
            font-size: 1rem;
            font-weight: 700;
            color: var(--text);
            letter-spacing: -0.01em;
            margin-bottom: 22px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .card-title::before {
            content: "";
            display: block;
            width: 3px;
            height: 18px;
            border-radius: 2px;
            background: var(--primary);
            flex-shrink: 0;
        }

        /* ── Grille de formulaire ── */
        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 14px;
            margin-bottom: 22px;
        }
        .form-group {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .form-label {
            font-size: 0.75rem;
            font-weight: 700;
            letter-spacing: 0.05em;
            text-transform: uppercase;
            color: var(--text-muted);
        }

        /* ── Inputs & Selects ── */
        input, select {
            width: 100%;
            min-height: 44px;
            padding: 10px 14px;
            border-radius: var(--radius-sm);
            border: 1px solid var(--line);
            background: var(--surface-2);
            color: var(--text);
            font-family: inherit;
            font-size: 0.925rem;
            outline: none;
            transition: border-color var(--transition), box-shadow var(--transition), background var(--transition);
            -webkit-appearance: none;
            appearance: none;
        }
        input::placeholder { color: var(--text-muted); }
        input:hover, select:hover {
            border-color: var(--line-strong);
            background: var(--surface-hover);
        }
        input:focus, select:focus {
            border-color: rgba(96, 165, 250, 0.70);
            box-shadow: 0 0 0 3px var(--accent-glow);
            background: var(--surface-hover);
        }
        input[disabled], select[disabled] {
            opacity: 0.45;
            cursor: not-allowed;
        }
        select option { background: #1e293b; }

        /* ── Bouton principal ── */
        .btn-primary {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            min-height: 44px;
            padding: 10px 28px;
            border-radius: var(--radius-sm);
            border: none;
            background: var(--primary);
            color: #fff;
            font-family: inherit;
            font-size: 0.925rem;
            font-weight: 700;
            letter-spacing: 0.01em;
            cursor: pointer;
            box-shadow: 0 4px 16px var(--primary-glow);
            transition: all var(--transition);
        }
        .btn-primary:hover {
            background: var(--primary-2);
            box-shadow: 0 8px 28px rgba(34, 197, 94, 0.36);
            transform: translateY(-1px);
        }
        .btn-primary:active { transform: translateY(0); }
        .btn-primary:focus {
            outline: none;
            box-shadow: 0 0 0 3px var(--primary-glow);
        }

        /* ── Alert erreur ── */
        .alert-error {
            margin-bottom: 20px;
            padding: 14px 18px;
            border-radius: var(--radius-md);
            border: 1px solid var(--danger-border);
            background: var(--danger-bg);
            color: var(--danger-text);
            font-size: 0.9rem;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .alert-error::before { content: "⚠"; font-size: 1.1rem; }

        /* ── Table ── */
        .table-wrap {
            overflow-x: auto;
            border-radius: var(--radius-md);
            border: 1px solid var(--line);
        }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.9rem;
        }
        thead th {
            padding: 13px 16px;
            text-align: left;
            font-size: 0.73rem;
            font-weight: 700;
            letter-spacing: 0.07em;
            text-transform: uppercase;
            color: var(--text-muted);
            background: rgba(8, 13, 26, 0.95);
            border-bottom: 1px solid var(--line-strong);
            white-space: nowrap;
        }
        tbody td {
            padding: 14px 16px;
            color: var(--text-soft);
            border-bottom: 1px solid var(--line);
            vertical-align: middle;
        }
        tbody tr:last-child td { border-bottom: none; }
        tbody tr { transition: background var(--transition); }
        tbody tr:hover { background: rgba(96, 165, 250, 0.05); }

        .td-name {
            color: var(--text);
            font-weight: 600;
        }

        /* ── Badges état ── */
        .badge {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            padding: 3px 10px;
            border-radius: 99px;
            font-size: 0.73rem;
            font-weight: 700;
            white-space: nowrap;
        }
        .badge-green  { background: rgba(34, 197, 94, 0.15);  color: #86efac; border: 1px solid rgba(34, 197, 94, 0.25); }
        .badge-blue   { background: rgba(96, 165, 250, 0.12); color: #93c5fd; border: 1px solid rgba(96, 165, 250, 0.22); }
        .badge-gold   { background: rgba(250, 204, 21, 0.12); color: #fde047; border: 1px solid rgba(250, 204, 21, 0.22); }
        .badge-muted  { background: rgba(148, 163, 184, 0.10); color: var(--text-muted); border: 1px solid rgba(148, 163, 184, 0.18); }

        /* ── Lien détail ── */
        .table-link {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            color: var(--accent);
            font-weight: 600;
            font-size: 0.875rem;
            text-decoration: none;
            transition: color var(--transition);
        }
        .table-link:hover { color: #bfdbfe; }
        .table-link::after { content: "→"; transition: transform var(--transition); }
        .table-link:hover::after { transform: translateX(3px); }

        /* ── Responsive ── */
        @media (max-width: 768px) {
            .container { padding: 28px 16px 0; }
            .card { padding: 20px 18px; }
            .form-grid { grid-template-columns: 1fr; }
        }
        @media (max-width: 480px) {
            .topbar { padding: 12px 16px; }
            thead th, tbody td { padding: 11px 12px; }
        }
    </style>
</head>
<body>

<nav class="topbar">
    <div class="topbar-inner">
        <a class="btn-back" href="${pageContext.request.contextPath}/">Accueil</a>
        <span class="topbar-title">TournoiSF6</span>
    </div>
</nav>

<div class="container">

    <div class="page-header">
        <h1>Tournois SF6</h1>
        <p>Gérez et suivez vos tournois Street Fighter 6</p>
    </div>

    <% if (erreur != null && !erreur.isBlank()) { %>
    <div class="alert-error"><%= erreur %></div>
    <% } %>

    <!-- ═══ Formulaire de création ═══ -->
    <div class="card">
        <div class="card-title">Créer un tournoi</div>
        <form method="post" action="${pageContext.request.contextPath}/tournois">
            <input type="hidden" name="action" value="create">
            <div class="form-grid">

                <div class="form-group">
                    <label class="form-label" for="nom">Nom</label>
                    <input id="nom" type="text" name="nom" placeholder="Nom du tournoi" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="dateDebut">Date début</label>
                    <input id="dateDebut" type="date" name="dateDebut" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="dateFin">Date fin</label>
                    <input id="dateFin" type="date" name="dateFin" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="joueurs">Max joueurs</label>
                    <input id="joueurs" type="number" name="nombreJoueursMax" min="2" placeholder="Ex : 8, 16, 32…" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="rank">Rank max autorisé</label>
                    <select id="rank" name="rankMaxAutorise" required>
                        <% for (RankEnum rank : ranks) { %>
                        <option value="<%= rank.name() %>"><%= rank.name() %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label">Format</label>
                    <input type="text" value="DOUBLE ELIMINATION" disabled>
                </div>

            </div>
            <button type="submit" class="btn-primary">Créer le tournoi</button>
        </form>
    </div>

    <!-- ═══ Liste des tournois ═══ -->
    <div class="card">
        <div class="card-title">Liste des tournois</div>
        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th>Nom</th>
                    <th>Date début</th>
                    <th>Date fin</th>
                    <th>Max joueurs</th>
                    <th>Format</th>
                    <th>État</th>
                    <th>Rank max</th>
                    <th>Détail</th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (Tournoi tournoi : tournois) {
                        String etatName  = tournoi.getEtat().name();
                        String etatClass;
                        switch (etatName) {
                            case "EN_COURS":   etatClass = "badge-blue";  break;
                            case "TERMINE":    etatClass = "badge-green"; break;
                            case "EN_ATTENTE": etatClass = "badge-gold";  break;
                            default:           etatClass = "badge-muted"; break;
                        }
                %>
                <tr>
                    <td class="td-name"><%= tournoi.getNom() %></td>
                    <td><%= tournoi.getDateDebut() %></td>
                    <td><%= tournoi.getDateFin() %></td>
                    <td><%= tournoi.getNombreJoueursMax() %></td>
                    <td><%= tournoi.getFormat() %></td>
                    <td><span class="badge <%= etatClass %>"><%= tournoi.getEtat() %></span></td>
                    <td><%= tournoi.getRankMaxAutorise() %></td>
                    <td>
                        <a class="table-link" href="${pageContext.request.contextPath}/tournois?id=<%= tournoi.getId() %>">Voir</a>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>

</div>
</body>
</html>