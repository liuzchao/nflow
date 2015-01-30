create table if not exists nflow_workflow (
  id int not null auto_increment primary key,
  status enum('created', 'executing', 'inProgress', 'finished', 'manual') not null,
  type varchar(64) not null,
  business_key varchar(64),
  external_id varchar(64) not null,
  state varchar(64) not null,
  state_text varchar(128),
  next_activation timestamp null,
  executor_id int,
  retries int not null default 0,
  created timestamp not null,
  modified timestamp not null default current_timestamp on update current_timestamp,
  executor_group varchar(64) not null,
  constraint nflow_workflow_uniq unique (type, external_id, executor_group),
  index nflow_workflow(next_activation)
);

drop trigger if exists nflow_workflow_insert;

create trigger nflow_workflow_insert before insert on `nflow_workflow`
  for each row set new.created = now();

create table if not exists nflow_workflow_action (
  id int not null auto_increment primary key,
  workflow_id int not null,
  executor_id int not null default -1,
  type enum('stateExecution', 'stateExecutionFailed', 'recovery', 'externalChange') not null,
  state varchar(64) not null,
  state_text varchar(128),
  retry_no int not null,
  execution_start timestamp not null,
  execution_end timestamp not null,
  foreign key (workflow_id) references nflow_workflow(id) on delete cascade
);

create table if not exists nflow_workflow_state (
  workflow_id int not null,
  action_id int not null,
  state_key varchar(64) not null,
  state_value varchar(10240) not null,
  primary key (workflow_id, action_id, state_key),
  foreign key (workflow_id) references nflow_workflow(id) on delete cascade
);

create table if not exists nflow_executor (
  id int not null auto_increment primary key,
  host varchar(64) not null,
  pid int not null,
  executor_group varchar(64),
  started timestamp not null default current_timestamp,
  active timestamp,
  expires timestamp
);

create table if not exists nflow_workflow_definition (
  type varchar(64) not null,
  definition_sha1 varchar(40) not null,
  definition text not null,
  modified timestamp not null default current_timestamp on update current_timestamp,
  modified_by int not null,
  created timestamp not null,
  executor_group varchar(64) not null,
  primary key (type, executor_group)
);

drop trigger if exists nflow_workflow_definition_insert;

create trigger nflow_workflow_definition_insert before insert on `nflow_workflow_definition`
  for each row set new.created = now();