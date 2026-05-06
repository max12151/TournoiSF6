<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="be.technifutur.tournoisf6.models.Joueur" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>SF6 Tournoi - Accueil</title>
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            font-family: Arial, sans-serif;
            margin: 0;
            background: #0f172a;
            color: #e2e8f0;
        }

        header {
            background: #111827;
            padding: 24px;
            border-bottom: 1px solid #1f2937;
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 12px;
        }

        h1, h2, h3 {
            margin-top: 0;
        }

        h1 {
            color: #f59e0b;
            margin-bottom: 10px;
        }

        h2 {
            color: #fbbf24;
            margin-bottom: 12px;
        }

        h3 {
            color: #93c5fd;
            margin-bottom: 10px;
        }

        nav {
            margin-top: 16px;
        }

        nav a {
            color: #7dd3fc;
            margin-right: 18px;
            text-decoration: none;
            font-weight: bold;
        }

        nav a:hover {
            text-decoration: underline;
        }

        /* --- Zone auth (droite du header) --- */
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

        .user-badge strong {
            color: #f59e0b;
        }

        .btn-logout {
            background: #ef4444;
            color: white;
            border: none;
            border-radius: 8px;
            padding: 8px 16px;
            font-size: 14px;
            font-weight: bold;
            text-decoration: none;
            cursor: pointer;
            transition: background 0.2s;
        }

        .btn-logout:hover { background: #dc2626; }

        .btn-login {
            background: #f59e0b;
            color: #0f172a;
            border: none;
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

        /* --- Contenu principal --- */
        main {
            max-width: 1150px;
            margin: 30px auto;
            padding: 0 20px 40px;
        }

        .card {
            background: #1e293b;
            border-radius: 14px;
            padding: 22px;
            margin-bottom: 22px;
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.20);
        }

        .intro {
            border-left: 5px solid #f59e0b;
        }

        .welcome-banner {
            border-left: 5px solid #7dd3fc;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 20px;
        }

        .step {
            margin-bottom: 16px;
            padding: 14px;
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 10px;
        }

        .step strong {
            color: #f8fafc;
        }

        ul, ol {
            padding-left: 20px;
            line-height: 1.7;
        }

        li {
            margin-bottom: 8px;
        }

        .tag {
            display: inline-block;
            padding: 6px 10px;
            margin-right: 8px;
            margin-bottom: 8px;
            border-radius: 999px;
            background: #334155;
            color: #e2e8f0;
            font-size: 14px;
        }

        .highlight {
            color: #fbbf24;
            font-weight: bold;
        }

        footer {
            text-align: center;
            color: #94a3b8;
            font-size: 14px;
            margin-top: 30px;
        }
    </style>
</head>
<body>

<%
    Joueur joueurConnecte = (Joueur) session.getAttribute("joueurConnecte");
%>

<header>
    <div>
        <h1>Street Fighter 6 Tournament Manager</h1>
        <p style="color:#64748b; font-size:13px; margin: 0;">
            Feuille de route du projet Jakarta EE / Tomcat / IntelliJ
        </p>
        <nav>
            <a href="${pageContext.request.contextPath}/">Accueil</a>
            <a href="${pageContext.request.contextPath}/joueurs">Joueurs</a>
            <a href="${pageContext.request.contextPath}/tournois">Tournois</a>
            <a href="${pageContext.request.contextPath}/matchs">Matchs</a>
        </nav>
    </div>

    <div class="header-auth">
        <% if (joueurConnecte != null) { %>
        <div class="user-badge">
            👤 Connecté : <strong><%= joueurConnecte.getPseudo() %></strong>
        </div>
        <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Déconnexion</a>
        <% } else { %>
        <a href="${pageContext.request.contextPath}/login" class="btn-login">Connexion</a>
        <a href="${pageContext.request.contextPath}/register" class="btn-register">S'inscrire</a>
        <% } %>
    </div>
</header>

<main>

    <%-- Bannière de bienvenue si connecté --%>
    <% if (joueurConnecte != null) { %>
    <section class="card welcome-banner">
        <h2>Bienvenue, <%= joueurConnecte.getPseudo() %> ! 🔥</h2>
        <p>
            Rôle : <strong style="color:#7dd3fc;"><%= joueurConnecte.getRole() %></strong>
            &nbsp;|&nbsp;
            Personnage : <strong style="color:#fbbf24;"><%= joueurConnecte.getPersonnagePrincipal() %></strong>
            &nbsp;|&nbsp;
            Rang : <strong style="color:#a78bfa;"><%= joueurConnecte.getRank() %></strong>
        </p>
    </section>
    <% } %>

    <section class="card intro">
        <h2>Objectif du projet</h2>
        <p>
            Cette V1 sert de base pour construire une application web de gestion de tournois Street Fighter 6.
            Le projet fonctionne actuellement avec des servlets, des JSP et des données stockées en mémoire.
        </p>
        <p>
            L'idée est d'améliorer le projet petit à petit sans tout casser :
            <span class="highlight">d'abord stabiliser le web, ensuite ajouter la base de données, puis améliorer l'architecture et l'interface.</span>
        </p>
    </section>

    <section class="card">
        <h2>État actuel de la V1</h2>
        <div class="tag">JDK 21</div>
        <div class="tag">Jakarta Servlet</div>
        <div class="tag">JSP</div>
        <div class="tag">Tomcat 10</div>
        <div class="tag">Maven WAR</div>
        <div class="tag">BCrypt</div>

        <ul>
            <li>Accueil avec navigation entre les pages.</li>
            <li>Liste des joueurs.</li>
            <li>Ajout d'un joueur via formulaire.</li>
            <li>Liste des tournois.</li>
            <li>Liste des matchs.</li>
            <li>✅ Inscription avec hashage BCrypt du mot de passe.</li>
            <li>✅ Connexion / Déconnexion avec session.</li>
        </ul>
    </section>

    <section class="card">
        <h2>Plan d'amélioration du projet</h2>

        <div class="step">
            <h3>Étape 1 - Nettoyer et structurer le code</h3>
            <ol>
                <li>Créer des packages plus clairs : <strong>controller</strong>, <strong>service</strong>, <strong>model</strong>, puis plus tard <strong>repository</strong>.</li>
                <li>Éviter de mettre trop de logique dans les servlets.</li>
                <li>Laisser les servlets gérer la requête HTTP, et déplacer la logique métier dans le service.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 2 - Améliorer les JSP</h3>
            <ol>
                <li>Créer un style commun pour toutes les pages afin d'éviter de répéter le CSS partout.</li>
                <li>Ajouter un vrai layout avec un header, un menu et un footer commun.</li>
                <li>Ensuite, remplacer progressivement les gros scripts JSP par JSTL pour avoir des pages plus propres.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 3 - Ajouter la gestion des tournois</h3>
            <ol>
                <li>Permettre l'ajout d'un tournoi avec un formulaire.</li>
                <li>Ajouter le format : simple elimination, double elimination, round robin.</li>
                <li>Lier les joueurs à un tournoi au lieu d'avoir seulement des listes séparées.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 4 - Gérer les matchs</h3>
            <ol>
                <li>Créer les matchs automatiquement à partir des joueurs inscrits.</li>
                <li>Ajouter la saisie du score.</li>
                <li>Calculer le vainqueur automatiquement après saisie des résultats.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 5 - Passer à la base de données</h3>
            <ol>
                <li>Ajouter PostgreSQL.</li>
                <li>Créer les entités JPA : <strong>Joueur</strong>, <strong>Tournoi</strong>, <strong>Match</strong>.</li>
                <li>Créer un <strong>persistence.xml</strong> pour configurer la persistence unit et la connexion base de données.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 6 - Ajouter un vrai CRUD</h3>
            <ol>
                <li>Ajouter la modification d'un joueur.</li>
                <li>Ajouter la suppression d'un joueur.</li>
                <li>Faire pareil pour les tournois et les matchs.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 7 - Validation et erreurs</h3>
            <ol>
                <li>Vérifier les champs de formulaire côté serveur.</li>
                <li>Afficher des messages d'erreur clairs si un champ est vide ou invalide.</li>
                <li>Empêcher les doublons de pseudo ou les inscriptions incohérentes.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 8 - Authentification admin</h3>
            <ol>
                <li>Créer un <strong>Filter</strong> Jakarta qui vérifie la session sur les URLs <code>/admin/*</code>.</li>
                <li>Rediriger vers <code>/login</code> si l'utilisateur n'est pas ADMIN.</li>
                <li>Conserver les pages publiques en lecture seule pour les visiteurs.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 9 - Améliorer l'interface</h3>
            <ol>
                <li>Ajouter une vraie identité visuelle Street Fighter 6.</li>
                <li>Mettre un tableau de bord avec statistiques : nombre de joueurs, tournois, matchs joués.</li>
                <li>Améliorer l'affichage du bracket pour le rendre lisible.</li>
            </ol>
        </div>

        <div class="step">
            <h3>Étape 10 - Préparer une V2 propre</h3>
            <ol>
                <li>Passer des listes en mémoire à une persistance réelle.</li>
                <li>Séparer davantage la couche web et la couche métier.</li>
                <li>Préparer une base assez propre pour ajouter plus tard une API REST ou un frontend plus moderne.</li>
            </ol>
        </div>
    </section>

    <section class="card">
        <h2>Ordre conseillé pour toi</h2>
        <ol>
            <li>Terminer le CRUD des joueurs.</li>
            <li>Ajouter le CRUD des tournois.</li>
            <li>Créer la relation entre joueurs, tournois et matchs.</li>
            <li>Ajouter PostgreSQL + JPA.</li>
            <li>Nettoyer les JSP avec JSTL.</li>
            <li>Ajouter l'auth admin avec un Filter.</li>
            <li>Refaire l'interface visuelle.</li>
        </ol>
    </section>

    <section class="card">
        <h2>Petite méthode de travail</h2>
        <ul>
            <li>Faire une seule petite fonctionnalité à la fois.</li>
            <li>Tester dans Tomcat après chaque modification importante.</li>
            <li>Ne pas ajouter la base de données trop tôt si la partie web n'est pas stable.</li>
            <li>Garder le projet compilable à chaque étape.</li>
            <li>Commencer simple, puis refactoriser ensuite.</li>
        </ul>
    </section>

    <footer>
        Projet SF6 Tournoi - V1 en cours d'évolution
    </footer>
</main>
</body>
</html>