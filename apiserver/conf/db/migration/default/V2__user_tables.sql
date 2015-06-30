drop table if exists subscriber;

CREATE TABLE subscriber (
  id bigint NOT NULL AUTO_INCREMENT,
  username VARCHAR(255),
  email VARCHAR(255),
  password VARCHAR(255),
  created_at timestamp not null,
  deleted_at timestamp null,

  PRIMARY KEY (id)
);

insert into subscriber (username, email, password, created_at) values ('admin', 'nam.nvt@cosatech-vn.com', 'admin', current_timestamp);