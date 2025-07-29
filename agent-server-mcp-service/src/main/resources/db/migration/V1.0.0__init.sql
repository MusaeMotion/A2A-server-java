DROP TABLE IF EXISTS `mcp_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mcp_server` (
                              `id` varchar(45) NOT NULL,
                              `display_name` varchar(45) DEFAULT NULL,
                              `description` varchar(1000) DEFAULT NULL,
                              `name` varchar(1000) DEFAULT NULL,
                              `mcp_config` varchar(1000) DEFAULT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mcp_server`
--

LOCK TABLES `mcp_server` WRITE;
/*!40000 ALTER TABLE `mcp_server` DISABLE KEYS */;
/*!40000 ALTER TABLE `mcp_server` ENABLE KEYS */;
UNLOCK TABLES;