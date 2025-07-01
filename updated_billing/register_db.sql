-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 27, 2025 at 12:31 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `register_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`id`, `username`, `password`, `created_at`) VALUES
(1, 'shin', '456', '2025-06-13 11:28:50'),
(2, 'mao', '123', '2025-06-14 08:42:55');

-- --------------------------------------------------------

--
-- Table structure for table `discounts`
--

CREATE TABLE `discounts` (
  `discount_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `discount_type` varchar(50) DEFAULT NULL,
  `discount_value` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `discounts`
--

INSERT INTO `discounts` (`discount_id`, `event_id`, `discount_type`, `discount_value`) VALUES
(10, 42, 'Group Registration', 0.23);

-- --------------------------------------------------------

--
-- Table structure for table `events`
--

CREATE TABLE `events` (
  `event_id` int(11) NOT NULL,
  `event_name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL DEFAULT 0.00,
  `event_date` datetime NOT NULL,
  `venue` varchar(100) NOT NULL,
  `capacity` int(11) NOT NULL,
  `available_seats` int(11) NOT NULL DEFAULT 0,
  `category` varchar(50) DEFAULT NULL,
  `organizer_id` int(11) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `deactive_reason` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `events`
--

INSERT INTO `events` (`event_id`, `event_name`, `price`, `event_date`, `venue`, `capacity`, `available_seats`, `category`, `organizer_id`, `is_active`, `deactive_reason`) VALUES
(41, 'Free', 0.00, '2025-06-29 00:00:00', 'Padan', 100, 100, 'Workshops', 2, 1, NULL),
(42, 'Paider', 55.00, '2025-06-29 00:00:00', 'Premium Padan', 52, 52, 'Workshops', 2, 1, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `event_registration`
--

CREATE TABLE `event_registration` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `registration_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` varchar(20) DEFAULT 'pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `event_registration`
--

INSERT INTO `event_registration` (`id`, `event_id`, `user_id`, `registration_date`, `status`) VALUES
(58, 41, 28, '2025-06-26 16:00:00', 'pending'),
(59, 42, 27, '2025-06-26 16:00:00', 'pending');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `event_registration_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `notif_message` text NOT NULL,
  `notif_type` varchar(50) DEFAULT NULL,
  `recipient_type` enum('user','admin') NOT NULL,
  `recipient_user_id` int(11) DEFAULT NULL,
  `recipient_admin_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `event_registration_id`, `user_id`, `notif_message`, `notif_type`, `recipient_type`, `recipient_user_id`, `recipient_admin_id`) VALUES
(182, 58, 28, 'New registration for your event: Free Event', 'registration', 'admin', NULL, 2),
(183, 59, 27, 'New registration for your event: Paid', 'registration', 'admin', NULL, 2),
(184, 58, 28, 'Event name changed from \'Free Event\' to \'Free\'', 'event_name_change', 'user', 28, NULL),
(185, 58, 28, 'Event date changed from 2025-06-28 00:00:00 to 2025-06-29', 'event_date_change', 'user', 28, NULL),
(186, 58, 28, 'Event venue changed from \'Padang\' to \'Padan\'', 'event_venue_change', 'user', 28, NULL),
(187, 58, 28, 'Event capacity changed from 101 to 100', 'event_capacity_change', 'user', 28, NULL),
(188, 58, 28, 'Event category changed from \'Cultural Events\' to \'Workshops\'', 'event_category_change', 'user', 28, NULL),
(189, 59, 27, 'Event name changed from \'Paid\' to \'Paider\'', 'event_name_change', 'user', 27, NULL),
(190, 59, 27, 'Event price changed from RM50.00 to RM55.00', 'event_price_change', 'user', 27, NULL),
(191, 59, 27, 'Event date changed from 2025-06-28 00:00:00 to 2025-06-29', 'event_date_change', 'user', 27, NULL),
(192, 59, 27, 'Event venue changed from \'Premium Padang\' to \'Premium Padan\'', 'event_venue_change', 'user', 27, NULL),
(193, 59, 27, 'Event capacity changed from 50 to 52', 'event_capacity_change', 'user', 27, NULL),
(194, 59, 27, 'Event category changed from \'Sports Events\' to \'Workshops\'', 'event_category_change', 'user', 27, NULL),
(195, 59, 27, 'Additional services for the event have been updated', 'event_services_change', 'user', 27, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `services`
--

CREATE TABLE `services` (
  `service_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `service_name` varchar(100) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `services`
--

INSERT INTO `services` (`service_id`, `event_id`, `service_name`, `price`) VALUES
(10, 42, 'gawk', 10.00),
(11, 42, 'gawk gawk', 25.00);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `created_at`) VALUES
(27, 'User1', '1234', '2025-06-26 22:01:15'),
(28, 'User2', '123', '2025-06-26 22:02:43');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `discounts`
--
ALTER TABLE `discounts`
  ADD PRIMARY KEY (`discount_id`),
  ADD KEY `fk_discount_event` (`event_id`);

--
-- Indexes for table `events`
--
ALTER TABLE `events`
  ADD PRIMARY KEY (`event_id`);

--
-- Indexes for table `event_registration`
--
ALTER TABLE `event_registration`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `event_id_2` (`event_id`,`user_id`),
  ADD KEY `event_id` (`event_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `event_registration_id` (`event_registration_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `fk_notification_user` (`recipient_user_id`),
  ADD KEY `fk_notification_admin` (`recipient_admin_id`);

--
-- Indexes for table `services`
--
ALTER TABLE `services`
  ADD PRIMARY KEY (`service_id`),
  ADD KEY `fk_service_event` (`event_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin`
--
ALTER TABLE `admin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `discounts`
--
ALTER TABLE `discounts`
  MODIFY `discount_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `events`
--
ALTER TABLE `events`
  MODIFY `event_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT for table `event_registration`
--
ALTER TABLE `event_registration`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=60;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=196;

--
-- AUTO_INCREMENT for table `services`
--
ALTER TABLE `services`
  MODIFY `service_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `discounts`
--
ALTER TABLE `discounts`
  ADD CONSTRAINT `fk_discount_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`);

--
-- Constraints for table `event_registration`
--
ALTER TABLE `event_registration`
  ADD CONSTRAINT `event_registration_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_event_registration` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `fk_notification_admin` FOREIGN KEY (`recipient_admin_id`) REFERENCES `admin` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_notification_user` FOREIGN KEY (`recipient_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `services`
--
ALTER TABLE `services`
  ADD CONSTRAINT `fk_service_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
