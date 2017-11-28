-- phpMyAdmin SQL Dump
-- version 4.7.4
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost
-- Généré le :  mar. 28 nov. 2017 à 21:41
-- Version du serveur :  10.1.28-MariaDB
-- Version de PHP :  7.1.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données :  `catalog`
--

-- --------------------------------------------------------

--
-- Structure de la table `file`
--

CREATE TABLE `file` (
  `name` varchar(100) CHARACTER SET utf8 NOT NULL,
  `size` bigint(20) UNSIGNED NOT NULL,
  `owner` varchar(50) CHARACTER SET utf8 NOT NULL,
  `access` enum('public','private') CHARACTER SET utf8 NOT NULL,
  `action` enum('read','write') CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Déchargement des données de la table `file`
--

INSERT INTO `file` (`name`, `size`, `owner`, `access`, `action`) VALUES
('23632112_1591179874312168_4765289876997081932_o.jpg', 233180, 'lucie', 'public', 'write'),
('23632291_1591179794312176_4279051922610823039_o.jpg', 217869, 'lucie', 'private', NULL),
('A1Lektion3.pdf', 1755248, 'lucie', 'public', 'write'),
('Baccalauréat.jpg', 897432, 'lucie', 'public', 'write'),
('Convention OUVRIER-BUFFET Lucie.pdf', 928437, 'lucie', 'private', NULL),
('Course description A1.pdf', 140059, 'test', 'private', NULL),
('Disability & Special needs.pdf', 257379, 'test', 'public', 'read'),
('FicheEntreeFloralis_Lucie_Ouvrier_Buffet.docx', 46374, 'lucie', 'public', 'write'),
('Group 2 members_.pdf', 85102, 'test', 'private', NULL),
('IMG_0657.jpg', 1989374, 'marine', 'public', 'write'),
('IMG_0665.jpg', 2510037, 'lucie', 'private', NULL),
('IMG_0964.jpg', 2616823, 'test', 'private', NULL),
('IMG_1013.jpg', 2704729, 'lucie', 'public', 'read'),
('sujet PFE.pdf', 376805, 'marine', 'public', 'write'),
('taxe habitation.rtf', 516, 'test', 'public', 'read');

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `username` varchar(50) CHARACTER SET utf8 NOT NULL,
  `password` varchar(100) CHARACTER SET utf8 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`username`, `password`) VALUES
('lucie', '318fbaf5acbf2b69a202de4c6df4cd2c'),
('marine', 'b329f324cc17d6221a385ea1afb3a289'),
('patrice', 'ffa75eb1a893fe631f92b6f3a9874ba5'),
('test', '098f6bcd4621d373cade4e832627b4f6');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `file`
--
ALTER TABLE `file`
  ADD PRIMARY KEY (`name`),
  ADD KEY `owner` (`owner`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`username`);

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `file`
--
ALTER TABLE `file`
  ADD CONSTRAINT `owner` FOREIGN KEY (`owner`) REFERENCES `user` (`username`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
