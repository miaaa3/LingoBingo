-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 03, 2025 at 03:46 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `quizdb`
--

-- --------------------------------------------------------

--
-- Table structure for table `flashcards`
--

CREATE TABLE `flashcards` (
  `flashcard_set_id` bigint(20) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `definition` varchar(255) NOT NULL,
  `term` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `flashcard_sets`
--

CREATE TABLE `flashcard_sets` (
  `is_public` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `games`
--

CREATE TABLE `games` (
  `is_active` bit(1) DEFAULT NULL,
  `number_of_questions` int(11) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `flashcard_set_id` bigint(20) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `quiz_id` bigint(20) DEFAULT NULL,
  `teacher_id` bigint(20) NOT NULL,
  `game_code` varchar(255) DEFAULT NULL,
  `question_type` enum('TERM','DEFINITION','RANDOM') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `game_players`
--

CREATE TABLE `game_players` (
  `game_id` bigint(20) NOT NULL,
  `player_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `game_player_current_question_index`
--

CREATE TABLE `game_player_current_question_index` (
  `current_question_index` int(11) DEFAULT NULL,
  `game_id` bigint(20) NOT NULL,
  `player_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `game_player_finish_times`
--

CREATE TABLE `game_player_finish_times` (
  `finish_time` bigint(20) DEFAULT NULL,
  `game_id` bigint(20) NOT NULL,
  `player_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `game_player_question_ids`
--

CREATE TABLE `game_player_question_ids` (
  `game_id` bigint(20) NOT NULL,
  `player_id` bigint(20) NOT NULL,
  `question_ids` varbinary(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `game_player_scores`
--

CREATE TABLE `game_player_scores` (
  `score` int(11) DEFAULT NULL,
  `game_id` bigint(20) NOT NULL,
  `player_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `players`
--

CREATE TABLE `players` (
  `id` bigint(20) NOT NULL,
  `username` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `questions`
--

CREATE TABLE `questions` (
  `question_id` bigint(20) NOT NULL,
  `quiz_id` bigint(20) DEFAULT NULL,
  `correct_answer` varchar(255) DEFAULT NULL,
  `explanation` varchar(255) DEFAULT NULL,
  `question_description` varchar(255) DEFAULT NULL,
  `question_text` varchar(255) DEFAULT NULL,
  `tip` varchar(255) DEFAULT NULL,
  `user_answer` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `question_answers`
--

CREATE TABLE `question_answers` (
  `question_id` bigint(20) NOT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `answer_option` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `quizzes`
--

CREATE TABLE `quizzes` (
  `is_public` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `recent_activities`
--

CREATE TABLE `recent_activities` (
  `flashcard_set_id` bigint(20) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `quiz_id` bigint(20) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` enum('QUIZ','FLASHCARD_SET') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `role` enum('ADMIN','USER') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`created_at`, `id`, `email`, `name`, `password`, `username`, `role`) VALUES
('2025-06-03 15:45:38.000000', 1, 'admin@quizit.com', 'admin', '$2a$10$9xCN3GAuCeAG8uikdwzlOuQ/ACTn5B0Wpar/4oTYacKjSb.hbUrDe', 'admin', 'USER');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `flashcards`
--
ALTER TABLE `flashcards`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKob8kpup4qkh3734j7imlsl2hu` (`flashcard_set_id`);

--
-- Indexes for table `flashcard_sets`
--
ALTER TABLE `flashcard_sets`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKrih1wcm7pm84ei97mdxjvpaek` (`user_id`);

--
-- Indexes for table `games`
--
ALTER TABLE `games`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKrwe19rndk5bd25bvf9epkwayv` (`teacher_id`),
  ADD KEY `FKgs1v77003xw4wdq1cinj1i160` (`flashcard_set_id`),
  ADD KEY `FKb7edmbejggwvotxcbyybyoei0` (`quiz_id`);

--
-- Indexes for table `game_players`
--
ALTER TABLE `game_players`
  ADD KEY `FKjltki2cjqrxb27qnpe0bb36v6` (`player_id`),
  ADD KEY `FKrbr2flqdav5ovyjas7q92u64r` (`game_id`);

--
-- Indexes for table `game_player_current_question_index`
--
ALTER TABLE `game_player_current_question_index`
  ADD PRIMARY KEY (`game_id`,`player_id`);

--
-- Indexes for table `game_player_finish_times`
--
ALTER TABLE `game_player_finish_times`
  ADD PRIMARY KEY (`game_id`,`player_id`);

--
-- Indexes for table `game_player_question_ids`
--
ALTER TABLE `game_player_question_ids`
  ADD PRIMARY KEY (`game_id`,`player_id`);

--
-- Indexes for table `game_player_scores`
--
ALTER TABLE `game_player_scores`
  ADD PRIMARY KEY (`game_id`,`player_id`);

--
-- Indexes for table `players`
--
ALTER TABLE `players`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_p1chj5w2v1nune5xmmd94u1yj` (`username`);

--
-- Indexes for table `questions`
--
ALTER TABLE `questions`
  ADD PRIMARY KEY (`question_id`),
  ADD KEY `FKn3gvco4b0kewxc0bywf1igfms` (`quiz_id`);

--
-- Indexes for table `question_answers`
--
ALTER TABLE `question_answers`
  ADD PRIMARY KEY (`question_id`,`answer_option`);

--
-- Indexes for table `quizzes`
--
ALTER TABLE `quizzes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK1qg4klxor82vbamctkjechwe2` (`created_by`);

--
-- Indexes for table `recent_activities`
--
ALTER TABLE `recent_activities`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKnm0tjsjfn2qb9txb5hteir58` (`flashcard_set_id`),
  ADD KEY `FKf2pgenxmpd2r31nqv0yj32w3w` (`quiz_id`),
  ADD KEY `FKalbvtuw0jxk70bu4wi7s796h5` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  ADD UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `flashcards`
--
ALTER TABLE `flashcards`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `flashcard_sets`
--
ALTER TABLE `flashcard_sets`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `games`
--
ALTER TABLE `games`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `players`
--
ALTER TABLE `players`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `questions`
--
ALTER TABLE `questions`
  MODIFY `question_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `quizzes`
--
ALTER TABLE `quizzes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `recent_activities`
--
ALTER TABLE `recent_activities`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `flashcards`
--
ALTER TABLE `flashcards`
  ADD CONSTRAINT `FKob8kpup4qkh3734j7imlsl2hu` FOREIGN KEY (`flashcard_set_id`) REFERENCES `flashcard_sets` (`id`);

--
-- Constraints for table `flashcard_sets`
--
ALTER TABLE `flashcard_sets`
  ADD CONSTRAINT `FKrih1wcm7pm84ei97mdxjvpaek` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `games`
--
ALTER TABLE `games`
  ADD CONSTRAINT `FKb7edmbejggwvotxcbyybyoei0` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`),
  ADD CONSTRAINT `FKgs1v77003xw4wdq1cinj1i160` FOREIGN KEY (`flashcard_set_id`) REFERENCES `flashcard_sets` (`id`),
  ADD CONSTRAINT `FKrwe19rndk5bd25bvf9epkwayv` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `game_players`
--
ALTER TABLE `game_players`
  ADD CONSTRAINT `FKjltki2cjqrxb27qnpe0bb36v6` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`),
  ADD CONSTRAINT `FKrbr2flqdav5ovyjas7q92u64r` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`);

--
-- Constraints for table `game_player_current_question_index`
--
ALTER TABLE `game_player_current_question_index`
  ADD CONSTRAINT `FK9s0cd9uub6ce58inogse55308` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`);

--
-- Constraints for table `game_player_finish_times`
--
ALTER TABLE `game_player_finish_times`
  ADD CONSTRAINT `FKl6uitaf02kdy7vlbeu4qpahcx` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`);

--
-- Constraints for table `game_player_question_ids`
--
ALTER TABLE `game_player_question_ids`
  ADD CONSTRAINT `FK9bw7gehxke70pkk4xhn468riw` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`);

--
-- Constraints for table `game_player_scores`
--
ALTER TABLE `game_player_scores`
  ADD CONSTRAINT `FKcfgfnb5xpg5hxf9s87djbhpye` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`);

--
-- Constraints for table `questions`
--
ALTER TABLE `questions`
  ADD CONSTRAINT `FKn3gvco4b0kewxc0bywf1igfms` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`);

--
-- Constraints for table `question_answers`
--
ALTER TABLE `question_answers`
  ADD CONSTRAINT `FKrms3u35c10orgjqyw03ajd7x7` FOREIGN KEY (`question_id`) REFERENCES `questions` (`question_id`);

--
-- Constraints for table `quizzes`
--
ALTER TABLE `quizzes`
  ADD CONSTRAINT `FK1qg4klxor82vbamctkjechwe2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`);

--
-- Constraints for table `recent_activities`
--
ALTER TABLE `recent_activities`
  ADD CONSTRAINT `FKalbvtuw0jxk70bu4wi7s796h5` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKf2pgenxmpd2r31nqv0yj32w3w` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`),
  ADD CONSTRAINT `FKnm0tjsjfn2qb9txb5hteir58` FOREIGN KEY (`flashcard_set_id`) REFERENCES `flashcard_sets` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
