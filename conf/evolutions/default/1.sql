-- !Ups

create table recipes (
    id uuid primary key,
    name text not null,
    description text not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

-- !Downs

drop table recipes;
