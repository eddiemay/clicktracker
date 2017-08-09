create table Click (
  id int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  url varchar(128) NOT NULL,
  recorded timestamp NOT NULL,
  base_url varchar(128),
  lang varchar(8),
  ip_address varchar(64)
);