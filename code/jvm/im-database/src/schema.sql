drop table if exists access_token cascade;
drop table if exists refresh_token cascade;
drop table if exists im_invitation cascade;
drop table if exists channel_member cascade;
drop table if exists channel_invitation cascade;
drop table if exists message cascade;
drop table if exists channel cascade;
drop table if exists session cascade;
drop table if exists users cascade;


create table users
(
    id       bigserial primary key,
    name     varchar(30) unique not null,
    password varchar(30)       not null,
    email    varchar(50) unique not null,
    check ( email ~* '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$' )
);

create table session
(
    id         bigserial primary key,
    user_id    bigint    not null references "users" (id) on delete cascade,
    expires_at timestamp not null default current_timestamp + interval '90 day'
);

create table access_token
(
    token      uuid primary key,
    session_id bigint    not null references session (id) on delete cascade,
    expires_at timestamp not null default current_timestamp + interval '1 day'
);

create table refresh_token
(
    token      uuid primary key,
    session_id bigint not null references session(id) on delete cascade
);

create table im_invitation
(
    token      uuid primary key,
    expires_at timestamp   not null default current_timestamp + interval '7 day',
    status     varchar(10) not null default 'PENDING'
        CHECK (status in ('PENDING', 'USED'))
);

create table channel
(
    id         bigserial primary key,
    name       varchar(30) not null,
    owner      bigint      not null references "users" (id) on delete cascade,
    is_public  boolean     not null,
    created_at timestamp   not null default current_timestamp
);

create table channel_member
(
    channel_id bigint      not null references channel (id) on delete cascade,
    user_id    bigint      not null references "users" (id) on delete cascade,
    role       varchar(10) not null default 'MEMBER',
    check (role in ('OWNER', 'MEMBER', 'GUEST')),
    primary key (channel_id, user_id)
);

create table message
(
    id         bigserial primary key,
    channel_id bigint    not null references channel (id) on delete cascade,
    user_id    bigint    not null references "users" (id) on delete cascade,
    content    text      not null,
    created_at timestamp not null default current_timestamp,
    edited_at  timestamp          default null
);

create table channel_invitation(
    id         bigserial primary key,
    channel_id bigint      not null references channel (id) on delete cascade,
    inviter    bigint      not null references users (id) on delete cascade,
    invitee    bigint      not null references users (id) on delete cascade,
    expires_at timestamp   not null default current_timestamp + interval '7 day',
    role       varchar(10) not null default 'MEMBER',
    status     varchar(10) not null default 'PENDING',
    CHECK (status in ('PENDING', 'ACCEPTED', 'REJECTED')),
    CHECK (role in ('MEMBER', 'GUEST'))
);

select * from users;
select * from channel;
select * from channel_member;