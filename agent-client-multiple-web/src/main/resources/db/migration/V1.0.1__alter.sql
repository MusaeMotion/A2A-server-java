
ALTER TABLE `a2a`.`task`
    ADD COLUMN `message_id` CHAR(36) NULL AFTER `conversation_id`;
