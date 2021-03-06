--
-- DB script for creating necessary tables (PostgreSQL 9.x)
--

drop sequence if exists hibernate_sequence;

create sequence hibernate_sequence;

--
-- table: message
--
drop table if exists message cascade;

create table message (
    msg_id int8 not null,
    correlation_id varchar(100) not null,
    process_id varchar(100),
    msg_timestamp timestamp not null,
    receive_timestamp timestamp not null,
    service varchar(30) not null,
    source_system varchar(15) not null,
    state varchar(15) not null,
    start_process_timestamp timestamp,
    object_id varchar(50),
    entity_type varchar(30),
    operation_name varchar(100) not null,
    payload text not null,
    envelope text null,
    failed_desc text,
    failed_error_code varchar(5),
    failed_count int4 not null,
    last_update_timestamp timestamp,
    custom_data varchar(20000),
    business_error varchar(20000),
    parent_msg_id int8,
    primary key (msg_id)
);

alter table message add constraint uq_correlation_system unique (correlation_id, source_system);

drop index if exists msg_state_idx;
create index msg_state_idx ON message (state);

-- adding funnel_value
alter table message add column funnel_value varchar(50) null;

drop index if exists funnel_value_idx;
create index funnel_value_idx ON message (funnel_value);


--
-- table: external_call
--
drop table if exists external_call cascade;

create table external_call (
    call_id int8 not null,
    creation_timestamp timestamp not null,
    entity_id varchar(150) not null,
    failed_count integer not null,
    last_update_timestamp timestamp not null,
    msg_timestamp timestamp not null,
    msg_id bigint not null,
    operation_name varchar(100) not null,
    state varchar(20) not null,
    primary key (call_id)
);

alter table external_call add constraint uq_ext_call_operation_entity_id unique (operation_name, entity_id);

alter table external_call add constraint fk_external_call_message foreign key (msg_id) references message;

drop index if exists operation_name_idx;
create index operation_name_idx ON external_call (operation_name);

drop index if exists ext_state_idx;
create index ext_state_idx ON external_call (state);
