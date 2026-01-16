-- USERS
create table users (
                       id bigserial primary key,
                       email varchar(255) not null unique,
                       username varchar(255) not null unique,
                       password varchar(255) not null,
                       created_at timestamp not null default now()
);

-- MOVIES
create table movies (
                        id bigserial primary key,
                        external_id varchar(64) not null unique,
                        title varchar(255) not null,
                        year integer,
                        runtime_minutes integer,
                        poster_url varchar(1024),
                        overview text,
                        created_at timestamp not null default now()
);

-- GENRES
create table genres (
                        id bigserial primary key,
                        name varchar(255) not null unique,
                        slug varchar(255) not null
);

-- MOVIE <-> GENRE (many-to-many)
create table movie_genres (
                              movie_id bigint not null references movies(id) on delete cascade,
                              genre_id bigint not null references genres(id) on delete cascade,
                              primary key (movie_id, genre_id)
);

-- WATCHLISTS
create table watchlists (
                            id bigserial primary key,
                            user_id bigint not null references users(id) on delete cascade,
                            title varchar(255) not null,
                            description text,
                            created_at timestamp not null default now()
);

-- WATCHLIST ITEMS
create table watchlist_items (
                                 id bigserial primary key,
                                 watchlist_id bigint not null references watchlists(id) on delete cascade,
                                 movie_id bigint not null references movies(id) on delete cascade,
                                 position integer not null,
                                 added_at timestamp not null default now(),
                                 constraint uq_watchlist_movie unique (watchlist_id, movie_id)
);

create index idx_watchlist_items_watchlist on watchlist_items(watchlist_id);

-- USER MOVIES
create type watch_status as enum ('PLANNED', 'WATCHED');

create table user_movies (
                             id bigserial primary key,
                             user_id bigint not null references users(id) on delete cascade,
                             movie_id bigint not null references movies(id) on delete cascade,
                             status watch_status not null,
                             rating integer,
                             liked boolean not null default false,
                             watched_at date,
                             created_at timestamp not null default now(),
                             updated_at timestamp not null default now(),
                             constraint uq_user_movie unique (user_id, movie_id)
);

create index idx_user_movies_user on user_movies(user_id);
create index idx_user_movies_movie on user_movies(movie_id);
create index idx_user_movies_status on user_movies(status);
