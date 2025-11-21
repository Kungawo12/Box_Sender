-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3307
-- Generation Time: Nov 01, 2025 at 09:43 PM
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
-- Database: `boxsender`
--

-- --------------------------------------------------------

--
-- Table structure for table `activity_log`
--

CREATE TABLE `activity_log` (
  `id` int(11) NOT NULL,
  `package_id` int(11) NOT NULL,
  `employee_id` int(11) DEFAULT NULL,
  `detail` text DEFAULT NULL,
  `action` enum('RECEIVED','PICKED_UP') NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `employees`
--

CREATE TABLE `employees` (
  `id` int(11) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `email` varchar(200) NOT NULL,
  `password_hash` varchar(225) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL,
  `role` varchar(50) NOT NULL DEFAULT 'EMPLOYEE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `employees`
--

INSERT INTO `employees` (`id`, `first_name`, `last_name`, `email`, `password_hash`, `created_at`, `updated_at`, `role`) VALUES
(6, 'Nick', 'Herberg', 'nicholas.herberg@my.metrostate.edu', '$2a$10$2BNXW.ylpB3qg8pQS.Wig.j.kcBW3V815M4TOA5qGZlR.PJow0Ldq', '2025-10-31 14:40:15', NULL, 'ADMIN'),
(7, 'Admin', 'BoxSender', 'admin@boxsender.com', '$2a$10$jlWeuO5aw1lynGcIAgp.4.tWRQ7qvSJqJyaOGV1IubpV8Df/kav9.', '2025-11-01 12:06:56', NULL, 'ADMIN'),
(8, 'John', 'Doe', 'john.doe@boxsender.com', '$2a$10$9WRwAiDCxk5sMZcQfU47jezSYm5cA2xeD7N5hKr6oVxk4zFH2FeWe', '2025-11-01 15:11:28', NULL, 'EMPLOYEE'),
(9, 'Sarah', 'Johnson', 'sarah.johnson@boxsender.com', '$2a$10$KtozXTPUuHIcNm6/zsAwDed501xe6zkjdpKzhdMWJ.gLrZDnW7DmO', '2025-11-01 15:25:10', NULL, 'EMPLOYEE'),
(10, 'Michael', 'Chen', 'michael.chen@boxsender.com', '$2a$10$FACr.nQE0tnVGGzJmRd/Vu503v0aiUEGKsBExi.pfUHk7cVzsx/6e', '2025-11-01 15:26:31', NULL, 'EMPLOYEE'),
(11, 'Emily', 'Rodriguez', 'emily.rodriguez@boxsender.com', '$2a$10$ApMO4fJm/RR1R4kWKW2EQuHgtGOSSTJoVAOVinJRpWtL4RWGPKRFO', '2025-11-01 15:27:02', NULL, 'MAILROOM_STAFF'),
(12, 'James', 'Williams', 'james.williams@boxsender.com', '$2a$10$4NUeZyiZsAoGDOqIK.hMeuaQc8jxWCtIg.SIUQVpJvJH5ZZDdeZIe', '2025-11-01 15:28:12', NULL, 'MAILROOM_STAFF');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `package_id` int(11) NOT NULL,
  `recipient_id` int(11) NOT NULL,
  `message` text DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `packages`
--

CREATE TABLE `packages` (
  `id` int(11) NOT NULL,
  `tracking_number` varchar(255) NOT NULL,
  `carrier` varchar(45) NOT NULL,
  `description` text DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `pickup_code` varchar(10) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `employee_id` int(11) NOT NULL,
  `recipient_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `packages`
--

INSERT INTO `packages` (`id`, `tracking_number`, `carrier`, `description`, `status`, `pickup_code`, `created_at`, `updated_at`, `employee_id`, `recipient_id`) VALUES
(46, '1Z12345678901111', 'UPS', 'Testing Email', 'received', 'BJ85SY', '2025-10-31 15:29:57', '2025-10-31 15:29:57', 6, 21),
(47, '1Z123456789011TT', 'USPS', 'Testing email', 'picked', 'FDGMPX', '2025-10-31 15:36:33', '2025-10-31 15:41:20', 6, 21),
(48, 'AMZN098765', 'Amazon Logistics', 'Amazon package for Alex Smith; Heavy!', 'picked', '6BVVKS', '2025-11-01 11:32:30', '2025-11-01 11:33:39', 6, 22),
(49, 'FED123123451', 'FedEx', 'New Package came in!', 'received', 'R5V5CZ', '2025-11-01 12:25:46', '2025-11-01 12:25:46', 6, 21),
(50, '1Z12WEB09352TEST', 'UPS', 'Test 2', 'received', 'GS6NE8', '2025-11-01 12:30:43', '2025-11-01 12:30:43', 6, 21),
(51, '1Z890349854064', 'UPS', 'Testing Email', 'received', 'RCSE7W', '2025-11-01 13:11:37', '2025-11-01 13:11:37', 6, 21),
(52, 'FEDX0987654321', 'FedEx', 'Email once again', 'received', 'NGCC89', '2025-11-01 13:19:46', '2025-11-01 13:19:46', 6, 21),
(53, '1FEDX0987654321', 'FedEx', '', 'picked', '52DALP', '2025-11-01 13:32:49', '2025-11-01 15:12:30', 6, 21),
(54, 'testing123456789', 'UPS', 'test', 'received', 'Q6VDK6', '2025-11-01 13:34:24', '2025-11-01 13:34:24', 6, 23),
(55, '1Z23435542H00894', 'UPS', 'New Dell Laptop', 'picked', 'UK6TDZ', '2025-11-01 15:40:19', '2025-11-01 15:43:01', 12, 23);

-- --------------------------------------------------------

--
-- Table structure for table `recipients`
--

CREATE TABLE `recipients` (
  `id` int(11) NOT NULL,
  `first_name` varchar(45) NOT NULL,
  `last_name` varchar(45) NOT NULL,
  `email` varchar(200) NOT NULL,
  `department` varchar(120) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `recipients`
--

INSERT INTO `recipients` (`id`, `first_name`, `last_name`, `email`, `department`, `created_at`, `updated_at`) VALUES
(1, 'Robert', 'Martinez', 'robert.martinez@my.metrostate.edu', 'Computer Science', '2025-10-15 09:00:00', '2025-10-15 09:00:00'),
(2, 'Jennifer', 'Taylor', 'jennifer.taylor@my.metrostate.edu', 'Computer Science', '2025-10-16 10:30:00', '2025-10-16 10:30:00'),
(3, 'David', 'Brown', 'david.brown@my.metrostate.edu', 'Computer Science', '2025-10-17 14:00:00', '2025-10-17 14:00:00'),
(4, 'Jessica', 'Wilson', 'jessica.wilson@my.metrostate.edu', 'Computer Science', '2025-10-18 11:15:00', '2025-10-18 11:15:00'),
(5, 'Christopher', 'Moore', 'christopher.moore@my.metrostate.edu', 'Business', '2025-10-19 08:45:00', '2025-10-19 08:45:00'),
(6, 'Amanda', 'Jackson', 'amanda.jackson@my.metrostate.edu', 'Business', '2025-10-20 09:30:00', '2025-10-20 09:30:00'),
(7, 'Matthew', 'Lee', 'matthew.lee@my.metrostate.edu', 'Business', '2025-10-21 13:00:00', '2025-10-21 13:00:00'),
(8, 'Ashley', 'Harris', 'ashley.harris@my.metrostate.edu', 'Psychology', '2025-10-22 10:00:00', '2025-10-22 10:00:00'),
(9, 'Daniel', 'Clark', 'daniel.clark@my.metrostate.edu', 'Psychology', '2025-10-23 11:45:00', '2025-10-23 11:45:00'),
(10, 'Michelle', 'Lewis', 'michelle.lewis@my.metrostate.edu', 'Psychology', '2025-10-24 15:30:00', '2025-10-24 15:30:00'),
(11, 'Ryan', 'Walker', 'ryan.walker@my.metrostate.edu', 'Engineering', '2025-10-25 09:15:00', '2025-10-25 09:15:00'),
(12, 'Stephanie', 'Hall', 'stephanie.hall@my.metrostate.edu', 'Engineering', '2025-10-26 10:45:00', '2025-10-26 10:45:00'),
(13, 'Kevin', 'Allen', 'kevin.allen@my.metrostate.edu', 'Engineering', '2025-10-27 14:20:00', '2025-10-27 14:20:00'),
(14, 'Nicole', 'Young', 'nicole.young@my.metrostate.edu', 'Arts & Humanities', '2025-10-28 11:00:00', '2025-10-28 11:00:00'),
(15, 'Brandon', 'King', 'brandon.king@my.metrostate.edu', 'Arts & Humanities', '2025-10-29 13:30:00', '2025-10-29 13:30:00'),
(16, 'Rachel', 'Wright', 'rachel.wright@metrostate.edu', 'Administration', '2025-10-15 08:00:00', '2025-10-15 08:00:00'),
(17, 'Thomas', 'Lopez', 'thomas.lopez@metrostate.edu', 'Administration', '2025-10-16 09:00:00', '2025-10-16 09:00:00'),
(18, 'Melissa', 'Hill', 'melissa.hill@metrostate.edu', 'Library', '2025-10-17 10:00:00', '2025-10-17 10:00:00'),
(19, 'Eric', 'Scott', 'eric.scott@metrostate.edu', 'Library', '2025-10-18 11:00:00', '2025-10-18 11:00:00'),
(20, 'Dr. Laura', 'Green', 'laura.green@metrostate.edu', 'Faculty - CS', '2025-10-19 08:30:00', '2025-10-19 08:30:00'),
(21, 'Test', 'Testing', 'nicholas.herberg@my.metrostate.edu', NULL, '2025-10-31 15:29:57', '2025-10-31 15:29:57'),
(22, 'Alex', 'Smith', 'alex@yourcompany.com', NULL, '2025-11-01 11:32:30', '2025-11-01 11:32:30'),
(23, 'Box Sender', 'boxer', 'boxsender.ics370@gmail.com', NULL, '2025-11-01 13:34:24', '2025-11-01 13:34:24');

-- --------------------------------------------------------

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `id` bigint(20) NOT NULL,
  `report_type` varchar(50) NOT NULL COMMENT 'DAILY, OVERDUE, RECIPIENT_HISTORY, SUMMARY',
  `title` varchar(200) NOT NULL,
  `generated_date` datetime NOT NULL,
  `generated_by` int(11) NOT NULL,
  `report_data` text DEFAULT NULL,
  `date_range` varchar(100) DEFAULT NULL,
  `record_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activity_log`
--
ALTER TABLE `activity_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_log_pkg` (`package_id`),
  ADD KEY `idx_log_emp` (`employee_id`);

--
-- Indexes for table `employees`
--
ALTER TABLE `employees`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`package_id`,`recipient_id`),
  ADD KEY `fk_packages_has_recipients_recipients1_idx` (`recipient_id`),
  ADD KEY `fk_packages_has_recipients_packages_idx` (`package_id`);

--
-- Indexes for table `packages`
--
ALTER TABLE `packages`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `tracking_number` (`tracking_number`),
  ADD KEY `fk_packages_employees1_idx` (`employee_id`),
  ADD KEY `fk_packages_recipients1_idx` (`recipient_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_updated_at` (`updated_at`),
  ADD KEY `idx_status_created` (`status`,`created_at`),
  ADD KEY `idx_pickup_code` (`pickup_code`);

--
-- Indexes for table `recipients`
--
ALTER TABLE `recipients`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`);

--
-- Indexes for table `reports`
--
ALTER TABLE `reports`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_reports_employees` (`generated_by`),
  ADD KEY `idx_report_type` (`report_type`),
  ADD KEY `idx_generated_date` (`generated_date`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activity_log`
--
ALTER TABLE `activity_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `employees`
--
ALTER TABLE `employees`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `packages`
--
ALTER TABLE `packages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;

--
-- AUTO_INCREMENT for table `recipients`
--
ALTER TABLE `recipients`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `reports`
--
ALTER TABLE `reports`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activity_log`
--
ALTER TABLE `activity_log`
  ADD CONSTRAINT `fk_log_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_log_package` FOREIGN KEY (`package_id`) REFERENCES `packages` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `fk_packages_has_recipients_packages` FOREIGN KEY (`package_id`) REFERENCES `packages` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_packages_has_recipients_recipients1` FOREIGN KEY (`recipient_id`) REFERENCES `recipients` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `packages`
--
ALTER TABLE `packages`
  ADD CONSTRAINT `fk_packages_employees1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_packages_recipients1` FOREIGN KEY (`recipient_id`) REFERENCES `recipients` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `reports`
--
ALTER TABLE `reports`
  ADD CONSTRAINT `fk_reports_employees` FOREIGN KEY (`generated_by`) REFERENCES `employees` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
