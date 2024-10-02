drop table if exists AccessToken cascade;
drop table if exists RefreshToken cascade;
drop table if exists ImInvitation cascade;
drop table if exists ChannelMember cascade;
drop table if exists ChannelInvitation cascade;
drop table if exists Message cascade;
drop table if exists Channel cascade;
drop table if exists session cascade;
drop table if exists users cascade;


create table users
(
    id       serial primary key,
    name     varchar(30) unique not null,
    password varchar(100)       not null
);

create table Session(
    id         serial primary key,
    user_id    integer   not null references "users" (id) on delete cascade,
    expires_at timestamp not null default current_timestamp + interval '90 day'
);

create table AccessToken
(
    token      varchar(32) primary key,
    session_id integer   not null references session (id) on delete cascade,
    expires_at timestamp not null default current_timestamp + interval '1 day'
);

create table RefreshToken (
    token      varchar(32) primary key,
    session_id integer not null references session(id) on delete cascade
);

create table ImInvitation(
    token      varchar(32) primary key,
    expires_at timestamp not null default current_timestamp + interval '7 day',
    status     varchar(10) not null default 'pending'
        CHECK (status in ('pending', 'used'))
);

create table Channel(
    id         serial primary key,
    name       varchar(30) not null,
    owner      integer     not null references "users" (id) on delete cascade,
    is_public  boolean     not null,
    created_at timestamp   not null default current_timestamp
);

create table ChannelMember(
    channel_id integer     not null references channel (id),
    user_id    integer     not null references "users" (id),
    role       varchar(10) not null default 'member',
    check (role in ('admin', 'member', 'guest')),
    primary key (channel_id, user_id)
);

create table Message(
    id         serial primary key,
    channel_id integer   not null references channel (id) on delete cascade,
    user_id    integer   not null references "users" (id) on delete cascade,
    content    text      not null,
    created_at timestamp not null default current_timestamp,
    edited_at  timestamp          default null
);

create table ChannelInvitation(
    id         serial primary key,
    channel_id integer     not null references channel (id) on delete cascade,
    inviter    integer     not null references users (id) on delete cascade,
    invitee    integer     not null references users (id) on delete cascade,
    expires_at timestamp   not null default current_timestamp + interval '7 day',
    status     varchar(10) not null default 'pending'
        CHECK (status in ('pending', 'accepted', 'rejected'))
);