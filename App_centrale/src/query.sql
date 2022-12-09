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
    UNIQUE (etudiant, projet)
);



--------------------------------------------------------------------
------------------------Application centrale------------------------
--------------------------------------------------------------------

--1
CREATE FUNCTION logiciel.inserer_cours(_code_cours char(8), _nom VARCHAR, _bloc INTEGER, _nb_credits INTEGER)
    RETURNS VOID AS
$$
BEGIN
    INSERT INTO logiciel.cours(code_cours, nom, bloc, nombre_credits)
    VALUES (UPPER(_code_cours), _nom, _bloc, _nb_credits);
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
    SELECT logiciel.chercher_id_etudiant(_mail) INTO _etudiant;

    --rechercher code cours si existant alors on prend son id_cours
    SELECT c.id_cours
    FROM logiciel.cours c
    WHERE c.code_cours = _code_cours
    INTO _id_cours;

    IF (_id_cours IS NULL) THEN
        RAISE 'code cours invalide';
    end if;

    INSERT INTO logiciel.inscriptions_cours(cours, etudiant)
    VALUES (_id_cours, _etudiant);
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

CREATE OR REPLACE FUNCTION logiciel.creer_groupes(_num_projet INTEGER, _nombre_groupes INTEGER,
                                                  _taille_groupe INTEGER)
    RETURNS VOID AS
$$
DECLARE
    i            INTEGER := 1;
    _nb_etudiant INTEGER;
    _nb_groupe   INTEGER;
BEGIN
    IF(_nombre_groupes = 0 or _taille_groupe = 0) THEN
        RAISE 'Veuillez entrer des nombres strictement positifs';
    END IF;


    SELECT c.nombre_etudiants
    FROM logiciel.projets p,
         logiciel.cours c
    WHERE p.num_projet = _num_projet
      AND p.cours = c.id_cours
    INTO _nb_etudiant;

    SELECT p.nombre_groupe
    FROM logiciel.projets p
    WHERE p.num_projet = _num_projet
    INTO _nb_groupe;

    IF ((_nb_etudiant < (_nombre_groupes + _nb_groupe)) OR ((_nombre_groupes * _taille_groupe) > _nb_etudiant)) THEN
        RAISE 'Impossible de créer plus de groupe que le nombre d étudiant inscrits';
    end if;

    WHILE i <= _nombre_groupes
        LOOP
            PERFORM logiciel.creer_un_groupe(_num_projet, _taille_groupe);
            i := i + 1;
        end loop;

    UPDATE logiciel.projets p
    SET nombre_groupe = _nombre_groupes + nombre_groupe --<<<<<<<<<--------- mettre dans un trigger
    WHERE p.num_projet = _num_projet;
end;
$$ LANGUAGE plpgsql;

CREATE FUNCTION logiciel.test_capacite_groupe()
    RETURNS TRIGGER AS
$$
DECLARE
    _nb_etudiant_cours                 INTEGER;
    _nb_etudiant_inscrits_actuellement INTEGER;
BEGIN
    SELECT c.nombre_etudiants
    FROM logiciel.projets p,
         logiciel.cours c
    WHERE p.num_projet = NEW.projet
      AND p.cours = c.id_cours
    INTO _nb_etudiant_cours;

    SELECT SUM(g.nombre_inscrits)
    FROM logiciel.projets p,
         logiciel.groupes g
    WHERE p.num_projet = NEW.projet
      AND p.num_projet = g.projet
    INTO _nb_etudiant_inscrits_actuellement;

    IF (_nb_etudiant_inscrits_actuellement = _nb_etudiant_cours) THEN
        RAISE 'Impossible de créer plus de groupe car c est complet ';
    end if;

    --     SELECT SUM(g.taille_groupe * p.nombre_groupe)
--     FROM logiciel.groupes g,
--          logiciel.projets p
--     WHERE g.projet = NEW.projet
--       AND p.num_projet = g.projet
--     INTO nb_places_groupe;

--     IF (_nb_etudiant < _nb_places_groupe) THEN
--         RAISE 'Impossible de créer autant de groupe que d étudiant inscrit ! ';
--     end if;
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


CREATE FUNCTION logiciel.creer_un_groupe(_num_projet INTEGER, _taille_groupe INTEGER)
    RETURNS VOID AS
$$
DECLARE
    tuple RECORD;
BEGIN
    --si aucun groupe alors numero groupe vaut 1
    IF NOT EXISTS(SELECT *
                  FROM logiciel.groupes g,
                       logiciel.projets p
                  WHERE p.num_projet = _num_projet
                    AND g.projet = p.num_projet) THEN
        INSERT INTO logiciel.groupes(num_groupe, taille_groupe, projet)
        VALUES (1, _taille_groupe, _num_projet);
    END IF;

    --cherche le dernier tuple
    SELECT g.*
    FROM logiciel.groupes g,
         logiciel.projets p
    WHERE p.num_projet = _num_projet
      AND g.projet = p.num_projet
    ORDER BY g.num_groupe DESC
    LIMIT 1
    INTO tuple;

    --incrémente num_groupe avant d'insérer
    INSERT INTO logiciel.groupes(num_groupe, taille_groupe, projet)
    VALUES (tuple.num_groupe + 1, _taille_groupe, tuple.projet);

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

SELECT DISTINCT p.identifiant_projet              as "Identifiant",
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

CREATE FUNCTION logiciel.valider_un_groupe(_num_projet INTEGER, _numero_groupe INTEGER)
    RETURNS BOOLEAN AS
$$
BEGIN
    UPDATE logiciel.groupes g
    SET valide = TRUE
    WHERE g.num_groupe = _numero_groupe
      AND g.projet = _num_projet;
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
    record      RECORD;
    _num_projet INTEGER;
BEGIN
    SELECT logiciel.chercher_id_projet(_identifiant_projet) INTO _num_projet;

    FOR record IN SELECT *
                  FROM logiciel.projets p,
                       logiciel.groupes g
                  WHERE p.num_projet = _num_projet
                    AND p.num_projet = g.projet
                    AND g.valide = FALSE
        LOOP
            PERFORM logiciel.valider_un_groupe(_num_projet, record.num_groupe);
        end loop;
end;
$$ language plpgsql;


-------------------------------------------------------------------
-------------------------------------------------------------------
-----------------------Application etudiant------------------------
-------------------------------------------------------------------
-------------------------------------------------------------------

CREATE OR REPLACE FUNCTION logiciel.chercher_id_etudiant(_mail VARCHAR)
    RETURNS INTEGER AS
$$
DECLARE
    _id_etudiant INTEGER;
BEGIN
    SELECT e.id_etudiant
    FROM logiciel.etudiants e
    WHERE e.mail = _mail
    INTO _id_etudiant;

    IF (_id_etudiant IS NULL) then
        RAISE 'mail inexistant dans la DB ';
    end if;

    RETURN _id_etudiant;
end;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION logiciel.recuperer_mdp_etudiant(_id_etudiant INTEGER)
    RETURNS VARCHAR AS
$$
DECLARE
    mdp_etudiant VARCHAR;
BEGIN
    SELECT e.pass_word
    FROM logiciel.etudiants e
    WHERE e.id_etudiant = _id_etudiant
    INTO mdp_etudiant;

    RETURN mdp_etudiant;
end;
$$ LANGUAGE plpgsql;


--------------------------------------------------------------------------1
CREATE OR REPLACE VIEW logiciel.afficher_mes_cours AS
SELECT ic.etudiant                                                              as "Etudiant",
       c.code_cours                                                             as "Code cours",
       c.nom                                                                    as "Nom",
       COALESCE(string_agg(p.identifiant_projet, ', '), 'pas encore de projet') as les_projets
FROM logiciel.cours c
         LEFT OUTER JOIN logiciel.projets p on c.id_cours = p.cours
         LEFT OUTER JOIN logiciel.inscriptions_cours ic on c.id_cours = ic.cours
group by c.nom, c.code_cours, ic.etudiant;

--------------------------------------------------------------------------2
CREATE OR REPLACE FUNCTION logiciel.inscrire_etudiant_groupe(_etudiant INTEGER, _num_groupe INTEGER, _identifiant_projet VARCHAR)
    RETURNS BOOLEAN AS
$$
DECLARE
    _num_projet INTEGER;
    _id_groupe  INTEGER;
BEGIN
    -- SELECT logiciel.chercher_id_projet(_identifiant_projet) INTO _num_projet;
    -- SELECT logiciel.groupe_existe(_identifiant_projet, _num_groupe) INTO _id_groupe;
    SELECT p.num_projet
    FROM logiciel.projets p
    WHERE p.identifiant_projet = _identifiant_projet
    INTO _num_projet;

    IF (_num_projet IS NULL) THEN
        RAISE 'identifiant projet inexistant !';
    end if;

    SELECT g.id_groupe
    FROM logiciel.groupes g
    WHERE g.num_groupe = _num_groupe
      AND g.projet = _num_projet
    INTO _id_groupe;

    IF (_id_groupe IS NULL) THEN
        RAISE 'Numéro de groupe inexistant';
    end if;

    INSERT INTO logiciel.inscriptions_groupes(etudiant, groupe, projet)
    VALUES (_etudiant, _id_groupe, _num_projet);
    RETURN TRUE;
end;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION logiciel.deja_inscrits()
    RETURNS TRIGGER AS
$$
BEGIN
    IF EXISTS(SELECT ig.id_inscription_groupe
              FROM logiciel.inscriptions_groupes ig
              WHERE ig.projet = NEW.projet
                AND ig.etudiant = NEW.etudiant) THEN
        RAISE 'Vous êtes déjà inscrit dans un groupe pour ce projet !';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_etudiant_deja_inscrit_groupe
    BEFORE INSERT
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.deja_inscrits();

CREATE OR REPLACE FUNCTION logiciel.incrementer_nb_inscrits()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE logiciel.groupes g
    SET nombre_inscrits = g.nombre_inscrits + 1
    WHERE g.id_groupe = NEW.groupe
      AND g.projet = NEW.projet;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_incrementer_nb_etudiant_dans_groupe
    AFTER INSERT
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.incrementer_nb_inscrits();

CREATE OR REPLACE FUNCTION logiciel.check_groupe_complet()
    RETURNS TRIGGER AS
$$
DECLARE
    taille_gr  INTEGER;
    nb_inscrit INTEGER;
BEGIN
    SELECT g.taille_groupe
    FROM logiciel.groupes g
    WHERE g.id_groupe = NEW.groupe
    INTO taille_gr;

    SELECT g.nombre_inscrits
    FROM logiciel.groupes g
    WHERE g.id_groupe = NEW.groupe
    INTO nb_inscrit;

    IF (taille_gr = nb_inscrit) THEN
        UPDATE logiciel.groupes g
        SET complet = TRUE
        WHERE g.num_groupe = NEW.groupe;
    end if;
    RETURN NEW;
end ;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_groupe_complet
    AFTER INSERT
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.check_groupe_complet();


CREATE OR REPLACE FUNCTION logiciel.taille_groupe()
    RETURNS TRIGGER AS
$$
DECLARE
    nb_inscrits   INTEGER;
    taille_groupe INTEGER;
BEGIN
    SELECT g.nombre_inscrits
    FROM logiciel.groupes g
    WHERE g.id_groupe = NEW.groupe
    INTO nb_inscrits;

    SELECT g.taille_groupe
    FROM logiciel.groupes g
    WHERE g.id_groupe = NEW.groupe
    INTO taille_groupe;

    IF (nb_inscrits = taille_groupe) THEN
        RAISE 'Groupe déjà complet';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_place_disponible_groupe
    BEFORE INSERT
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.taille_groupe();

--------------------------------------------------------------------------3 - le monstre buggé





CREATE OR REPLACE FUNCTION logiciel.retirer_etudiant(_etudiant INTEGER, _identifiant_projet VARCHAR)
    RETURNS BOOLEAN AS
$$
DECLARE
    _num_projet INTEGER;
BEGIN
    SELECT logiciel.chercher_id_projet(_identifiant_projet) INTO _num_projet;

    DELETE
    FROM logiciel.inscriptions_groupes ig
    WHERE ig.etudiant = _etudiant
      AND ig.projet = _num_projet;

    RETURN TRUE;
end;
$$ LANGUAGE plpgsql;






--TRIGGER décrémenter nb etudiant
CREATE OR REPLACE FUNCTION logiciel.decrementer_nb_etudiants()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE logiciel.groupes g
    SET nombre_inscrits = nombre_inscrits - 1,
        complet         = FALSE
    WHERE g.id_groupe = OLD.groupe;
    RETURN OLD;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_dec_nb_etudiant
    AFTER DELETE
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.decrementer_nb_etudiants();






CREATE OR REPLACE FUNCTION logiciel.groupe_deja_valide()
    RETURNS TRIGGER AS
$$
DECLARE
    est_valide BOOLEAN;
BEGIN

    IF (SELECT g.valide
        FROM logiciel.groupes g,
             logiciel.inscriptions_groupes ig
        WHERE OLD.id_inscription_groupe = ig.id_inscription_groupe
          AND ig.groupe = g.id_groupe) THEN
        RAISE 'Le groupe est déjà validé, impossible de quitter le groupe';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_est_groupe_valide
    BEFORE DELETE
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.groupe_deja_valide();






CREATE OR REPLACE FUNCTION logiciel.etudiant_est_dans_groupe()
    RETURNS TRIGGER AS
$$
DECLARE
    _id_etudiant INTEGER;
BEGIN
    SELECT ig.etudiant
    FROM logiciel.inscriptions_groupes ig
    WHERE ig.id_inscription_groupe = OLD.id_inscription_groupe
    INTO _id_etudiant;

    IF (_id_etudiant IS NULL) THEN
        RAISE 'Vous n êtes pas inscrit dans ce groupe de ce projet';
    end if;
    RETURN NEW;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_etudiant_est_dans_groupe
    BEFORE DELETE
    on logiciel.inscriptions_groupes
    FOR EACH ROW
EXECUTE PROCEDURE logiciel.etudiant_est_dans_groupe();






--------------------------------------------------------------------------4 Visualiser tous les projets des cours inscrits
CREATE OR REPLACE VIEW logiciel.afficher_lesProjets_d_etudiant AS
SELECT ig.etudiant          as id_etudiant,
       p.identifiant_projet as Identifiant_projet,
       p.nom                as Nom,
       p.cours              as Identifiant_cours,
       g.num_groupe         as Num_groupe
FROM logiciel.projets p
         LEFT OUTER JOIN logiciel.groupes g ON p.num_projet = g.projet
         LEFT OUTER JOIN logiciel.inscriptions_groupes ig ON g.id_groupe = ig.groupe
ORDER BY p.identifiant_projet;


--------------------------------------------------------------------------5 Visualiser tous les projets où il n'a pas de groupe

CREATE OR REPLACE VIEW logiciel.afficher_projets_pas_encore_de_groupe AS
SELECT DISTINCT ic.etudiant          as "id etudiant",
                p.identifiant_projet as "Identifiant projet",
                p.nom                as "Nom",
                c.code_cours         as "Identifiant cours",
                p.date_debut         as "Date début",
                p.date_fin           as "Date fin"
FROM logiciel.projets p
         LEFT OUTER JOIN logiciel.groupes g ON p.num_projet = g.projet
         LEFT OUTER JOIN logiciel.cours c ON p.cours = c.id_cours
         LEFT OUTER JOIN logiciel.inscriptions_cours ic on c.id_cours = ic.cours
WHERE NOT EXISTS(SELECT ig.groupe
                 FROM logiciel.inscriptions_groupes ig
                 WHERE ig.projet = p.num_projet
                   AND ig.etudiant = ic.etudiant
                 ORDER BY p.cours);


--------------------------------------------------------------------------6 Visualiser toutes les compositions de groupes incomplets d'un projet

CREATE OR REPLACE VIEW logiciel.afficher_composition_groupes_incomplets AS
SELECT e.id_etudiant,
       g.num_groupe                        as "Num groupe",
       p.identifiant_projet                as "Identifiant projet",
       e.nom                               as "Nom",
       e.prenom                            as "Prénom",
       g.taille_groupe - g.nombre_inscrits as "Nombre de place restantes"

FROM logiciel.projets p,
     logiciel.etudiants e,
     logiciel.groupes g,
     logiciel.inscriptions_groupes ig
WHERE p.num_projet = g.projet
  AND g.valide = FALSE
  AND g.complet = FALSE
  AND e.id_etudiant = ig.etudiant
  AND ig.projet = p.num_projet
group by p.identifiant_projet, g.num_groupe, e.nom, e.prenom, g.taille_groupe, g.nombre_inscrits, e.id_etudiant
ORDER BY g.num_groupe;




--------------------------------------------
------------DEMO---------------------------- à supprimer avant de soumettre

SELECT logiciel.inserer_cours('BINV2040', 'BD2', 2, 6);
SELECT logiciel.inserer_cours('BINV1020', 'APOO', 1, 6);
INSERT INTO logiciel.etudiants(nom, prenom, mail, pass_word)
VALUES ('Damas', 'Christophe', 'cd@student.vinci.be', '$2a$10$Z1UzzMyxT.V4sOHuJAyan.X3v.zFB4pqVDy5zsftTZwvSR2rpHqKK');
INSERT INTO logiciel.etudiants(nom, prenom, mail, pass_word)
VALUES ('Ferneeuw', 'Stephanie', 'sf@student.vinci.be', '$2a$10$Z1UzzMyxT.V4sOHuJAyan.X3v.zFB4pqVDy5zsftTZwvSR2rpHqKK');
SELECT logiciel.inscrire_etudiant_cours('cd@student.vinci.be', 'BINV2040');
SELECT logiciel.inscrire_etudiant_cours('sf@student.vinci.be', 'BINV2040');
SELECT logiciel.inserer_projets('projSQL', 'projet SQL', '2023-09-10', '2023-12-15', 'BINV2040');
SELECT logiciel.inserer_projets('dsd', 'DSD', '2023-09-30', '2023-12-01', 'BINV1020');
SELECT logiciel.creer_groupes(1, 1, 2);
SELECT logiciel.inscrire_etudiant_groupe(1, 1, 'projSQL');
SELECT logiciel.inscrire_etudiant_groupe(2, 1, 'projSQL');
SELECT logiciel.inserer_cours('BINV2140', 'SD2', 2, 3);
INSERT INTO logiciel.etudiants(nom, prenom, mail, pass_word)
VALUES ('Cambron', 'Isabelle', 'ic@student.vinci.be', '$2a$10$Z1UzzMyxT.V4sOHuJAyan.X3v.zFB4pqVDy5zsftTZwvSR2rpHqKK');

--SELECT logiciel.inscrire_etudiant_cours('ic@student.vinci.be', 'BINV2040');

SELECT logiciel.inscrire_etudiant_cours('ic@student.vinci.be', 'BINV2140');
SELECT logiciel.inscrire_etudiant_cours('sf@student.vinci.be', 'BINV2140');
SELECT logiciel.inscrire_etudiant_cours('cd@student.vinci.be', 'BINV2140');
SELECT logiciel.inserer_projets('projSD', 'projet SD2', '2023-03-01', '2023-04-01', 'BINV2140');

--SELECT logiciel.creer_groupes(3, 2, 2);

SELECT logiciel.creer_groupes(3, 1, 1);
SELECT logiciel.creer_groupes(3, 1, 2);

--SELECT logiciel.creer_groupes(3, 3, 1);






-------------------------------------------------------------------
-------------------------------User--------------------------------
-------------------------------------------------------------------

GRANT CREATE ON SCHEMA logiciel TO PUBLIC;
GRANT ALL ON DATABASE postgres TO PUBLIC;

/*
REVOKE CREATE ON SCHEMA logiciel FROM PUBLIC;
REVOKE ALL ON DATABASE postgres FROM PUBLIC;

DROP USER IF EXISTS mariammiclauri, chehrazadouazzani;
GRANT CONNECT ON DATABASE postgres TO chehrazadouazzani;
GRANT USAGE ON SCHEMA logiciel TO chehrazadouazzani;

GRANT CONNECT ON DATABASE postgres TO mariammiclauri;
GRANT USAGE ON SCHEMA logiciel TO mariammiclauri;
GRANT SELECT ON logiciel.projets, logiciel.cours, logiciel.etudiants, logiciel.groupes TO mariammiclauri;
GRANT INSERT ON logiciel.inscriptions_groupes TO mariammiclauri;


ALTER FUNCTION logiciel.inscrire_etudiant_groupe(integer, integer, varchar) SECURITY DEFINER SET search_path = public;
ALTER FUNCTION logiciel.recuperer_mdp_etudiant(_id_etudiant INTEGER) SECURITY DEFINER SET search_path = public;
ALTER FUNCTION logiciel.retirer_etudiant(integer, varchar) SECURITY DEFINER SET search_path = public;
ALTER FUNCTION logiciel.chercher_id_projet(varchar) SECURITY DEFINER SET search_path = public;
*/

SELECT DISTINCT p.identifiant_projet              as "Identifiant",
                p.nom                             as "Nom",
                c.code_cours                      as "Cours",
                p.nombre_groupe                   as "Nombre de groupe",
                logiciel.nb_complets(g.id_groupe) as "Nombre de groupe complets",
                logiciel.nb_valide(g.id_groupe)   as "Nombre de groupe validés"
FROM logiciel.projets p
         LEFT OUTER JOIN logiciel.cours c on c.id_cours = p.cours
         LEFT OUTER JOIN logiciel.groupes g on p.num_projet = g.projet
group by p.identifiant_projet, p.nom, c.code_cours, p.nombre_groupe, g.id_groupe;