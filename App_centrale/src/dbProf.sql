DROP SCHEMA IF EXISTS logiciel CASCADE;
CREATE SCHEMA logiciel;

CREATE TABLE logiciel.cours
(
    id_cours         SERIAL PRIMARY KEY,
    code_cours       VARCHAR(8) UNIQUE NOT NULL CHECK ( code_cours SIMILAR TO 'BINV[0-9]{4}'),
    nom              VARCHAR           NOT NULL CHECK (nom <> '' AND nom <> ' '),
    bloc             INTEGER           NOT NULL CHECK ( bloc >= 1 AND bloc <= 3 ),
    nombre_credits   INTEGER           NOT NULL CHECK ( nombre_credits > 0 ),
    nombre_etudiants INTEGER           NOT NULL DEFAULT 0
);

CREATE TABLE logiciel.projets
(
    num_projet         SERIAL PRIMARY KEY,
    identifiant_projet VARCHAR                                      NOT NULL UNIQUE CHECK ( identifiant_projet <> '' AND identifiant_projet <> ' '),
    nom                VARCHAR                                      NOT NULL CHECK (nom <> '' AND nom <> ' '),
    date_debut         DATE                                         NOT NULL DEFAULT current_date,
    date_fin           DATE                                         NOT NULL DEFAULT current_date,
    nombre_groupe      INTEGER                                      NOT NULL DEFAULT 0 CHECK (nombre_groupe >= 0),
    cours              INTEGER REFERENCES logiciel.cours (id_cours) NOT NULL,
    CHECK (date_fin > date_debut)
);

CREATE TABLE logiciel.etudiants
(
    id_etudiant SERIAL PRIMARY KEY,
    nom         VARCHAR NOT NULL CHECK (nom <> '' AND nom <> ' '),
    prenom      VARCHAR NOT NULL CHECK (prenom <> '' AND prenom <> ' '),
    mail        VARCHAR NOT NULL UNIQUE CHECK ( mail SIMILAR TO '%_@student.vinci.be'),
    pass_word   VARCHAR NOT NULL CHECK ( pass_word <> '' AND pass_word <> ' ')
);

CREATE TABLE logiciel.inscriptions_cours
(
    id_inscription_cours SERIAL PRIMARY KEY,
    cours                INTEGER REFERENCES logiciel.cours (id_cours)        NOT NULL,
    etudiant             INTEGER REFERENCES logiciel.etudiants (id_etudiant) NOT NULL,
    UNIQUE (cours, etudiant)
);

CREATE TABLE logiciel.groupes
(
    id_groupe       SERIAL PRIMARY KEY,
    num_groupe      INTEGER                                          NOT NULL                            DEFAULT 1,
    taille_groupe   INTEGER                                          NOT NULL CHECK ( taille_groupe > 0) DEFAULT 0,
    nombre_inscrits INTEGER                                          NOT NULL                            DEFAULT 0,
    valide          BOOLEAN                                          NOT NULL                            DEFAULT FALSE,
    complet         BOOLEAN                                          NOT NULL                            DEFAULT FALSE,
    projet          INTEGER REFERENCES logiciel.projets (num_projet) NOT NULL
);
CREATE TABLE logiciel.inscriptions_groupes
(
    id_inscription_groupe SERIAL PRIMARY KEY,
    etudiant              INTEGER REFERENCES logiciel.etudiants (id_etudiant) NOT NULL,
    groupe                INTEGER REFERENCES logiciel.groupes (id_groupe)     NOT NULL,
    projet                INTEGER REFERENCES logiciel.projets (num_projet)    NOT NULL,
    UNIQUE (etudiant, groupe)
);

-------------------------------------------------------------------
----Application centrale
-------------------------------------------------------------------

--1
CREATE FUNCTION logiciel.inserer_cours(_code_cours char(8), _nom VARCHAR, _bloc INTEGER, _nb_credits INTEGER)
    RETURNS VOID AS
$$
BEGIN
    _code_cours = UPPER(_code_cours);
    INSERT INTO logiciel.cours(code_cours, nom, bloc, nombre_credits)
    VALUES (_code_cours, _nom, _bloc, _nb_credits);
end;
$$ LANGUAGE plpgsql;

------------------------------------------------------------------------------------

--2
CREATE FUNCTION logiciel.inserer_etudiant(_nom VARCHAR, _prenom VARCHAR, _mail VARCHAR, _mdp VARCHAR)
    RETURNS VOID AS
$$
BEGIN
    INSERT INTO logiciel.etudiants(nom, prenom, mail, pass_word) VALUES (_nom, _prenom, _mail, _mdp);
end;
$$ LANGUAGE plpgsql;
------------------------------------------------------------------------------------

--3
CREATE FUNCTION logiciel.inscrire_etudiant_cours(_mail VARCHAR, _code_cours VARCHAR(8))
    RETURNS VOID AS
$$
DECLARE
    _etudiant INTEGER;
    _id_cours INTEGER;
BEGIN
    --recherche etudiant
    SELECT e.id_etudiant
    FROM logiciel.etudiants e
    WHERE e.mail = _mail
    INTO _etudiant;

    IF (_etudiant IS NULL) THEN
        RAISE 'mail inexistant dans la DB';
    end if;

    --rechercher code cours si existant alors on prend son id_cours
    SELECT c.id_cours
    FROM logiciel.cours c
    WHERE c.code_cours = _code_cours
    INTO _id_cours;

    IF (_id_cours IS NULL) THEN
        RAISE 'code cours invalide';
    end if;

    INSERT INTO logiciel.inscriptions_cours(cours, etudiant) VALUES (_id_cours, _etudiant);
end;
$$ LANGUAGE plpgsql;

----
CREATE FUNCTION logiciel.tester_inscription_cours()
    RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS(SELECT p.num_projet
              FROM logiciel.projets p
              WHERE p.cours = NEW.cours) THEN
        RAISE 'Trop tard ! Il y a deja des projets dans ce cours !';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_inscrire_etudiant_cours
    BEFORE INSERT
    on logiciel.inscriptions_cours
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.tester_inscription_cours();

--TRIGGER incrémenter champ etudiant
CREATE FUNCTION logiciel.incrementer_nb_etudiants()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE logiciel.cours c
    SET nombre_etudiants = nombre_etudiants + 1
    WHERE c.id_cours = NEW.cours;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_inc_nb_etudiant
    AFTER INSERT
    on logiciel.inscriptions_cours
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.incrementer_nb_etudiants();

--TRIGGER si groupe est complet
CREATE FUNCTION logiciel.groupe_est_complet()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE logiciel.groupes g
    SET complet = TRUE
    WHERE g.id_groupe = NEW.id_groupe;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_groupe_complet
    AFTER UPDATE OF nombre_inscrits
    on logiciel.groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.groupe_est_complet();

------------------------------------------------------------------------------------
--4
CREATE FUNCTION logiciel.inserer_projets(_identifiant_projet VARCHAR, _nom VARCHAR, _date_debut DATE, _date_fin DATE,
                                         _code_cours VARCHAR(8))
    RETURNS VOID AS
$$
DECLARE
    _cours INTEGER;
BEGIN
    SELECT c.id_cours
    FROM logiciel.cours c
    WHERE c.code_cours = _code_cours
    INTO _cours;

    IF (_cours IS NULL) THEN
        RAISE 'Code du cours inconnu !';
    end if;
    INSERT INTO logiciel.projets(identifiant_projet, nom, date_debut, date_fin, cours)
    VALUES (_identifiant_projet, _nom, _date_debut, _date_fin, _cours);
end;
$$ LANGUAGE plpgsql;

--------------------------------------------------------------------------------------------------------
--5

CREATE FUNCTION logiciel.creer_groupes(_identifiant_projet VARCHAR, _nombre_groupes INTEGER, _taille_groupe INTEGER)
    RETURNS VOID AS
$$
DECLARE
    i             INTEGER := 1;
    numero_projet INTEGER;
BEGIN
    SELECT p.num_projet
    FROM logiciel.projets p
    WHERE p.identifiant_projet = _identifiant_projet
    INTO numero_projet;

    IF (numero_projet IS NULL) THEN
        RAISE 'Projet inexistant !';
    end if;


    --si aucun groupe alors numero groupe vaut 1
    IF NOT EXISTS(SELECT *
                  FROM logiciel.groupes g,
                       logiciel.projets p
                  WHERE p.identifiant_projet = _identifiant_projet
                    AND g.projet = p.num_projet) THEN
        INSERT INTO logiciel.groupes(num_groupe, taille_groupe, projet)
        VALUES (1, _taille_groupe, numero_projet);
    END IF;
    i := i + 1;

    WHILE i < _nombre_groupes
        LOOP
            PERFORM logiciel.creer_un_groupe(_identifiant_projet, _taille_groupe);
            i := i + 1;
        end loop;

    UPDATE logiciel.projets p
    SET nombre_groupe = _nombre_groupes + nombre_groupe
    WHERE p.identifiant_projet = _identifiant_projet;
end;
$$ LANGUAGE plpgsql;

CREATE FUNCTION logiciel.test_capacite_groupe()
    RETURNS TRIGGER AS
$$
DECLARE
    nb_etudiant      INTEGER;
    nb_places_groupe INTEGER;
BEGIN
    SELECT c.nombre_etudiants
    FROM logiciel.projets p,
         logiciel.cours c
    WHERE p.num_projet = NEW.projet
      AND p.cours = c.id_cours
    INTO nb_etudiant;

    SELECT g.taille_groupe * p.nombre_groupe
    FROM logiciel.groupes g,
         logiciel.projets p
    WHERE g.projet = NEW.projet
      AND p.num_projet = g.projet
    LIMIT 1
    INTO nb_places_groupe;

    IF (nb_etudiant < nb_places_groupe) THEN
        RAISE 'Impossible de creer plus de groupe qu il n y a d etudiants ! ';
    end if;
    RETURN NEW;
end ;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_checker_creation_groupe
    BEFORE INSERT
    on logiciel.groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.test_capacite_groupe();

-- CREATE TRIGGER trigger_checker_nombre_groupe
--     BEFORE UPDATE OF nombre_groupe
--     on logiciel.projets
--     FOR EACH ROW
-- EXECUTE PROCEDURE logiciel.test_capacite_groupe();


CREATE FUNCTION logiciel.creer_un_groupe(_identifiant_projet VARCHAR, _taille_groupe INTEGER)
    RETURNS VOID AS
$$
DECLARE
    tulpe RECORD;
BEGIN
    --cherche le dernier tuple
    SELECT *
    FROM logiciel.groupes g,
         logiciel.projets p
    WHERE p.identifiant_projet = _identifiant_projet
      AND g.projet = p.num_projet
    ORDER BY g.num_groupe DESC
    LIMIT 1
    INTO tulpe;

    --incrémente num_groupe avant d'insérer
    INSERT INTO logiciel.groupes(num_groupe, taille_groupe, projet)
    VALUES (tulpe.num_groupe + 1, _taille_groupe, tulpe.projet);

end;
$$ LANGUAGE plpgsql;


--6 Visualiser les cours

CREATE VIEW logiciel.afficher_cours AS
SELECT c.code_cours                                                             as "Code du cours",
       c.nom                                                                    as "Nom du cours",
       COALESCE(string_agg(p.identifiant_projet, ', '), 'pas encore de projet') as "les projets"
FROM logiciel.cours c
         LEFT OUTER JOIN logiciel.projets p ON c.id_cours = p.cours
group by c.id_cours;


------------------------------------------------------------------------------------
--7 Visualiser tous les projets

CREATE FUNCTION logiciel.nb_complets(_groupe INTEGER)
    RETURNS INTEGER AS
$$
DECLARE
    nombre_complet INTEGER := 0;
BEGIN
    SELECT COUNT(g.complet)
    FROM logiciel.groupes g
    WHERE g.id_groupe = _groupe
      AND g.complet = TRUE
    INTO nombre_complet;
    RETURN nombre_complet;
end;
$$ LANGUAGE plpgsql;

CREATE FUNCTION logiciel.nb_valide(_groupe INTEGER)
    RETURNS INTEGER AS
$$
DECLARE
    nombre_valide INTEGER := 0;
BEGIN
    SELECT COUNT(g.valide)
    FROM logiciel.groupes g
    WHERE g.id_groupe = _groupe
      AND g.valide = TRUE
    INTO nombre_valide;
    RETURN nombre_valide;
end;
$$ LANGUAGE plpgsql;

CREATE VIEW logiciel.afficher_projets AS

SELECT p.identifiant_projet              as "Identifiant",
       p.nom                             as "Nom",
       c.code_cours                      as "Cours",
       p.nombre_groupe                   as "Nombre de groupe",
       logiciel.nb_complets(g.id_groupe) as "Nombre de groupe complets",
       logiciel.nb_valide(g.id_groupe)   as "Nombre de groupe validés"
FROM logiciel.projets p
         LEFT OUTER JOIN logiciel.cours c on c.id_cours = p.cours
         LEFT OUTER JOIN logiciel.groupes g on p.num_projet = g.projet
group by p.identifiant_projet, p.nom, c.code_cours, p.nombre_groupe, g.id_groupe;


------------------------------------------------------------------------------------
--8 Visualiser compositions de groupe d'un projet

CREATE VIEW logiciel.afficher_composition_groupe AS
SELECT p.num_projet,
       g.num_groupe as "Numéro",
       e.nom        as "Nom",
       e.prenom     as "Prénom",
       g.complet    as "Complet ?",
       g.valide     as "Validé ?"
FROM logiciel.projets p
         LEFT OUTER JOIN logiciel.groupes g on g.projet = p.num_projet
         LEFT OUTER JOIN logiciel.inscriptions_groupes ig on g.id_groupe = ig.groupe AND p.num_projet = ig.projet
         LEFT OUTER JOIN logiciel.etudiants e ON e.id_etudiant = ig.etudiant
ORDER BY g.num_groupe;

CREATE FUNCTION logiciel.chercher_id_projet(_identifiant_projet VARCHAR)
    RETURNS INTEGER AS
$$
DECLARE
    _id_projet INTEGER;
BEGIN
    SELECT p.num_projet
    FROM logiciel.projets p
    WHERE p.identifiant_projet = _identifiant_projet
    INTO _id_projet;

    IF (_id_projet IS NULL) THEN
        RAISE 'Identifiant du projet inexistant !';
    end if;
    RETURN _id_projet;
end;
$$ LANGUAGE plpgsql;
------------------------------------------------------------------------------------
--9 Valider un groupe

CREATE FUNCTION logiciel.valider_un_groupe(_identifiant_projet VARCHAR, _numero_groupe INTEGER)
    RETURNS BOOLEAN AS
$$
BEGIN
    PERFORM (logiciel.groupe_existe(_identifiant_projet, _numero_groupe));
    UPDATE logiciel.groupes g
    SET valide = TRUE
    WHERE g.num_groupe = _numero_groupe;
    RETURN TRUE;
end;
$$ LANGUAGE plpgsql;

CREATE FUNCTION logiciel.groupe_existe(_identifiant_projet VARCHAR, _numero_groupe INTEGER)
    RETURNS VOID AS
$$
DECLARE
    _id_groupe INTEGER;
BEGIN
    SELECT g.id_groupe
    FROM logiciel.groupes g,
         logiciel.projets p
    WHERE g.num_groupe = _numero_groupe
      AND p.num_projet = g.projet
      AND p.identifiant_projet = _identifiant_projet
    INTO _id_groupe;

    IF (_id_groupe IS NULL) THEN
        RAISE 'Groupe inexistant';
    end if;

end;
$$ LANGUAGE plpgsql;
----------

CREATE FUNCTION logiciel.check_valider_groupe()
    RETURNS TRIGGER AS
$$
DECLARE
    taille_gr  INTEGER;
    nb_inscrit INTEGER;
BEGIN
    SELECT g.taille_groupe
    FROM logiciel.groupes g
    WHERE g.projet = OLD.projet
    INTO taille_gr;

    SELECT g.nombre_inscrits
    FROM logiciel.groupes g
    WHERE g.projet = OLD.projet
    INTO nb_inscrit;

    IF (taille_gr > nb_inscrit) THEN
        RAISE 'Impossible de valider le groupe car il n est pas complet !';
    end if;
    RETURN NEW;
end ;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_valider_groupe
    BEFORE UPDATE OF valide
    on logiciel.groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.check_valider_groupe();

------------------------------------------------------------------------------------
--10 Valider tous les groupes d'un projet

CREATE FUNCTION logiciel.valider_tous_les_groupes(_identifiant_projet VARCHAR)
    RETURNS VOID AS
$$
DECLARE
    record RECORD;
BEGIN
    FOR record IN SELECT *
                  FROM logiciel.projets p,
                       logiciel.groupes g
                  WHERE p.identifiant_projet = _identifiant_projet
                    AND p.num_projet = g.projet
        LOOP
            PERFORM logiciel.valider_un_groupe(record.identifiant_projet, record.num_groupe);
        end loop;
end;
$$ language plpgsql;