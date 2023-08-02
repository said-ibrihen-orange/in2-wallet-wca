CREATE DATABASE IF NOT EXISTS `issuer`;
GRANT ALL ON `issuer`.* TO 'in2admin'@'%';

CREATE DATABASE IF NOT EXISTS `wallet`;
GRANT ALL ON `wallet`.* TO 'in2admin'@'%';

CREATE DATABASE IF NOT EXISTS `verifier`;
GRANT ALL ON `verifier`.* TO 'in2admin'@'%';