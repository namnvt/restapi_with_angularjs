drop table if exists programmer;
create table programmer (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) not null,
  company_id bigint,
  created_timestamp timestamp not null,
  deleted_timestamp timestamp null,
  PRIMARY KEY (id)
);

drop table if exists company;
create table company (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) not null,
  url varchar(255),
  created_at timestamp not null,
  deleted_at timestamp null,
  PRIMARY KEY (id)
);

drop table if exists skill;
create table skill (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) not null,
  created_at timestamp not null,
  deleted_at timestamp null,
  PRIMARY KEY (id)
);

drop table if exists programmer_skill;
create table programmer_skill (
  programmer_id bigint not null,
  skill_id bigint not null,
  primary key(programmer_id, skill_id)
);

insert into company (name, url, created_at) values ('Typesafe', 'http://typesafe.com/', current_timestamp);
insert into company (name, url, created_at) values ('Oracle', 'http://www.oracle.com/', current_timestamp);
insert into company (name, url, created_at) values ('Google', 'http://www.google.com/', current_timestamp);
insert into company (name, url, created_at) values ('Microsoft', 'http://www.microsoft.com/', current_timestamp);

insert into skill (name, created_at) values ('Scala', current_timestamp);
insert into skill (name, created_at) values ('Java', current_timestamp);
insert into skill (name, created_at) values ('Ruby', current_timestamp);
insert into skill (name, created_at) values ('MySQL', current_timestamp);
insert into skill (name, created_at) values ('PostgreSQL', current_timestamp);


insert into programmer (name, company_id, created_timestamp) values ('Alice', 1, current_timestamp);
insert into programmer (name, company_id, created_timestamp) values ('Bob', 2, current_timestamp);
insert into programmer (name, company_id, created_timestamp) values ('Chris', 1, current_timestamp);

insert into programmer_skill values (1, 1);
insert into programmer_skill values (1, 2);
insert into programmer_skill values (2, 2);

