-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3307
-- Generation Time: Oct 31, 2025 at 09:08 PM
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

--
-- Dumping data for table `activity_log`
--

INSERT INTO `activity_log` (`id`, `package_id`, `employee_id`, `detail`, `action`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 'Package logged into system', 'RECEIVED', '2025-10-29 09:15:00', '2025-10-31 14:38:37'),
(2, 2, 1, 'Package logged into system', 'RECEIVED', '2025-10-29 10:30:00', '2025-10-31 14:38:37'),
(3, 3, 2, 'Package logged into system', 'RECEIVED', '2025-10-29 11:45:00', '2025-10-31 14:38:37'),
(4, 4, 2, 'Package logged into system', 'RECEIVED', '2025-10-29 13:00:00', '2025-10-31 14:38:37'),
(5, 5, 1, 'Package logged into system', 'RECEIVED', '2025-10-29 14:20:00', '2025-10-31 14:38:37'),
(6, 4, 3, 'Signature: Jessica Wilson', 'PICKED_UP', '2025-10-29 16:30:00', '2025-10-31 14:38:37'),
(7, 5, 2, 'Signature: Christopher Moore', 'PICKED_UP', '2025-10-29 17:45:00', '2025-10-31 14:38:37'),
(8, 9, 4, 'Signature: Daniel Clark', 'PICKED_UP', '2025-10-30 15:00:00', '2025-10-31 14:38:37'),
(9, 10, 3, 'Signature: Michelle Lewis', 'PICKED_UP', '2025-10-30 16:45:00', '2025-10-31 14:38:37'),
(10, 15, 2, 'Signature: Brandon King', 'PICKED_UP', '2025-10-31 16:00:00', '2025-10-31 14:38:37'),
(11, 16, 1, 'Package logged into system', 'RECEIVED', '2025-10-24 09:00:00', '2025-10-31 14:38:37'),
(12, 16, 2, 'Signature: Rachel Wright', 'PICKED_UP', '2025-10-24 14:30:00', '2025-10-31 14:38:37'),
(13, 17, 2, 'Package logged into system', 'RECEIVED', '2025-10-24 10:15:00', '2025-10-31 14:38:37'),
(14, 17, 1, 'Signature: Thomas Lopez', 'PICKED_UP', '2025-10-25 09:00:00', '2025-10-31 14:38:37'),
(15, 26, 1, 'OVERDUE - Reminder sent', 'RECEIVED', '2025-10-15 10:00:00', '2025-10-31 14:38:37'),
(16, 27, 2, 'OVERDUE - Reminder sent', 'RECEIVED', '2025-10-16 11:30:00', '2025-10-31 14:38:37'),
(17, 28, 3, 'OVERDUE - Reminder sent', 'RECEIVED', '2025-10-14 09:00:00', '2025-10-31 14:38:37');

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
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `employees`
--

INSERT INTO `employees` (`id`, `first_name`, `last_name`, `email`, `password_hash`, `created_at`, `updated_at`) VALUES
(1, 'Sarah', 'Johnson', 'sarah.johnson@metrostate.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JvGGg6T8oBBZTJvJJmKqkNJN/FfVmi', '2025-10-01 08:30:00', NULL),
(2, 'Michael', 'Chen', 'michael.chen@metrostate.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JvGGg6T8oBBZTJvJJmKqkNJN/FfVmi', '2025-10-01 08:35:00', NULL),
(3, 'Emily', 'Rodriguez', 'emily.rodriguez@metrostate.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JvGGg6T8oBBZTJvJJmKqkNJN/FfVmi', '2025-10-05 09:00:00', NULL),
(4, 'James', 'Williams', 'james.williams@metrostate.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JvGGg6T8oBBZTJvJJmKqkNJN/FfVmi', '2025-10-10 10:15:00', NULL),
(5, 'Lisa', 'Anderson', 'lisa.anderson@metrostate.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JvGGg6T8oBBZTJvJJmKqkNJN/FfVmi', '2025-10-15 11:00:00', NULL),
(6, 'Nick', 'Herberg', 'nicholas.herberg@my.metrostate.edu', '$2a$10$2BNXW.ylpB3qg8pQS.Wig.j.kcBW3V815M4TOA5qGZlR.PJow0Ldq', '2025-10-31 14:40:15', NULL);

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

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`package_id`, `recipient_id`, `message`, `created_at`, `updated_at`) VALUES
(1, 1, 'Package arrival notification sent', '2025-10-29 09:15:00', '2025-10-31 14:38:37'),
(2, 2, 'Package arrival notification sent', '2025-10-29 10:30:00', '2025-10-31 14:38:37'),
(3, 3, 'Package arrival notification sent', '2025-10-29 11:45:00', '2025-10-31 14:38:37'),
(4, 4, 'Package arrival notification sent', '2025-10-29 13:00:00', '2025-10-31 14:38:37'),
(5, 5, 'Package arrival notification sent', '2025-10-29 14:20:00', '2025-10-31 14:38:37'),
(6, 6, 'Package arrival notification sent', '2025-10-30 08:30:00', '2025-10-31 14:38:37'),
(7, 7, 'Package arrival notification sent', '2025-10-30 09:45:00', '2025-10-31 14:38:37'),
(8, 8, 'Package arrival notification sent', '2025-10-30 11:00:00', '2025-10-31 14:38:37'),
(9, 9, 'Package arrival notification sent', '2025-10-30 12:15:00', '2025-10-31 14:38:37'),
(10, 10, 'Package arrival notification sent', '2025-10-30 13:30:00', '2025-10-31 14:38:37'),
(16, 16, 'Package arrival notification sent', '2025-10-24 09:00:00', '2025-10-31 14:38:37'),
(17, 17, 'Package arrival notification sent', '2025-10-24 10:15:00', '2025-10-31 14:38:37'),
(18, 18, 'Package arrival notification sent', '2025-10-24 11:30:00', '2025-10-31 14:38:37'),
(26, 6, 'Reminder: Package waiting for pickup (7 days)', '2025-10-22 09:00:00', '2025-10-31 14:38:37'),
(27, 7, 'Reminder: Package waiting for pickup (7 days)', '2025-10-23 09:00:00', '2025-10-31 14:38:37'),
(28, 8, 'Reminder: Package waiting for pickup (7 days)', '2025-10-21 09:00:00', '2025-10-31 14:38:37');

-- --------------------------------------------------------

--
-- Table structure for table `packages`
--

CREATE TABLE `packages` (
  `id` int(11) NOT NULL,
  `tracking_number` varchar(50) NOT NULL,
  `carrier` varchar(45) NOT NULL,
  `description` text DEFAULT NULL,
  `status` enum('received','picked') NOT NULL DEFAULT 'received',
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
(1, '1Z999AA10123456784', 'UPS', 'Textbook order from Amazon', 'received', 'KZ2V3F', '2025-10-29 09:15:00', '2025-10-31 15:03:16', 1, 1),
(2, '9400111699000123456780', 'USPS', 'Small envelope - priority mail', 'received', 'PW7XVB', '2025-10-29 10:30:00', '2025-10-31 15:03:16', 1, 2),
(3, '7820123456789', 'FedEx', 'Large box - laptop', 'received', 'N22P5B', '2025-10-29 11:45:00', '2025-10-31 15:03:16', 2, 3),
(4, '1Z999AA10123456785', 'UPS', 'Office supplies', 'picked', 'U5QZKL', '2025-10-29 13:00:00', '2025-10-31 15:03:16', 2, 4),
(5, '9400111699000123456781', 'USPS', 'Package from home', 'picked', 'XRKDV2', '2025-10-29 14:20:00', '2025-10-31 15:03:16', 1, 5),
(6, '1Z999AA10123456786', 'UPS', 'Electronics - fragile', 'received', 'A49QJA', '2025-10-30 08:30:00', '2025-10-31 15:03:16', 3, 6),
(7, '7820123456790', 'FedEx', 'Documents - overnight delivery', 'received', 'LVAG8H', '2025-10-30 09:45:00', '2025-10-31 15:03:16', 3, 7),
(8, 'DHL9876543210', 'DHL', 'International package from Germany', 'received', 'MA83DE', '2025-10-30 11:00:00', '2025-10-31 15:03:16', 2, 8),
(9, '1Z999AA10123456787', 'UPS', 'Textbooks (2 boxes)', 'picked', 'NRHZ7R', '2025-10-30 12:15:00', '2025-10-31 15:03:16', 1, 9),
(10, '9400111699000123456782', 'USPS', 'Small padded envelope', 'picked', 'UQVVJR', '2025-10-30 13:30:00', '2025-10-31 15:03:16', 4, 10),
(11, '7820123456791', 'FedEx', 'Engineering supplies', 'received', 'VT8BRJ', '2025-10-31 08:00:00', '2025-10-31 15:03:16', 3, 11),
(12, '1Z999AA10123456788', 'UPS', 'Art supplies - large box', 'received', '5PW8Z6', '2025-10-31 09:15:00', '2025-10-31 15:03:16', 2, 12),
(13, '9400111699000123456783', 'USPS', 'Media mail - books', 'received', 'HQSLBK', '2025-10-31 10:30:00', '2025-10-31 15:03:16', 1, 13),
(14, 'DHL9876543211', 'DHL', 'Package from UK', 'received', 'JRWZ2W', '2025-10-31 11:45:00', '2025-10-31 15:03:16', 4, 14),
(15, '7820123456792', 'FedEx', 'Lab equipment', 'picked', '4J4LE3', '2025-10-31 13:00:00', '2025-10-31 15:03:16', 3, 15),
(16, '1Z999AA10123456789', 'UPS', 'Conference materials', 'picked', 'SFKBKK', '2025-10-24 09:00:00', '2025-10-31 15:03:16', 1, 16),
(17, '9400111699000123456784', 'USPS', 'Personal package', 'picked', 'UBSMLU', '2025-10-24 10:15:00', '2025-10-31 15:03:16', 2, 17),
(18, '7820123456793', 'FedEx', 'Research materials', 'picked', '4S9NB9', '2025-10-24 11:30:00', '2025-10-31 15:03:16', 3, 18),
(19, '1Z999AA10123456790', 'UPS', 'Office furniture parts', 'picked', '3DCCDP', '2025-10-24 13:45:00', '2025-10-31 15:03:16', 1, 19),
(20, 'DHL9876543212', 'DHL', 'International textbooks', 'picked', 'XDSLCM', '2025-10-24 15:00:00', '2025-10-31 15:03:16', 2, 20),
(21, '9400111699000123456785', 'USPS', 'Computer parts', 'picked', 'WBKNA6', '2025-10-17 08:30:00', '2025-10-31 15:03:16', 4, 1),
(22, '7820123456794', 'FedEx', 'Software license dongle', 'picked', 'NMTZ8T', '2025-10-17 09:45:00', '2025-10-31 15:03:16', 3, 2),
(23, '1Z999AA10123456791', 'UPS', 'Printer toner cartridges', 'picked', '9F7FB2', '2025-10-17 11:00:00', '2025-10-31 15:03:16', 2, 3),
(24, 'DHL9876543213', 'DHL', 'Package from Japan', 'picked', 'XBJD3U', '2025-10-17 13:15:00', '2025-10-31 15:03:16', 1, 4),
(25, '9400111699000123456786', 'USPS', 'Thesis binding materials', 'picked', 'VKVE55', '2025-10-17 14:30:00', '2025-10-31 15:03:16', 4, 5),
(26, '1Z999AA10123456792', 'UPS', 'Unopened package - attempted delivery notice sent', 'received', '3PXCNX', '2025-10-15 10:00:00', '2025-10-31 15:03:16', 1, 6),
(27, '7820123456795', 'FedEx', 'Hold for pickup - customer notified', 'received', 'JL3AYS', '2025-10-16 11:30:00', '2025-10-31 15:03:16', 2, 7),
(28, '9400111699000123456787', 'USPS', 'Certified mail - signature required', 'received', 'PUUE8P', '2025-10-14 09:00:00', '2025-10-31 15:03:16', 3, 8),
(29, 'DHL9876543214', 'DHL', 'Research samples - temperature sensitive', 'picked', 'JBRGWU', '2025-10-10 08:00:00', '2025-10-31 15:03:16', 4, 9),
(30, '1Z999AA10123456793', 'UPS', 'Department supplies', 'picked', '63JAHC', '2025-10-10 10:15:00', '2025-10-31 15:03:16', 1, 10),
(31, '7820123456796', 'FedEx', 'Project materials', 'picked', 'ZRHVQN', '2025-10-10 12:30:00', '2025-10-31 15:03:16', 2, 11),
(32, '9400111699000123456788', 'USPS', 'Exam materials', 'picked', 'UYZWBG', '2025-10-10 14:45:00', '2025-10-31 15:03:16', 3, 12),
(33, '1Z999AA10123456794', 'UPS', 'Library books', 'picked', '8DZMSS', '2025-10-11 08:30:00', '2025-10-31 15:03:16', 4, 13),
(34, '7820123456797', 'FedEx', 'Conference registration materials', 'picked', 'BZTR4V', '2025-10-03 09:00:00', '2025-10-31 15:03:16', 1, 14),
(35, 'DHL9876543215', 'DHL', 'International journal subscription', 'picked', 'WM5FMK', '2025-10-03 11:15:00', '2025-10-31 15:03:16', 2, 15),
(36, '9400111699000123456789', 'USPS', 'Student organization materials', 'picked', 'NDJDYG', '2025-10-04 13:30:00', '2025-10-31 15:03:16', 3, 16),
(37, '1Z999AA10123456795', 'UPS', 'Department equipment', 'picked', '4VT8CU', '2025-10-04 15:45:00', '2025-10-31 15:03:16', 4, 17),
(38, '7820123456798', 'FedEx', 'Marketing materials', 'picked', 'WQLJNF', '2025-10-05 08:00:00', '2025-10-31 15:03:16', 1, 18),
(39, '1Z999AA10123456796', 'UPS', 'IT equipment', 'picked', 'VU9KQN', '2025-10-05 10:15:00', '2025-10-31 15:03:16', 2, 19),
(40, 'DHL9876543216', 'DHL', 'Research grant materials', 'picked', 'TUJWPE', '2025-10-05 12:30:00', '2025-10-31 15:03:16', 3, 20),
(41, '9400111699000123456790', 'USPS', 'Textbook return label enclosed', 'received', 'SD8QQ8', '2025-10-31 14:00:00', '2025-10-31 15:03:16', 4, 1),
(42, '1Z999AA10123456797', 'UPS', 'Amazon Prime - headphones', 'received', 'L39VBM', '2025-10-31 15:15:00', '2025-10-31 15:03:16', 1, 2),
(43, '7820123456799', 'FedEx', 'Graduation regalia', 'received', 'XMXMZU', '2025-10-31 16:30:00', '2025-10-31 15:03:16', 2, 3),
(44, 'AMZN123456789', 'Amazon Logistics', 'Dorm room supplies', 'received', '2B7S5X', '2025-10-31 17:45:00', '2025-10-31 15:03:16', 3, 4),
(45, '1Z999AA10123456798', 'UPS', 'Laptop charger replacement', 'received', '3BZUY3', '2025-10-31 18:00:00', '2025-10-31 15:03:16', 1, 5);

-- --------------------------------------------------------

--
-- Table structure for table `recipients`
--

CREATE TABLE `recipients` (
  `id` int(11) NOT NULL,
  `first_name` varchar(45) NOT NULL,
  `last_name` varchar(45) NOT NULL,
  `email` varchar(225) NOT NULL,
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
(20, 'Dr. Laura', 'Green', 'laura.green@metrostate.edu', 'Faculty - CS', '2025-10-19 08:30:00', '2025-10-19 08:30:00');

-- --------------------------------------------------------

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `id` int(11) NOT NULL,
  `report_type` varchar(50) NOT NULL COMMENT 'DAILY, OVERDUE, RECIPIENT_HISTORY, SUMMARY',
  `title` varchar(200) NOT NULL,
  `generated_date` datetime NOT NULL,
  `generated_by` int(11) NOT NULL,
  `report_data` text DEFAULT NULL,
  `date_range` varchar(100) DEFAULT NULL,
  `record_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Dumping data for table `reports`
--

INSERT INTO `reports` (`id`, `report_type`, `title`, `generated_date`, `generated_by`, `report_data`, `date_range`, `record_count`) VALUES
(1, 'SUMMARY', 'System Summary Report', '2025-10-31 09:00:00', 1, 'Box Sender System Summary Report\r\nGenerated: 2025-10-31 09:00:00\r\n\r\n=== Package Statistics ===\r\nTotal Packages: 45\r\nAwaiting Pickup: 18\r\nPicked Up: 27\r\nPickup Rate: 60.0%\r\n\r\n=== Recipient Statistics ===\r\nTotal Recipients: 20', NULL, 45),
(2, 'DAILY', 'Daily Package Log - 2025-10-31', '2025-10-31 17:00:00', 2, 'Tracking Number,Carrier,Recipient Name,Recipient Email,Status,Logged At\r\n7820123456791,FedEx,Ryan Walker,ryan.walker@my.metrostate.edu,received,2025-10-31 08:00:00\r\n1Z999AA10123456788,UPS,Stephanie Hall,stephanie.hall@my.metrostate.edu,received,2025-10-31 09:15:00\r\n9400111699000123456783,USPS,Kevin Allen,kevin.allen@my.metrostate.edu,received,2025-10-31 10:30:00\r\nDHL9876543211,DHL,Nicole Young,nicole.young@my.metrostate.edu,received,2025-10-31 11:45:00\r\n7820123456792,FedEx,Brandon King,brandon.king@my.metrostate.edu,picked,2025-10-31 13:00:00', '2025-10-31', 5),
(3, 'OVERDUE', 'Overdue Packages (>7 days)', '2025-10-31 08:30:00', 1, 'Overdue Packages Report (older than 7 days)\r\n\r\nTracking Number,Carrier,Recipient Name,Recipient Email,Days Waiting,Logged At\r\n9400111699000123456787,USPS,Ashley Harris,ashley.harris@my.metrostate.edu,17,2025-10-14 09:00:00\r\n1Z999AA10123456792,UPS,Christopher Moore,christopher.moore@my.metrostate.edu,16,2025-10-15 10:00:00\r\n7820123456795,FedEx,Amanda Jackson,amanda.jackson@my.metrostate.edu,15,2025-10-16 11:30:00', NULL, 3),
(4, 'RECIPIENT_HISTORY', 'Package History - Robert Martinez', '2025-10-30 14:00:00', 3, 'Package History for: Robert Martinez (robert.martinez@my.metrostate.edu)\r\n\r\nTracking Number,Carrier,Description,Status,Logged At,Picked Up At\r\n1Z999AA10123456784,UPS,Textbook order from Amazon,received,2025-10-29 09:15:00,Not picked up\r\n9400111699000123456785,USPS,Computer parts,picked,2025-10-17 08:30:00,2025-10-17 15:00:00\r\n9400111699000123456790,USPS,Textbook return label enclosed,received,2025-10-31 14:00:00,Not picked up', NULL, 3);

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `packages`
--
ALTER TABLE `packages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- AUTO_INCREMENT for table `recipients`
--
ALTER TABLE `recipients`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `reports`
--
ALTER TABLE `reports`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

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
